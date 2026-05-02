package name.expenses.mapper;

import name.expenses.dto.transaction.TransactionReqDto;
import name.expenses.dto.transaction.TransactionRespDto;
import name.expenses.dto.transaction.TransactionUpdateDto;
import name.expenses.globals.Page;
import name.expenses.model.Transaction;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {BudgetAmountMapper.class, ExpenseMapper.class})
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "budgetAmounts", ignore = true)
    @Mapping(target = "expense", ignore = true)
    @Mapping(target = "amount", ignore = true)
    Transaction reqDtoToEntity(TransactionReqDto dto);

    TransactionRespDto entityToRespDto(Transaction entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "budgetAmounts", ignore = true)
    @Mapping(target = "expense", ignore = true)
    @Mapping(target = "amount", ignore = true)
    void updateEntityFromDto(TransactionUpdateDto dto, @MappingTarget Transaction entity);

    List<TransactionRespDto> entitiesToRespDtos(List<Transaction> entities);
    Set<TransactionRespDto> entitiesToRespDtos(Set<Transaction> entities);

    default Page<TransactionRespDto> entityPageToRespDtoPage(org.springframework.data.domain.Page<Transaction> entityPage) {
        return Page.<TransactionRespDto>builder()
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
