package name.expenses.repository;

import name.expenses.model.Account;
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
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByRefNoAndDeletedFalse(String refNo);
    List<Account> findByNameContainingIgnoreCaseAndDeletedFalse(String name);
    Page<Account> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.refNo IN :refNos AND a.deleted = false")
    Set<Account> findAllByRefNoIn(@Param("refNos") Set<String> refNos);

    @Query("SELECT a FROM Account a WHERE a.name = 'default account' AND a.deleted = false")
    Optional<Account> findDefaultAccount();
}
