package name.expenses.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.expenses.dto.category.CategoryReqDto;
import name.expenses.dto.category.CategoryRespDto;
import name.expenses.dto.category.CategoryUpdateDto;
import name.expenses.dto.sub_category.SubCategoryRespDto;
import name.expenses.error.ObjectNotFoundException;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.mapper.CategoryMapper;
import name.expenses.mapper.SubCategoryMapper;
import name.expenses.model.Category;
import name.expenses.model.SubCategory;
import name.expenses.repository.CategoryRepository;
import name.expenses.repository.SubCategoryRepository;
import name.expenses.service.CategoryService;
import name.expenses.utils.FieldValidator;
import name.expenses.utils.ResponseDtoBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final SubCategoryRepository subCategoryRepository;
    private final SubCategoryMapper subCategoryMapper;

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public ResponseDto create(CategoryReqDto reqDto) {
        Category category = categoryMapper.reqDtoToEntity(reqDto);
        category = categoryRepository.save(category);
        log.info("Created category with refNo: {}", category.getRefNo());
        return ResponseDtoBuilder.getCreateResponse("Category", category.getRefNo(),
                categoryMapper.entityToRespDto(category));
    }

    @Override
    @Cacheable("categories")
    public ResponseDto get(String refNo) {
        Category category = categoryRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found with refNo: " + refNo));
        return ResponseDtoBuilder.getFetchResponse("Category", refNo,
                categoryMapper.entityToRespDto(category));
    }

    @Override
    public Optional<Category> getEntity(String refNo) {
        return categoryRepository.findByRefNoAndDeletedFalse(refNo);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public ResponseDto update(String refNo, CategoryUpdateDto updateDto) {
        Category category = categoryRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found with refNo: " + refNo));
        categoryMapper.updateEntityFromDto(updateDto, category);
        category = categoryRepository.save(category);
        log.info("Updated category with refNo: {}", refNo);
        return ResponseDtoBuilder.getUpdateResponse("Category", refNo,
                categoryMapper.entityToRespDto(category));
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public ResponseDto delete(String refNo) {
        Category category = categoryRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found with refNo: " + refNo));
        category.setDeleted(true);
        categoryRepository.save(category);
        log.info("Soft-deleted category with refNo: {}", refNo);
        return ResponseDtoBuilder.getDeleteResponse("Category", refNo);
    }

    @Override
    public ResponseDto getAllEntities(Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        if (!FieldValidator.hasField(sortBy, Category.class)) {
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

        Page<Category> entityPage = categoryRepository.findAllByDeletedFalse(pageRequest);
        name.expenses.globals.Page<CategoryRespDto> dtoPage = categoryMapper.entityPageToRespDtoPage(entityPage);

        return ResponseDtoBuilder.getFetchAllResponse("Category", dtoPage);
    }

    @Override
    public Set<Category> getEntities(Set<String> refNos) {
        return categoryRepository.findAllByRefNoIn(refNos);
    }

    @Override
    public ResponseDto getCategoryByName(String name) {
        var categories = categoryRepository.findByNameContainingIgnoreCaseAndDeletedFalse(name);
        var dtos = categoryMapper.entitiesToRespDtos(categories);
        return ResponseDtoBuilder.getFetchAllResponse("Category", dtos);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public ResponseDto addAssociation(String categoryRefNo, String subCategoryRefNo) {
        Category category = categoryRepository.findByRefNoAndDeletedFalse(categoryRefNo)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found with refNo: " + categoryRefNo));
        SubCategory subCategory = subCategoryRepository.findByRefNoAndDeletedFalse(subCategoryRefNo)
                .orElseThrow(() -> new ObjectNotFoundException("SubCategory not found with refNo: " + subCategoryRefNo));
        category.getSubCategories().add(subCategory);
        categoryRepository.save(category);
        log.info("Added subCategory {} to category {}", subCategoryRefNo, categoryRefNo);
        return ResponseDtoBuilder.getUpdateResponse("Category", categoryRefNo,
                categoryMapper.entityToRespDto(category));
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public ResponseDto removeAssociation(String categoryRefNo, String subCategoryRefNo) {
        Category category = categoryRepository.findByRefNoAndDeletedFalse(categoryRefNo)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found with refNo: " + categoryRefNo));
        SubCategory subCategory = subCategoryRepository.findByRefNoAndDeletedFalse(subCategoryRefNo)
                .orElseThrow(() -> new ObjectNotFoundException("SubCategory not found with refNo: " + subCategoryRefNo));
        category.getSubCategories().remove(subCategory);
        categoryRepository.save(category);
        log.info("Removed subCategory {} from category {}", subCategoryRefNo, categoryRefNo);
        return ResponseDtoBuilder.getUpdateResponse("Category", categoryRefNo,
                categoryMapper.entityToRespDto(category));
    }

    @Override
    public ResponseDto getSubcategories(String refNo, Long pageNumber, Long pageSize, String sortBy, SortDirection sortDirection) {
        Category category = categoryRepository.findByRefNoAndDeletedFalse(refNo)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found with refNo: " + refNo));

        List<SubCategory> subCategories = category.getSubCategories().stream()
                .filter(sc -> !sc.isDeleted())
                .collect(Collectors.toList());

        // Sort
        if (!FieldValidator.hasField(sortBy, SubCategory.class)) {
            sortBy = "id";
        }
        String finalSortBy = sortBy;
        @SuppressWarnings({"unchecked", "rawtypes"})
        Comparator<SubCategory> comparator = (sc1, sc2) -> {
            try {
                var field1 = getFieldFromHierarchy(sc1.getClass(), finalSortBy);
                field1.setAccessible(true);
                Object val1 = field1.get(sc1);

                var field2 = getFieldFromHierarchy(sc2.getClass(), finalSortBy);
                field2.setAccessible(true);
                Object val2 = field2.get(sc2);

                if (val1 instanceof Comparable && val2 instanceof Comparable) {
                    return ((Comparable) val1).compareTo(val2);
                }
                return 0;
            } catch (Exception e) {
                return 0;
            }
        };
        if (sortDirection == SortDirection.DESC) {
            comparator = comparator.reversed();
        }
        subCategories.sort(comparator);

        // Paginate
        int start = (int) ((pageNumber - 1) * pageSize);
        int end = Math.min(start + pageSize.intValue(), subCategories.size());
        List<SubCategory> pageContent = start < subCategories.size()
                ? subCategories.subList(start, end)
                : Collections.emptyList();

        List<SubCategoryRespDto> dtos = subCategoryMapper.entitiesToRespDtos(pageContent);
        long totalElements = subCategories.size();
        long totalPages = (long) Math.ceil((double) totalElements / pageSize);

        name.expenses.globals.Page<SubCategoryRespDto> dtoPage = name.expenses.globals.Page.<SubCategoryRespDto>builder()
                .content(dtos)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(pageNumber < totalPages)
                .hasPrevious(pageNumber > 1)
                .build();

        return ResponseDtoBuilder.getFetchAllResponse("SubCategory", dtoPage);
    }

    private java.lang.reflect.Field getFieldFromHierarchy(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
