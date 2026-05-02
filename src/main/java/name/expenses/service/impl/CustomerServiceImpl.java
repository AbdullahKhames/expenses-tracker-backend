package name.expenses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.account.AccountRespDto;
import name.expenses.dto.budget.BudgetRespDto;
import name.expenses.dto.budget_transfer.BudgetTransferRespDto;
import name.expenses.dto.category.CategoryRespDto;
import name.expenses.dto.customer.CustomerReqDto;
import name.expenses.dto.customer.CustomerRespDto;
import name.expenses.dto.customer.CustomerUpdateDto;
import name.expenses.dto.expense.ExpenseRespDto;
import name.expenses.dto.sub_category.SubCategoryRespDto;
import name.expenses.dto.transaction.TransactionRespDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.Page;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.*;
import name.expenses.model.*;
import name.expenses.model.enums.EntityType;
import name.expenses.repository.CustomerRepository;
import name.expenses.repository.SubCategoryRepository;
import name.expenses.service.CustomerService;
import name.expenses.utils.FieldValidator;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final CustomerMapper customerMapper;
    private final AccountMapper accountMapper;
    private final BudgetMapper budgetMapper;
    private final ExpenseMapper expenseMapper;
    private final TransactionMapper transactionMapper;
    private final BudgetTransferMapper budgetTransferMapper;

    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public ResponseDto create(CustomerReqDto reqDto) {
        Customer customer = customerMapper.reqDtoToEntity(reqDto);
        customer = customerRepository.save(customer);
        log.info("Created customer with refNo: {}", customer.getRefNo());
        return ResponseDtoBuilder.getCreateResponse("Customer", customer.getRefNo(),
                customerMapper.entityToRespDto(customer));
    }

    @Override
    @Cacheable("customers")
    public ResponseDto get(String refNo) {
        Customer customer = customerRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Customer not found with refNo: " + refNo));
        return ResponseDtoBuilder.getFetchResponse("Customer", refNo,
                customerMapper.entityToRespDto(customer));
    }

    @Override
    public Optional<Customer> getEntity(String refNo) {
        return customerRepository.findByRefNoAndDeletedFalse(refNo);
    }

    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public ResponseDto update(String refNo, CustomerUpdateDto updateDto) {
        Customer customer = customerRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Customer not found with refNo: " + refNo));
        customerMapper.updateEntityFromDto(updateDto, customer);
        customer = customerRepository.save(customer);
        log.info("Updated customer with refNo: {}", refNo);
        return ResponseDtoBuilder.getUpdateResponse("Customer", refNo,
                customerMapper.entityToRespDto(customer));
    }

    @Override
    @CacheEvict(value = "customers", allEntries = true)
    public ResponseDto delete(String refNo) {
        Customer customer = customerRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Customer not found with refNo: " + refNo));
        customer.setDeleted(true);
        customerRepository.save(customer);
        log.info("Soft-deleted customer with refNo: {}", refNo);
        return ResponseDtoBuilder.getDeleteResponse("Customer", refNo);
    }

    @Override
    public ResponseDto getAllEntities(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, Customer.class)) {
            sortBy = "id";
        }

        Sort sort = sortDirection == SortDirection.DESC
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);

        PageRequest pageRequest = PageRequest.of(
                pageNumber.intValue() - 1,
                pageSize.intValue(),
                sort
        );

        org.springframework.data.domain.Page<Customer> entityPage = customerRepository.findAllByDeletedFalse(pageRequest);
        Page<CustomerRespDto> dtoPage = customerMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("Customer", dtoPage);
    }

    @Override
    public Set<Customer> getEntities(Set<String> refNos) {
        return new HashSet<>(customerRepository.findAllById(
                customerRepository.findAll().stream()
                        .filter(c -> refNos.contains(c.getRefNo()) && !c.isDeleted())
                        .map(Customer::getId)
                        .collect(Collectors.toSet())
        ));
    }

    @Override
    public ResponseDto getAllCustomerAccounts(Long page, Long perPage, String sortBy, SortDirection dir) {
        Customer customer = getCurrentCustomer();
        List<AccountRespDto> dtos = customer.getAccounts().stream()
                .filter(a -> !a.isDeleted())
                .map(accountMapper::entityToRespDto)
                .collect(Collectors.toList());
        return buildManualPage(dtos, page, perPage, "Account");
    }

    @Override
    public ResponseDto getAllCustomerAccountBudgets(String accountRef, Long page, Long perPage, String sortBy, SortDirection dir) {
        Customer customer = getCurrentCustomer();
        Account account = customer.getAccounts().stream()
                .filter(a -> a.getRefNo().equals(accountRef) && !a.isDeleted())
                .findFirst()
                .orElseThrow(() -> new ObjectNotFoundException("Account not found with refNo: " + accountRef));
        List<BudgetRespDto> dtos = account.getBudgets().stream()
                .filter(b -> !b.isDeleted())
                .map(budgetMapper::entityToRespDto)
                .collect(Collectors.toList());
        return buildManualPage(dtos, page, perPage, "Budget");
    }

    @Override
    public ResponseDto getAllCustomerSubCategoryExpenses(String subCategoryRef, Long page, Long perPage, String sortBy, SortDirection dir) {
        Customer customer = getCurrentCustomer();
        SubCategory subCategory = subCategoryRepository.getByRefNoAndDeletedFalse(subCategoryRef)
                .orElseThrow(()-> new ObjectNotFoundException("SubCategory not found with refNo: " + subCategoryRef));

        List<ExpenseRespDto> dtos = subCategory.getExpenses().stream()
                .filter(e -> !e.isDeleted())
                .map(expenseMapper::entityToRespDto)
                .collect(Collectors.toList());
        return buildManualPage(dtos, page, perPage, "Expense");
    }

    @Override
    public ResponseDto getAllCustomerBudgets(Long page, Long perPage, String sortBy, SortDirection dir) {
        Customer customer = getCurrentCustomer();
        List<BudgetRespDto> dtos = customer.getBudgets().stream()
                .filter(b -> !b.isDeleted())
                .map(budgetMapper::entityToRespDto)
                .collect(Collectors.toList());
        return buildManualPage(dtos, page, perPage, "Budget");
    }

    @Override
    public ResponseDto getAllCustomerExpenses(Long page, Long perPage, String sortBy, SortDirection dir) {
        Customer customer = getCurrentCustomer();
        List<ExpenseRespDto> dtos = customer.getExpenses().stream()
                .filter(e -> !e.isDeleted())
                .map(expenseMapper::entityToRespDto)
                .collect(Collectors.toList());
        return buildManualPage(dtos, page, perPage, "Expense");
    }

    @Override
    public ResponseDto getAllCustomerTransactions(Long page, Long perPage, String sortBy, SortDirection dir) {
        Customer customer = getCurrentCustomer();
        List<TransactionRespDto> dtos = customer.getTransactions().stream()
                .filter(t -> !t.isDeleted())
                .map(transactionMapper::entityToRespDto)
                .collect(Collectors.toList());
        return buildManualPage(dtos, page, perPage, "Transaction");
    }

    @Override
    public ResponseDto getAllCustomerBudgetTransfers(Long page, Long perPage, String sortBy, SortDirection dir) {
        Customer customer = getCurrentCustomer();
        List<BudgetTransferRespDto> dtos = customer.getBudgetTransfers().stream()
                .filter(bt -> !bt.isDeleted())
                .map(budgetTransferMapper::entityToRespDto)
                .collect(Collectors.toList());
        return buildManualPage(dtos, page, perPage, "BudgetTransfer");
    }

    @Override
    public ResponseDto getCustomerAssociation(EntityType entityType) {
        Customer customer = getCurrentCustomer();
        Object data;
        switch (entityType) {
            case ACCOUNT:
                data = customer.getAccounts().stream()
                        .filter(a -> !a.isDeleted())
                        .map(accountMapper::entityToRespDto)
                        .collect(Collectors.toSet());
                break;
            case BUDGET:
                data = customer.getBudgets().stream()
                        .filter(b -> !b.isDeleted())
                        .map(budgetMapper::entityToRespDto)
                        .collect(Collectors.toSet());
                break;
            case EXPENSE:
                data = customer.getExpenses().stream()
                        .filter(e -> !e.isDeleted())
                        .map(expenseMapper::entityToRespDto)
                        .collect(Collectors.toSet());
                break;
            case TRANSACTION:
                data = customer.getTransactions().stream()
                        .filter(t -> !t.isDeleted())
                        .map(transactionMapper::entityToRespDto)
                        .collect(Collectors.toSet());
                break;
            case BUDGET_TRANSFER:
                data = customer.getBudgetTransfers().stream()
                        .filter(bt -> !bt.isDeleted())
                        .map(budgetTransferMapper::entityToRespDto)
                        .collect(Collectors.toSet());
                break;
            default:
                data = Collections.emptySet();
                break;
        }
        return ResponseDtoBuilder.getFetchResponse("Customer " + entityType.name(), customer.getRefNo(), data);
    }

    private Customer getCurrentCustomer() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return customerRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("Customer not found for current user"));
    }

    private <T> ResponseDto buildManualPage(List<T> allItems, Long page, Long perPage, String resourceName) {
        int pageInt = page.intValue();
        int perPageInt = perPage.intValue();
        int totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / perPageInt);
        int fromIndex = Math.min((pageInt - 1) * perPageInt, totalElements);
        int toIndex = Math.min(fromIndex + perPageInt, totalElements);

        List<T> content = allItems.subList(fromIndex, toIndex);

        Page<T> dtoPage = Page.<T>builder()
                .content(content)
                .pageNumber((long) pageInt)
                .pageSize((long) perPageInt)
                .totalElements((long) totalElements)
                .totalPages((long) totalPages)
                .hasNext(pageInt < totalPages)
                .hasPrevious(pageInt > 1)
                .build();

        return ResponseDtoBuilder.getFetchAllResponse(resourceName, dtoPage);
    }
}
