package name.expenses.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.expenses.dto.budget_transfer.BudgetAmountUpdateDto;
import name.expenses.dto.expense.ExpenseUpdateDto;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionUpdateDto {

    private String name;

    private String details;

    private Set<BudgetAmountUpdateDto> budgetAmounts;

    private ExpenseUpdateDto expense;
}
