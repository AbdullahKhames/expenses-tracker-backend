package name.expenses.mapper;

import name.expenses.dto.account.AccountReqDto;
import name.expenses.dto.account.AccountRespDto;
import name.expenses.dto.account.AccountUpdateDto;
import name.expenses.globals.Page;
import name.expenses.model.Account;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {BudgetMapper.class})
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "budgets", ignore = true)
    @Mapping(target = "customers", ignore = true)
    Account reqDtoToEntity(AccountReqDto dto);

    AccountRespDto entityToRespDto(Account entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "budgets", ignore = true)
    @Mapping(target = "customers", ignore = true)
    void updateEntityFromDto(AccountUpdateDto dto, @MappingTarget Account entity);

    List<AccountRespDto> entitiesToRespDtos(List<Account> entities);
    Set<AccountRespDto> entitiesToRespDtos(Set<Account> entities);

    default Page<AccountRespDto> entityPageToRespDtoPage(org.springframework.data.domain.Page<Account> entityPage) {
        return Page.<AccountRespDto>builder()
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
