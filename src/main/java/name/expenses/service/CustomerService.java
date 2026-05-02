package name.expenses.service;

import name.expenses.dto.customer.CustomerReqDto;
import name.expenses.dto.customer.CustomerUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.model.Customer;
import name.expenses.model.enums.EntityType;

public interface CustomerService extends CrudService<CustomerReqDto, CustomerUpdateDto, Customer> {

    ResponseDto getAllCustomerAccounts(Long page, Long perPage, String sortBy, SortDirection dir);

    ResponseDto getAllCustomerAccountBudgets(String accountRef, Long page, Long perPage, String sortBy, SortDirection dir);

    ResponseDto getAllCustomerSubCategoryExpenses(String subCategoryRef, Long page, Long perPage, String sortBy, SortDirection dir);

    ResponseDto getAllCustomerBudgets(Long page, Long perPage, String sortBy, SortDirection dir);

    ResponseDto getAllCustomerExpenses(Long page, Long perPage, String sortBy, SortDirection dir);

    ResponseDto getAllCustomerTransactions(Long page, Long perPage, String sortBy, SortDirection dir);

    ResponseDto getAllCustomerBudgetTransfers(Long page, Long perPage, String sortBy, SortDirection dir);

    ResponseDto getCustomerAssociation(EntityType entityType);
}
