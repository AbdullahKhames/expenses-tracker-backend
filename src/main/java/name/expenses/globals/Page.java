package name.expenses.globals;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page<T> {
    private List<T> content;
    private Long pageNumber;
    private Long pageSize;
    private Long totalElements;
    private Long totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}
