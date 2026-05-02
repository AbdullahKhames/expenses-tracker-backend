package name.expenses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.expense.ExpenseReqDto;
import name.expenses.dto.expense.ExpenseRespDto;
import name.expenses.dto.expense.ExpenseUpdateDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.ExpenseMapper;
import name.expenses.model.Expense;
import name.expenses.repository.ExpenseRepository;
import name.expenses.service.ExpenseService;
import name.expenses.utils.FieldValidator;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    @Override
    @CacheEvict(value = "expenses", allEntries = true)
    public ResponseDto create(ExpenseReqDto reqDto) {
        Expense expense = expenseMapper.reqDtoToEntity(reqDto);
        expense = expenseRepository.save(expense);
        log.info("Created expense with refNo: {}", expense.getRefNo());
        return ResponseDtoBuilder.getCreateResponse("Expense", expense.getRefNo(),
                expenseMapper.entityToRespDto(expense));
    }

    @Override
    @Cacheable("expenses")
    public ResponseDto get(String refNo) {
        Expense expense = expenseRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Expense not found with refNo: " + refNo));
        return ResponseDtoBuilder.getFetchResponse("Expense", refNo,
                expenseMapper.entityToRespDto(expense));
    }

    @Override
    public Optional<Expense> getEntity(String refNo) {
        return expenseRepository.findByRefNoAndDeletedFalse(refNo);
    }

    @Override
    @CacheEvict(value = "expenses", allEntries = true)
    public ResponseDto update(String refNo, ExpenseUpdateDto updateDto) {
        Expense expense = expenseRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Expense not found with refNo: " + refNo));
        expenseMapper.updateEntityFromDto(updateDto, expense);
        expense = expenseRepository.save(expense);
        log.info("Updated expense with refNo: {}", refNo);
        return ResponseDtoBuilder.getUpdateResponse("Expense", refNo,
                expenseMapper.entityToRespDto(expense));
    }

    @Override
    @CacheEvict(value = "expenses", allEntries = true)
    public ResponseDto delete(String refNo) {
        Expense expense = expenseRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Expense not found with refNo: " + refNo));
        expense.setDeleted(true);
        expenseRepository.save(expense);
        log.info("Soft-deleted expense with refNo: {}", refNo);
        return ResponseDtoBuilder.getDeleteResponse("Expense", refNo);
    }

    @Override
    public ResponseDto getAllEntities(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, Expense.class)) {
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

        Page<Expense> entityPage = expenseRepository.findAllByDeletedFalse(pageRequest);
        name.expenses.globals.Page<ExpenseRespDto> dtoPage = expenseMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("Expense", dtoPage);
    }

    @Override
    public Set<Expense> getEntities(Set<String> refNos) {
        return expenseRepository.findAllByRefNoIn(refNos);
    }

    @Override
    public ResponseDto getExpenseByName(String name) {
        var expenses = expenseRepository.findByNameContainingIgnoreCaseAndDeletedFalse(name);
        var dtos = expenseMapper.entitiesToRespDtos(expenses);
        return ResponseDtoBuilder.getFetchAllResponse("Expense", dtos);
    }

    @Override
    public ResponseDto getAllEntitiesWithoutSubCategory(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, Expense.class)) {
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

        Page<Expense> entityPage = expenseRepository.findAllBySubCategoryIsNullAndDeletedFalse(pageRequest);
        name.expenses.globals.Page<ExpenseRespDto> dtoPage = expenseMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("Expense", dtoPage);
    }
}
