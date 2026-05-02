package name.expenses.repository;

import name.expenses.model.Budget;
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
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByRefNoAndDeletedFalse(String refNo);
    List<Budget> findByNameContainingIgnoreCaseAndDeletedFalse(String name);
    Page<Budget> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT b FROM Budget b WHERE b.account IS NULL AND b.deleted = false")
    Page<Budget> findAllByAccountIsNullAndDeletedFalse(Pageable pageable);

    @Query("SELECT b FROM Budget b WHERE b.refNo IN :refNos AND b.deleted = false")
    Set<Budget> findAllByRefNoIn(@Param("refNos") Set<String> refNos);
}
