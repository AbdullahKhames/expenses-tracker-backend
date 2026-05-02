package name.expenses.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUpdateDto {

    private Set<String> accountRefNos;

    private Set<String> categoryRefNos;

    private Set<String> subCategoryRefNos;
}
