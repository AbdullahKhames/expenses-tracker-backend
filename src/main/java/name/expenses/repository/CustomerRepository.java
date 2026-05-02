package name.expenses.repository;

import name.expenses.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByRefNoAndDeletedFalse(String refNo);

    @Query("SELECT c FROM Customer c WHERE c.user.email = :email AND c.deleted = false")
    Optional<Customer> findByUserEmail(@Param("email") String email);

    Page<Customer> findAllByDeletedFalse(Pageable pageable);
}
