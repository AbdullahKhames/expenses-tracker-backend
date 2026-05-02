package name.expenses.dto.budget_transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.expenses.model.enums.AmountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAmountReqDto {

    @NotBlank(message = "Budget reference number is required")
    private String budgetRefNo;

    @NotNull(message = "Amount is required")
    private Double amount;

    @NotNull(message = "Amount type is required")
    private AmountType amountType;
}
