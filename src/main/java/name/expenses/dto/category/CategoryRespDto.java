package name.expenses.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRespDto {

    private String name;

    private String details;

    private String refNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
