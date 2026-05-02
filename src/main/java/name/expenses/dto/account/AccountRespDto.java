package name.expenses.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.expenses.dto.budget.BudgetRespDto;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRespDto {

    private String name;

    private String details;

    private String refNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<BudgetRespDto> budgets;
}
