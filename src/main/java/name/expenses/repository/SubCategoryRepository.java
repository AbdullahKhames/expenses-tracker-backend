package name.expenses.repository;

import name.expenses.model.SubCategory;
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
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    Optional<SubCategory> findByRefNoAndDeletedFalse(String refNo);
    List<SubCategory> findByNameContainingIgnoreCaseAndDeletedFalse(String name);
    Page<SubCategory> findAllByDeletedFalse(Pageable pageable);

    @Query("SELECT sc FROM SubCategory sc WHERE sc.category IS NULL AND sc.deleted = false")
    Page<SubCategory> findAllByCategoryIsNullAndDeletedFalse(Pageable pageable);

    @Query("SELECT sc FROM SubCategory sc WHERE sc.refNo IN :refNos AND sc.deleted = false")
    Set<SubCategory> findAllByRefNoIn(@Param("refNos") Set<String> refNos);

    Optional<SubCategory> getByRefNoAndDeletedFalse(String subCategoryRef);
}
