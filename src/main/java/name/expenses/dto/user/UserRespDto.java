package name.expenses.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRespDto {

    private String fullName;

    private String email;

    private int age;

    private boolean verified;

    private boolean loggedIn;

    private String refNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<String> roles;
}
