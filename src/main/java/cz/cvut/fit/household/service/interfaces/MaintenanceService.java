/**
 * The MaintenanceService interface provides methods to manage Maintenance entities in the application.
 * It includes operations for adding, updating, getting, and deleting maintenance tasks, as well as
 * stopping the generation of maintenance tasks, changing their state, and retrieving all maintenance tasks.
 *
 * @see cz.cvut.fit.household.datamodel.entity.household.Household
 * @see cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance
 * @see cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceCreationDTO
 * @see cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceStateDTO
 * @see cz.cvut.fit.household.datamodel.entity.Membership
 */
package cz.cvut.fit.household.service.interfaces;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceCreationDTO;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceStateDTO;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;

import java.util.List;
import java.util.Optional;

public interface MaintenanceService {

    /**
     * Adds a new Maintenance task to the specified Household, assigning it to the given members.
     *
     * @param updatedMaintenance The data needed to create the new Maintenance.
     * @param household          The Household to which the new Maintenance belongs.
     * @param assignee           The member assigned to the Maintenance.
     * @return The created Maintenance.
     */
    Maintenance addMaintenance(MaintenanceCreationDTO updatedMaintenance, Household household, Membership creator, Membership assignee);

    /**
     * Deletes a Maintenance by its unique id.
     *
     * @param maintenanceId The unique id of the Maintenance to be deleted.
     */
    void deleteMaintenance(Long maintenanceId);

    /**
     * Stops the generation of a recurring Maintenance.
     *
     * @param maintenance The Maintenance to stop generating.
     * @return The updated Maintenance.
     */
    Maintenance stopGeneratingMaintenance(Maintenance maintenance);

    /**
     * Changes the state of a Maintenance.
     *
     * @param updatedMaintenanceStateDTO The data needed to update the state of the Maintenance.
     * @param maintenanceId              The unique id of the Maintenance to be updated.
     * @return The updated Maintenance.
     */
    Maintenance changeState(MaintenanceStateDTO updatedMaintenanceStateDTO, Long maintenanceId);

    /**
     * Finds a Maintenance by its unique id.
     *
     * @param id The unique id of the Maintenance to be retrieved.
     * @return An Optional containing the found Maintenance, or an empty Optional if not found.
     */
    Optional<Maintenance> findMaintenanceById(Long id);

    /**
     * Retrieves a list of all Maintenance in the application.
     *
     * @return A list of all Maintenance objects.
     */
    List<Maintenance> getAll();

    /**
     * Updates a Maintenance with the provided data.
     *
     * @param maintenanceId      The unique id of the Maintenance to be updated.
     * @param updatedMaintenance The updated data for the Maintenance.
     * @param houseHold          The Household to which the updated Maintenance belongs.
     * @param creator           The member reporting the updated Maintenance.
     * @param assignee           The member assigned to the updated Maintenance.
     * @return The updated Maintenance.
     */
    Maintenance updateMaintenance(Long maintenanceId, MaintenanceCreationDTO updatedMaintenance, Household houseHold, Membership creator, Membership assignee);

    /**
     * Adds a Recurring pattern to a specified maintenance.
     *
     * @param maintenanceId      The unique id of the Maintenance that the pattern is added to.
     * @param recurringPattern   The recurring pattern to be added.
     * @return The Maintenance with added recurring pattern.
     */
    Maintenance addRecurringPattern(Long maintenanceId ,  RecurringPattern recurringPattern);

}
