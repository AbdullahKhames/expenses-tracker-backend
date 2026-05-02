package name.expenses.dto.association;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssociationReqDto {

    @NotEmpty(message = "Association reference numbers are required")
    private Set<String> associationRefNos;
}
