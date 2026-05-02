package name.expenses.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.expenses.dto.budget_transfer.BudgetAmountRespDto;
import name.expenses.dto.expense.ExpenseRespDto;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRespDto {

    private String name;

    private String details;

    private Double amount;

    private Set<BudgetAmountRespDto> budgetAmounts;

    private ExpenseRespDto expense;

    private String refNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
