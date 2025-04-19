/**
 * The MaintenanceDAO interface is a Data Access Object that is used to manage Maintenance entities in the data storage.
 * It provides methods for saving, deleting, and retrieving maintenance records.
 *
 * @see cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;

import java.util.List;
import java.util.Optional;

public interface MaintenanceDAO {

    /**
     * Saves the given Maintenance entity in the data storage.
     *
     * @param maintenance The Maintenance object to be saved.
     * @return The saved Maintenance object.
     */
    Maintenance saveMaintenance(Maintenance maintenance);

    /**
     * Deletes a Maintenance entity from the data storage by its unique id.
     *
     * @param id The unique id of the Maintenance to be deleted.
     */
    void deleteMaintenanceById(Long id);

    /**
     * Finds a Maintenance entity by its unique id.
     *
     * @param id The unique id of the Maintenance to be retrieved.
     * @return An Optional containing the found Maintenance, or an empty Optional if not found.
     */
    Optional<Maintenance> findMaintenanceById(Long id);

    /**
     * Retrieves a list of all Maintenance entities in the data storage.
     *
     * @return A list of Maintenance objects representing all maintenance records.
     */
    List<Maintenance> getAllMaintenances();
}
