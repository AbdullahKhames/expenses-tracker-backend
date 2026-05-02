package name.expenses.mapper;

import name.expenses.dto.customer.CustomerReqDto;
import name.expenses.dto.customer.CustomerRespDto;
import name.expenses.dto.customer.CustomerUpdateDto;
import name.expenses.globals.Page;
import name.expenses.model.Customer;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "budgets", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "budgetTransfers", ignore = true)
    Customer reqDtoToEntity(CustomerReqDto dto);

    CustomerRespDto entityToRespDto(Customer entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refNo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "budgets", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "budgetTransfers", ignore = true)
    void updateEntityFromDto(CustomerUpdateDto dto, @MappingTarget Customer entity);

    List<CustomerRespDto> entitiesToRespDtos(List<Customer> entities);
    Set<CustomerRespDto> entitiesToRespDtos(Set<Customer> entities);

    default Page<CustomerRespDto> entityPageToRespDtoPage(org.springframework.data.domain.Page<Customer> entityPage) {
        return Page.<CustomerRespDto>builder()
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
