/**
 * The MaintenanceTaskDAO interface is a Data Access Object that is used to manage MaintenanceTask entities in the data storage.
 * It provides methods for saving, deleting, and retrieving maintenance tasks.
 *
 * @see cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;

import java.util.List;
import java.util.Optional;

public interface MaintenanceTaskDAO {

    /**
     * Saves the given MaintenanceTask entity in the data storage.
     *
     * @param maintenanceTask The MaintenanceTask object to be saved.
     * @return The saved MaintenanceTask object.
     */
    MaintenanceTask saveMaintenanceTask(MaintenanceTask maintenanceTask);

    /**
     * Finds a MaintenanceTask entity by its unique id.
     *
     * @param id The unique id of the MaintenanceTask to be retrieved.
     * @return An Optional containing the found MaintenanceTask, or an empty Optional if not found.
     */
    Optional<MaintenanceTask> findMaintenanceTaskById(Long id);

    /**
     * Deletes a MaintenanceTask entity from the data storage by its unique id.
     *
     * @param maintenanceTaskId The unique id of the MaintenanceTask to be deleted.
     */
    void deleteMaintenanceTaskById(Long maintenanceTaskId);

    /**
     * Retrieves a list of all MaintenanceTask entities in the data storage.
     *
     * @return A list of MaintenanceTask objects representing all maintenance tasks.
     */
    List<MaintenanceTask> findAllMaintenanceTasks();

    /**
     * Finds all MaintenanceTask entities by its the user id of the user they are assigned to.
     *
     * @param userId The unique id of the User to whom the maintenance task was assigned to.
     * @return A list of MaintenanceTask objects representing all maintenance tasks assigned to the specified user.
     */
    List<MaintenanceTask> findAllMaintenanceTasksByAssigneeId(Long userId);
}
