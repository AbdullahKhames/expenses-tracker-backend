package name.expenses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.budget.BudgetReqDto;
import name.expenses.dto.budget.BudgetRespDto;
import name.expenses.dto.budget.BudgetUpdateDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.BudgetMapper;
import name.expenses.model.Budget;
import name.expenses.repository.BudgetRepository;
import name.expenses.service.BudgetService;
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
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;

    @Override
    @CacheEvict(value = "budgets", allEntries = true)
    public ResponseDto create(BudgetReqDto reqDto) {
        Budget budget = budgetMapper.reqDtoToEntity(reqDto);
        budget = budgetRepository.save(budget);
        log.info("Created budget with refNo: {}", budget.getRefNo());
        return ResponseDtoBuilder.getCreateResponse("Budget", budget.getRefNo(),
                budgetMapper.entityToRespDto(budget));
    }

    @Override
    @Cacheable("budgets")
    public ResponseDto get(String refNo) {
        Budget budget = budgetRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Budget not found with refNo: " + refNo));
        return ResponseDtoBuilder.getFetchResponse("Budget", refNo,
                budgetMapper.entityToRespDto(budget));
    }

    @Override
    public Optional<Budget> getEntity(String refNo) {
        return budgetRepository.findByRefNoAndDeletedFalse(refNo);
    }

    @Override
    @CacheEvict(value = "budgets", allEntries = true)
    public ResponseDto update(String refNo, BudgetUpdateDto updateDto) {
        Budget budget = budgetRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Budget not found with refNo: " + refNo));
        budgetMapper.updateEntityFromDto(updateDto, budget);
        budget = budgetRepository.save(budget);
        log.info("Updated budget with refNo: {}", refNo);
        return ResponseDtoBuilder.getUpdateResponse("Budget", refNo,
                budgetMapper.entityToRespDto(budget));
    }

    @Override
    @CacheEvict(value = "budgets", allEntries = true)
    public ResponseDto delete(String refNo) {
        Budget budget = budgetRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Budget not found with refNo: " + refNo));
        budget.setDeleted(true);
        budgetRepository.save(budget);
        log.info("Soft-deleted budget with refNo: {}", refNo);
        return ResponseDtoBuilder.getDeleteResponse("Budget", refNo);
    }

    @Override
    public ResponseDto getAllEntities(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, Budget.class)) {
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

        Page<Budget> entityPage = budgetRepository.findAllByDeletedFalse(pageRequest);
        name.expenses.globals.Page<BudgetRespDto> dtoPage = budgetMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("Budget", dtoPage);
    }

    @Override
    public Set<Budget> getEntities(Set<String> refNos) {
        return budgetRepository.findAllByRefNoIn(refNos);
    }

    @Override
    public ResponseDto getBudgetByName(String name) {
        var budgets = budgetRepository.findByNameContainingIgnoreCaseAndDeletedFalse(name);
        var dtos = budgetMapper.entitiesToRespDtos(budgets);
        return ResponseDtoBuilder.getFetchAllResponse("Budget", dtos);
    }

    @Override
    public ResponseDto getAllEntitiesWithoutAccount(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, Budget.class)) {
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

        Page<Budget> entityPage = budgetRepository.findAllByAccountIsNullAndDeletedFalse(pageRequest);
        name.expenses.globals.Page<BudgetRespDto> dtoPage = budgetMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("Budget", dtoPage);
    }

    @Override
    public Set<BudgetRespDto> entityToRespDto(Set<Budget> budgets) {
        return budgets.stream()
                .filter(b -> !b.isDeleted())
                .map(budgetMapper::entityToRespDto)
                .collect(Collectors.toSet());
    }
}
