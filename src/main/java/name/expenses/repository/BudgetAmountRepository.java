package name.expenses.repository;

import name.expenses.model.BudgetAmount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetAmountRepository extends JpaRepository<BudgetAmount, Long> {
}
