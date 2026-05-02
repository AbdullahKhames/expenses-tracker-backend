package name.expenses.dto.budget_transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetTransferUpdateDto {

    private String name;

    private String details;

    private Double amount;

    private Boolean lending;

    private BudgetAmountUpdateDto senderBudgetAmount;

    private Set<BudgetAmountUpdateDto> receiverBudgetAmounts;
}
