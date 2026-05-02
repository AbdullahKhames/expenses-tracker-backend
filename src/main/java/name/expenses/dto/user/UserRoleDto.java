package name.expenses.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleDto {
    @NotBlank
    private String email;
    @NotBlank
    private String roleName;
}
