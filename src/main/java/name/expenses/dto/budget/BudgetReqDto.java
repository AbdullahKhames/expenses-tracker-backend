package name.expenses.dto.budget;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.expenses.model.enums.BudgetType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetReqDto {

    @NotBlank(message = "Budget name is required")
    private String name;

    private String details;

    private Double amount;

    private BudgetType budgetType;

    private String accountRefNo;
}
