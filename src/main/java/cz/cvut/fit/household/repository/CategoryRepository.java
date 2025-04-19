package cz.cvut.fit.household.repository;

import cz.cvut.fit.household.datamodel.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query(value = "SELECT c FROM Category c WHERE c.id = :id")
    Category getCategoryById(@Param("id")Long id);
}
