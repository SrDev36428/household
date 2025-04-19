package cz.cvut.fit.household.repository;

import cz.cvut.fit.household.datamodel.entity.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(value = "WITH RECURSIVE LocationHierarchy AS (" +
            " SELECT id FROM Location WHERE house_hold_id = :householdId " +
            " UNION ALL " +
            " SELECT l.id FROM Location l JOIN LocationHierarchy lh ON l.main_id = lh.id " +
            ") " +
            "SELECT i.* FROM Item i WHERE i.location_id IN (SELECT id FROM LocationHierarchy)",
            nativeQuery = true)
    List<Item> findAllItemsFromHousehold(@Param("householdId") Long householdId);


}
