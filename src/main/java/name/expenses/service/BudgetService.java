package name.expenses.service;

import name.expenses.dto.budget.BudgetReqDto;
import name.expenses.dto.budget.BudgetRespDto;
import name.expenses.dto.budget.BudgetUpdateDto;
import name.expenses.globals.SortDirection;
import name.expenses.globals.responses.ResponseDto;
import name.expenses.model.Budget;

import java.util.Set;

public interface BudgetService extends CrudService<BudgetReqDto, BudgetUpdateDto, Budget> {
    ResponseDto getBudgetByName(String name);
    ResponseDto getAllEntitiesWithoutAccount(Long page, Long perPage, String sortBy, SortDirection dir);
    Set<BudgetRespDto> entityToRespDto(Set<Budget> budgets);
}
