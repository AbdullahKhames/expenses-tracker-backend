package name.expenses.repository;

import name.expenses.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByRefNoAndDeletedFalse(String refNo);
    Page<Transaction> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.refNo IN :refNos AND t.deleted = false")
    Set<Transaction> findAllByRefNoIn(@Param("refNos") Set<String> refNos);
}
