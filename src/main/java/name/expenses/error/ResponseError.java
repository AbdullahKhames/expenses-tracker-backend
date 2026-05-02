package name.expenses.error;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseError {
    private ErrorCategory errorCategory;
    private String errorCode;
    private String errorMessage;
}
