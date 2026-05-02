package name.expenses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.budget_transfer.BudgetAmountReqDto;
import name.expenses.dto.budget_transfer.BudgetTransferReqDto;
import name.expenses.dto.budget_transfer.BudgetTransferRespDto;
import name.expenses.dto.budget_transfer.BudgetTransferUpdateDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.BudgetAmountMapper;
import name.expenses.mapper.BudgetTransferMapper;
import name.expenses.model.Budget;
import name.expenses.model.BudgetAmount;
import name.expenses.model.BudgetTransfer;
import name.expenses.model.Customer;
import name.expenses.model.User;
import name.expenses.model.enums.AmountType;
import name.expenses.repository.BudgetRepository;
import name.expenses.repository.BudgetTransferRepository;
import name.expenses.repository.CustomerRepository;
import name.expenses.service.BudgetTransferService;
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
public class BudgetTransferServiceImpl implements BudgetTransferService {

    private final BudgetTransferRepository budgetTransferRepository;
    private final BudgetTransferMapper budgetTransferMapper;
    private final BudgetAmountMapper budgetAmountMapper;
    private final BudgetRepository budgetRepository;
    private final CustomerRepository customerRepository;

    @Override
    @CacheEvict(value = {"budgetTransfers", "budgets"}, allEntries = true)
    public ResponseDto create(BudgetTransferReqDto reqDto) {
        Customer customer = getCurrentCustomer();

        // Map basic fields (name, details, amount, lending) from DTO
        BudgetTransfer budgetTransfer = budgetTransferMapper.reqDtoToEntity(reqDto);

        // Process sender budget amount: find budget by refNo, create BudgetAmount with DEBIT, subtract from budget balance
        BudgetAmountReqDto senderReqDto = reqDto.getSenderBudgetAmount();
        Budget senderBudget = budgetRepository.findByRefNoAndDeletedFalse(senderReqDto.getBudgetRefNo())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Budget not found with refNo: " + senderReqDto.getBudgetRefNo()));

        BudgetAmount senderBudgetAmount = new BudgetAmount();
        senderBudgetAmount.setBudget(senderBudget);
        senderBudgetAmount.setAmount(senderReqDto.getAmount());
        senderBudgetAmount.setAmountType(AmountType.DEBIT);

        // Subtract from sender budget balance
        senderBudget.setAmount(senderBudget.getAmount() - senderReqDto.getAmount());
        budgetRepository.save(senderBudget);

        budgetTransfer.setSenderBudgetAmount(senderBudgetAmount);

        // Process each receiver budget amount: find budget by refNo, create BudgetAmount with CREDIT, add to budget balance
        Set<BudgetAmount> receiverBudgetAmounts = new HashSet<>();
        for (BudgetAmountReqDto receiverReqDto : reqDto.getReceiverBudgetAmounts()) {
            Budget receiverBudget = budgetRepository.findByRefNoAndDeletedFalse(receiverReqDto.getBudgetRefNo())
                    .orElseThrow(() -> new ObjectNotFoundException(
                            "Budget not found with refNo: " + receiverReqDto.getBudgetRefNo()));

            BudgetAmount receiverBudgetAmount = new BudgetAmount();
            receiverBudgetAmount.setBudget(receiverBudget);
            receiverBudgetAmount.setAmount(receiverReqDto.getAmount());
            receiverBudgetAmount.setAmountType(AmountType.CREDIT);

            // Add to receiver budget balance
            receiverBudget.setAmount(receiverBudget.getAmount() + receiverReqDto.getAmount());
            budgetRepository.save(receiverBudget);

            receiverBudgetAmounts.add(receiverBudgetAmount);
        }

        budgetTransfer.setReceiverBudgetAmounts(receiverBudgetAmounts);
        budgetTransfer.setCustomer(customer);

        // Save transfer (cascades budget amounts)
        budgetTransfer = budgetTransferRepository.save(budgetTransfer);

        // Add to customer's budgetTransfers collection
        customer.getBudgetTransfers().add(budgetTransfer);

        log.info("Created budget transfer with refNo: {}", budgetTransfer.getRefNo());
        return ResponseDtoBuilder.getCreateResponse("BudgetTransfer", budgetTransfer.getRefNo(),
                budgetTransferMapper.entityToRespDto(budgetTransfer));
    }

    @Override
    @Cacheable("budgetTransfers")
    public ResponseDto get(String refNo) {
        BudgetTransfer budgetTransfer = budgetTransferRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("BudgetTransfer not found with refNo: " + refNo));
        return ResponseDtoBuilder.getFetchResponse("BudgetTransfer", refNo,
                budgetTransferMapper.entityToRespDto(budgetTransfer));
    }

    @Override
    public Optional<BudgetTransfer> getEntity(String refNo) {
        return budgetTransferRepository.findByRefNoAndDeletedFalse(refNo);
    }

    @Override
    @CacheEvict(value = "budgetTransfers", allEntries = true)
    public ResponseDto update(String refNo, BudgetTransferUpdateDto updateDto) {
        BudgetTransfer budgetTransfer = budgetTransferRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("BudgetTransfer not found with refNo: " + refNo));

        // Update basic fields only (name, details, amount, lending) — budget amounts are not updated
        budgetTransferMapper.updateEntityFromDto(updateDto, budgetTransfer);
        budgetTransfer = budgetTransferRepository.save(budgetTransfer);

        log.info("Updated budget transfer with refNo: {}", refNo);
        return ResponseDtoBuilder.getUpdateResponse("BudgetTransfer", refNo,
                budgetTransferMapper.entityToRespDto(budgetTransfer));
    }

    @Override
    @CacheEvict(value = {"budgetTransfers", "budgets"}, allEntries = true)
    public ResponseDto delete(String refNo) {
        BudgetTransfer budgetTransfer = budgetTransferRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("BudgetTransfer not found with refNo: " + refNo));
        budgetTransfer.setDeleted(true);
        budgetTransferRepository.save(budgetTransfer);
        log.info("Soft-deleted budget transfer with refNo: {}", refNo);
        return ResponseDtoBuilder.getDeleteResponse("BudgetTransfer", refNo);
    }

    @Override
    public ResponseDto getAllEntities(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, BudgetTransfer.class)) {
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

        Page<BudgetTransfer> entityPage = budgetTransferRepository.findAllByDeletedFalse(pageRequest);
        name.expenses.globals.Page<BudgetTransferRespDto> dtoPage = budgetTransferMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("BudgetTransfer", dtoPage);
    }

    @Override
    public Set<BudgetTransfer> getEntities(Set<String> refNos) {
        return budgetTransferRepository.findAllByRefNoIn(refNos);
    }

    private Customer getCurrentCustomer() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return customerRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new ObjectNotFoundException("Customer not found for current user"));
    }
}
