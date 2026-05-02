package name.expenses.mapper;

import name.expenses.dto.sub_category.SubCategoryReqDto;
import name.expenses.dto.sub_category.SubCategoryRespDto;
import name.expenses.dto.sub_category.SubCategoryUpdateDto;
import name.expenses.globals.Page;
import name.expenses.model.SubCategory;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface SubCategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "customers", ignore = true)
    @Mapping(target = "category", ignore = true)
    SubCategory reqDtoToEntity(SubCategoryReqDto dto);

    SubCategoryRespDto entityToRespDto(SubCategory entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "customers", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromDto(SubCategoryUpdateDto dto, @MappingTarget SubCategory entity);

    List<SubCategoryRespDto> entitiesToRespDtos(List<SubCategory> entities);
    Set<SubCategoryRespDto> entitiesToRespDtos(Set<SubCategory> entities);

    default Page<SubCategoryRespDto> entityPageToRespDtoPage(org.springframework.data.domain.Page<SubCategory> entityPage) {
        return Page.<SubCategoryRespDto>builder()
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
