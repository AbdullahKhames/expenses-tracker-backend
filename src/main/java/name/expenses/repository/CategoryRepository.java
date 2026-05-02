package name.expenses.repository;

import name.expenses.model.Category;
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
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByRefNoAndDeletedFalse(String refNo);
    List<Category> findByNameContainingIgnoreCaseAndDeletedFalse(String name);
    Page<Category> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.refNo IN :refNos AND c.deleted = false")
    Set<Category> findAllByRefNoIn(@Param("refNos") Set<String> refNos);
}
