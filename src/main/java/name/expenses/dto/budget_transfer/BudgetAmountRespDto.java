package name.expenses.dto.budget_transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.expenses.model.enums.AmountType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAmountRespDto {

    private String budgetRefNo;

    private String budgetName;

    private Double amount;

    private AmountType amountType;

    private String refNo;
}
