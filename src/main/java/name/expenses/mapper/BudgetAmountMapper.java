package name.expenses.mapper;

import name.expenses.dto.budget_transfer.BudgetAmountReqDto;
import name.expenses.dto.budget_transfer.BudgetAmountRespDto;
import name.expenses.dto.budget_transfer.BudgetAmountUpdateDto;
import name.expenses.model.BudgetAmount;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface BudgetAmountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "budget", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "trans", ignore = true)
    BudgetAmount reqDtoToEntity(BudgetAmountReqDto dto);

    @Mapping(source = "budget.refNo", target = "budgetRefNo")
    @Mapping(source = "budget.name", target = "budgetName")
    BudgetAmountRespDto entityToRespDto(BudgetAmount entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "budget", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "trans", ignore = true)
    void updateEntityFromDto(BudgetAmountUpdateDto dto, @MappingTarget BudgetAmount entity);

    List<BudgetAmountRespDto> entitiesToRespDtos(List<BudgetAmount> entities);
    Set<BudgetAmountRespDto> entitiesToRespDtos(Set<BudgetAmount> entities);
}
