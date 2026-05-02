package name.expenses.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.expenses.model.enums.BudgetType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRespDto {

    private String name;

    private String details;

    private Double amount;

    private BudgetType budgetType;

    private boolean defaultReceiver;

    private boolean defaultSender;

    private String refNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
