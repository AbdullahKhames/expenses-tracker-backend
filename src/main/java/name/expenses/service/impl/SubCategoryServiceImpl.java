package name.expenses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.expense.ExpenseRespDto;
import name.expenses.dto.sub_category.SubCategoryReqDto;
import name.expenses.dto.sub_category.SubCategoryRespDto;
import name.expenses.dto.sub_category.SubCategoryUpdateDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.ExpenseMapper;
import name.expenses.mapper.SubCategoryMapper;
import name.expenses.model.SubCategory;
import name.expenses.repository.SubCategoryRepository;
import name.expenses.service.SubCategoryService;
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
public class SubCategoryServiceImpl implements SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final SubCategoryMapper subCategoryMapper;
    private final ExpenseMapper expenseMapper;

    @Override
    @CacheEvict(value = "subCategories", allEntries = true)
    public ResponseDto create(SubCategoryReqDto reqDto) {
        SubCategory subCategory = subCategoryMapper.reqDtoToEntity(reqDto);
        subCategory = subCategoryRepository.save(subCategory);
        log.info("Created subCategory with refNo: {}", subCategory.getRefNo());
        return ResponseDtoBuilder.getCreateResponse("SubCategory", subCategory.getRefNo(),
                subCategoryMapper.entityToRespDto(subCategory));
    }

    @Override
    @Cacheable("subCategories")
    public ResponseDto get(String refNo) {
        SubCategory subCategory = subCategoryRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("SubCategory not found with refNo: " + refNo));
        return ResponseDtoBuilder.getFetchResponse("SubCategory", refNo,
                subCategoryMapper.entityToRespDto(subCategory));
    }

    @Override
    public Optional<SubCategory> getEntity(String refNo) {
        return subCategoryRepository.findByRefNoAndDeletedFalse(refNo);
    }

    @Override
    @CacheEvict(value = "subCategories", allEntries = true)
    public ResponseDto update(String refNo, SubCategoryUpdateDto updateDto) {
        SubCategory subCategory = subCategoryRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("SubCategory not found with refNo: " + refNo));
        subCategoryMapper.updateEntityFromDto(updateDto, subCategory);
        subCategory = subCategoryRepository.save(subCategory);
        log.info("Updated subCategory with refNo: {}", refNo);
        return ResponseDtoBuilder.getUpdateResponse("SubCategory", refNo,
                subCategoryMapper.entityToRespDto(subCategory));
    }

    @Override
    @CacheEvict(value = "subCategories", allEntries = true)
    public ResponseDto delete(String refNo) {
        SubCategory subCategory = subCategoryRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("SubCategory not found with refNo: " + refNo));
        subCategory.setDeleted(true);
        subCategoryRepository.save(subCategory);
        log.info("Soft-deleted subCategory with refNo: {}", refNo);
        return ResponseDtoBuilder.getDeleteResponse("SubCategory", refNo);
    }

    @Override
    public ResponseDto getAllEntities(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, SubCategory.class)) {
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

        Page<SubCategory> entityPage = subCategoryRepository.findAllByDeletedFalse(pageRequest);
        name.expenses.globals.Page<SubCategoryRespDto> dtoPage = subCategoryMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("SubCategory", dtoPage);
    }

    @Override
    public Set<SubCategory> getEntities(Set<String> refNos) {
        return subCategoryRepository.findAllByRefNoIn(refNos);
    }

    @Override
    public ResponseDto getSubCategoryByName(String name) {
        var subCategories = subCategoryRepository.findByNameContainingIgnoreCaseAndDeletedFalse(name);
        var dtos = subCategoryMapper.entitiesToRespDtos(subCategories);
        return ResponseDtoBuilder.getFetchAllResponse("SubCategory", dtos);
    }

    @Override
    public ResponseDto getAllEntitiesWithoutCategory(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, SubCategory.class)) {
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

        Page<SubCategory> entityPage = subCategoryRepository.findAllByCategoryIsNullAndDeletedFalse(pageRequest);
        name.expenses.globals.Page<SubCategoryRespDto> dtoPage = subCategoryMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("SubCategory", dtoPage);
    }

    @Override
    public ResponseDto getSubCategoryExpenses(String refNo) {
        SubCategory subCategory = subCategoryRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("SubCategory not found with refNo: " + refNo));
        Set<ExpenseRespDto> expenseDtos = subCategory.getExpenses().stream()
                .filter(e -> !e.isDeleted())
                .map(expenseMapper::entityToRespDto)
                .collect(Collectors.toSet());
        return ResponseDtoBuilder.getFetchResponse("SubCategory Expenses", refNo, expenseDtos);
    }
}
