package name.expenses.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseUpdateDto {

    private String name;

    private String details;

    private Double amount;

    private String receipt;
}
