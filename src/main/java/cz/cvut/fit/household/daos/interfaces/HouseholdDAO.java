/**
 * The HouseholdDAO interface is a Data Access Object that is used to manage Household entities in the data storage.
 * It provides methods for saving, getting, and deleting households.
 *
 * @see cz.cvut.fit.household.datamodel.entity.household.Household
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.household.Household;

import java.util.List;
import java.util.Optional;

public interface HouseholdDAO {

    /**
     * Saves the given Household entity in the data storage.
     *
     * @param household The Household object to be saved.
     * @return The saved Household object.
     */
    Household saveHousehold(Household household);

    /**
     * Retrieves a list of all Household entities in the data storage.
     *
     * @return A list of Household objects representing all households.
     */
    List<Household> findAllHouseholds();

    /**
     * Finds a Household entity by its unique id.
     *
     * @param id The unique id of the Household to be retrieved.
     * @return An Optional containing the found Household, or an empty Optional if not found.
     */
    Optional<Household> findHouseholdById(Long id);

    /**
     * Deletes a Household entity from the data storage by its unique id.
     *
     * @param id The unique id of the Household to be deleted.
     */
    void deleteHouseholdById(Long id);
}
