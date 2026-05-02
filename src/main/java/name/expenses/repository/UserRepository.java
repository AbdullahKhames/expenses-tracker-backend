package name.expenses.repository;

import name.expenses.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRefNoAndDeletedFalse(String refNo);
    Page<User> findAllByDeletedFalse(Pageable pageable);
}
