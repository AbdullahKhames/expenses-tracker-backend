package name.expenses.mapper;

import name.expenses.dto.category.CategoryReqDto;
import name.expenses.dto.category.CategoryRespDto;
import name.expenses.dto.category.CategoryUpdateDto;
import name.expenses.globals.Page;
import name.expenses.model.Category;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "customers", ignore = true)
    Category reqDtoToEntity(CategoryReqDto dto);

    CategoryRespDto entityToRespDto(Category entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(target = "customers", ignore = true)
    void updateEntityFromDto(CategoryUpdateDto dto, @MappingTarget Category entity);

    List<CategoryRespDto> entitiesToRespDtos(List<Category> entities);
    Set<CategoryRespDto> entitiesToRespDtos(Set<Category> entities);

    default Page<CategoryRespDto> entityPageToRespDtoPage(org.springframework.data.domain.Page<Category> entityPage) {
        return Page.<CategoryRespDto>builder()
                .content(entitiesToRespDtos(entityPage.getContent()))
                .pageNumber((long) entityPage.getNumber() + 1)
                .pageSize((long) entityPage.getSize())
                .totalElements(entityPage.getTotalElements())
                .totalPages((long) entityPage.getTotalPages())
                .hasNext(entityPage.hasNext())
                .hasPrevious(entityPage.hasPrevious())
                .build();
    }
}
