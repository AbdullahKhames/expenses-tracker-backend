package name.expenses.dto.sub_category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubCategoryReqDto {

    @NotBlank(message = "Sub-category name is required")
    private String name;

    private String details;
}
