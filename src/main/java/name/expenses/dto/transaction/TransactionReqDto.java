package name.expenses.dto.transaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.expenses.dto.budget_transfer.BudgetAmountReqDto;
import name.expenses.dto.expense.ExpenseReqDto;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionReqDto {

    @NotBlank(message = "Transaction name is required")
    private String name;

    private String details;

    @NotEmpty(message = "Budget amounts are required")
    @Valid
    private Set<BudgetAmountReqDto> budgetAmounts;

    @NotNull(message = "Expense is required")
    @Valid
    private ExpenseReqDto expense;
}
