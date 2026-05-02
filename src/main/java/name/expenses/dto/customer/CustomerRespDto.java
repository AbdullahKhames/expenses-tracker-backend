package name.expenses.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRespDto {

    private String refNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
