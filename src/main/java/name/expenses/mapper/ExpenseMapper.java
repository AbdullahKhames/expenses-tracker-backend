package name.expenses.mapper;

import name.expenses.dto.expense.ExpenseReqDto;
import name.expenses.dto.expense.ExpenseRespDto;
import name.expenses.dto.expense.ExpenseUpdateDto;
import name.expenses.globals.Page;
import name.expenses.model.Expense;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "subCategory", ignore = true)
    Expense reqDtoToEntity(ExpenseReqDto dto);

    ExpenseRespDto entityToRespDto(Expense entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "subCategory", ignore = true)
    void updateEntityFromDto(ExpenseUpdateDto dto, @MappingTarget Expense entity);

    List<ExpenseRespDto> entitiesToRespDtos(List<Expense> entities);
    Set<ExpenseRespDto> entitiesToRespDtos(Set<Expense> entities);

    default Page<ExpenseRespDto> entityPageToRespDtoPage(org.springframework.data.domain.Page<Expense> entityPage) {
        return Page.<ExpenseRespDto>builder()
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
