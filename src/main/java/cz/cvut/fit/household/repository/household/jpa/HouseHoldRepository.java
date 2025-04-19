package cz.cvut.fit.household.repository.household.jpa;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.repository.household.AbstractHouseHoldRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for management of household entities in the database.
 */
@Repository
public interface HouseHoldRepository extends JpaRepository<Household, Long>, AbstractHouseHoldRepository {
}
