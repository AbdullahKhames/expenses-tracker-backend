package name.expenses.repository;

import name.expenses.model.BudgetTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface BudgetTransferRepository extends JpaRepository<BudgetTransfer, Long> {
    Optional<BudgetTransfer> findByRefNoAndDeletedFalse(String refNo);
    Page<BudgetTransfer> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT bt FROM BudgetTransfer bt WHERE bt.refNo IN :refNos AND bt.deleted = false")
    Set<BudgetTransfer> findAllByRefNoIn(@Param("refNos") Set<String> refNos);
}
