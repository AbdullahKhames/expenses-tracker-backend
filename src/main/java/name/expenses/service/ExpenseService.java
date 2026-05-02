package name.expenses.service;

import name.expenses.dto.expense.ExpenseReqDto;
import name.expenses.dto.expense.ExpenseUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.model.Expense;

public interface ExpenseService extends CrudService<ExpenseReqDto, ExpenseUpdateDto, Expense> {
    ResponseDto getExpenseByName(String name);
    ResponseDto getAllEntitiesWithoutSubCategory(Long page, Long perPage, String sortBy, SortDirection dir);
}
