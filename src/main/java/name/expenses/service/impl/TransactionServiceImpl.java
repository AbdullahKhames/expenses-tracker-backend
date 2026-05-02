package name.expenses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.budget_transfer.BudgetAmountReqDto;
import name.expenses.dto.transaction.TransactionReqDto;
import name.expenses.dto.transaction.TransactionRespDto;
import name.expenses.dto.transaction.TransactionUpdateDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.BudgetAmountMapper;
import name.expenses.mapper.ExpenseMapper;
import name.expenses.mapper.TransactionMapper;
import name.expenses.model.Budget;
import name.expenses.model.BudgetAmount;
import name.expenses.model.Customer;
import name.expenses.model.Expense;
import name.expenses.model.Transaction;
import name.expenses.model.User;
import name.expenses.model.enums.AmountType;
import name.expenses.repository.BudgetRepository;
import name.expenses.repository.CustomerRepository;
import name.expenses.repository.TransactionRepository;
import name.expenses.service.TransactionService;
import name.expenses.utils.FieldValidator;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final ExpenseMapper expenseMapper;
    private final BudgetAmountMapper budgetAmountMapper;
    private final BudgetRepository budgetRepository;
    private final CustomerRepository customerRepository;

    @Override
    @CacheEvict(value = {"transactions", "budgets"}, allEntries = true)
    public ResponseDto create(TransactionReqDto reqDto) {
        Customer customer = getCurrentCustomer();

        // Map basic transaction fields (name, details)
        Transaction transaction = transactionMapper.reqDtoToEntity(reqDto);

        // Map and set the expense
        Expense expense = expenseMapper.reqDtoToEntity(reqDto.getExpense());
        transaction.setExpense(expense);

        // Process budget amounts and adjust budget balances
        Set<BudgetAmount> budgetAmounts = new HashSet<>();
        double totalAmount = 0.0;

        for (BudgetAmountReqDto baReqDto : reqDto.getBudgetAmounts()) {
            Budget budget = budgetRepository.findByRefNoAndDeletedFalse(baReqDto.getBudgetRefNo())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "Budget not found with refNo: " + baReqDto.getBudgetRefNo()));

            BudgetAmount budgetAmount = new BudgetAmount();
            budgetAmount.setBudget(budget);
            budgetAmount.setAmount(baReqDto.getAmount());
            budgetAmount.setAmountType(baReqDto.getAmountType());

            // Adjust budget balance based on amount type
            if (baReqDto.getAmountType() == AmountType.DEBIT) {
                budget.setAmount(budget.getAmount() - baReqDto.getAmount());
            } else {
                budget.setAmount(budget.getAmount() + baReqDto.getAmount());
            }
            budgetRepository.save(budget);

            budgetAmounts.add(budgetAmount);
            totalAmount += baReqDto.getAmount();
        }

        transaction.setBudgetAmounts(budgetAmounts);
        transaction.setAmount(totalAmount);
        transaction.setCustomer(customer);

        // Save transaction (cascades expense and budget amounts)
        transaction = transactionRepository.save(transaction);

        // Add transaction to customer's transactions collection
        customer.getTransactions().add(transaction);

        log.info("Created transaction with refNo: {}", transaction.getRefNo());
        return ResponseDtoBuilder.getCreateResponse("Transaction", transaction.getRefNo(),
                transactionMapper.entityToRespDto(transaction));
    }

    @Override
    @Cacheable("transactions")
    public ResponseDto get(String refNo) {
        Transaction transaction = transactionRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Transaction not found with refNo: " + refNo));
        return ResponseDtoBuilder.getFetchResponse("Transaction", refNo,
                transactionMapper.entityToRespDto(transaction));
    }

    @Override
    public Optional<Transaction> getEntity(String refNo) {
        return transactionRepository.findByRefNoAndDeletedFalse(refNo);
    }

    @Override
    @CacheEvict(value = "transactions", allEntries = true)
    public ResponseDto update(String refNo, TransactionUpdateDto updateDto) {
        Transaction transaction = transactionRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Transaction not found with refNo: " + refNo));

        // Update basic fields only (name, details) — budget amounts are not updated
        transactionMapper.updateEntityFromDto(updateDto, transaction);
        transaction = transactionRepository.save(transaction);

        log.info("Updated transaction with refNo: {}", refNo);
        return ResponseDtoBuilder.getUpdateResponse("Transaction", refNo,
                transactionMapper.entityToRespDto(transaction));
    }

    @Override
    @CacheEvict(value = {"transactions", "budgets"}, allEntries = true)
    public ResponseDto delete(String refNo) {
        Transaction transaction = transactionRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Transaction not found with refNo: " + refNo));
        transaction.setDeleted(true);
        transactionRepository.save(transaction);
        log.info("Soft-deleted transaction with refNo: {}", refNo);
        return ResponseDtoBuilder.getDeleteResponse("Transaction", refNo);
    }

    @Override
    public ResponseDto getAllEntities(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, Transaction.class)) {
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

        Page<Transaction> entityPage = transactionRepository.findAllByDeletedFalse(pageRequest);
        name.expenses.globals.Page<TransactionRespDto> dtoPage = transactionMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("Transaction", dtoPage);
    }

    @Override
    public Set<Transaction> getEntities(Set<String> refNos) {
        return transactionRepository.findAllByRefNoIn(refNos);
    }

    private Customer getCurrentCustomer() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return customerRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("Customer not found for current user"));
    }
}
