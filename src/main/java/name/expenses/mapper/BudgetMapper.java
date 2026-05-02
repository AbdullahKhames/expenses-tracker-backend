package name.expenses.mapper;

import name.expenses.dto.budget.BudgetReqDto;
import name.expenses.dto.budget.BudgetRespDto;
import name.expenses.dto.budget.BudgetUpdateDto;
import name.expenses.globals.Page;
import name.expenses.model.Budget;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "account", ignore = true)
    Budget reqDtoToEntity(BudgetReqDto dto);

    BudgetRespDto entityToRespDto(Budget entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "account", ignore = true)
    void updateEntityFromDto(BudgetUpdateDto dto, @MappingTarget Budget entity);

    List<BudgetRespDto> entitiesToRespDtos(List<Budget> entities);
    Set<BudgetRespDto> entitiesToRespDtos(Set<Budget> entities);

    default Page<BudgetRespDto> entityPageToRespDtoPage(org.springframework.data.domain.Page<Budget> entityPage) {
        return Page.<BudgetRespDto>builder()
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
