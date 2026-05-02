package name.expenses.mapper;

import name.expenses.dto.budget_transfer.BudgetTransferReqDto;
import name.expenses.dto.budget_transfer.BudgetTransferRespDto;
import name.expenses.dto.budget_transfer.BudgetTransferUpdateDto;
import name.expenses.globals.Page;
import name.expenses.model.BudgetTransfer;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {BudgetAmountMapper.class})
public interface BudgetTransferMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "senderBudgetAmount", ignore = true)
    @Mapping(target = "receiverBudgetAmounts", ignore = true)
    BudgetTransfer reqDtoToEntity(BudgetTransferReqDto dto);

    BudgetTransferRespDto entityToRespDto(BudgetTransfer entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "senderBudgetAmount", ignore = true)
    @Mapping(target = "receiverBudgetAmounts", ignore = true)
    void updateEntityFromDto(BudgetTransferUpdateDto dto, @MappingTarget BudgetTransfer entity);

    List<BudgetTransferRespDto> entitiesToRespDtos(List<BudgetTransfer> entities);
    Set<BudgetTransferRespDto> entitiesToRespDtos(Set<BudgetTransfer> entities);

    default Page<BudgetTransferRespDto> entityPageToRespDtoPage(org.springframework.data.domain.Page<BudgetTransfer> entityPage) {
        return Page.<BudgetTransferRespDto>builder()
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
