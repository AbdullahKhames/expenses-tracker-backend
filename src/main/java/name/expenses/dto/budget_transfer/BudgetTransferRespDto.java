package name.expenses.dto.budget_transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetTransferRespDto {

    private String name;

    private String details;

    private Double amount;

    private boolean lending;

    private BudgetAmountRespDto senderBudgetAmount;

    private Set<BudgetAmountRespDto> receiverBudgetAmounts;

    private String refNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
