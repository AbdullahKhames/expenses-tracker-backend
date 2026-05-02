package name.expenses.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRespDto {

    private String name;

    private String details;

    private double amount;

    private String receipt;

    private String refNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
