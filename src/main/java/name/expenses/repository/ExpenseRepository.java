package name.expenses.repository;

import name.expenses.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Optional<Expense> findByRefNoAndDeletedFalse(String refNo);
    List<Expense> findByNameContainingIgnoreCaseAndDeletedFalse(String name);
    Page<Expense> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.subCategory IS NULL AND e.deleted = false")
    Page<Expense> findAllBySubCategoryIsNullAndDeletedFalse(Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.refNo IN :refNos AND e.deleted = false")
    Set<Expense> findAllByRefNoIn(@Param("refNos") Set<String> refNos);
}
