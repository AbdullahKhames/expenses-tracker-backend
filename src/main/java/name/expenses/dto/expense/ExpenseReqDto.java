package name.expenses.dto.expense;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseReqDto {

    @NotBlank(message = "Expense name is required")
    private String name;

    private String details;

    private double amount;

    private String receipt;
}
