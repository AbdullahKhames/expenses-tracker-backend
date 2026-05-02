package name.expenses.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.expenses.model.enums.BudgetType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetUpdateDto {

    private String name;

    private String details;

    private Double amount;

    private BudgetType budgetType;
}
