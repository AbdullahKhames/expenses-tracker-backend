package name.expenses.dto.budget_transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetTransferReqDto {

    @NotBlank(message = "Budget transfer name is required")
    private String name;

    private String details;

    @NotNull(message = "Amount is required")
    private Double amount;

    private boolean lending;

    @NotNull(message = "Sender budget amount is required")
    @Valid
    private BudgetAmountReqDto senderBudgetAmount;

    @NotEmpty(message = "Receiver budget amounts are required")
    @Valid
    private Set<BudgetAmountReqDto> receiverBudgetAmounts;
}
