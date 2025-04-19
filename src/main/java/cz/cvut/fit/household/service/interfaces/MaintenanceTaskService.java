package cz.cvut.fit.household.service.interfaces;

import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;

import java.util.List;
import java.util.Optional;

public interface MaintenanceTaskService {

    /**
     * Adds a new MaintenanceTask associated with the provided Maintenance.
     *
     * @param maintenanceTask The Maintenance to which the new MaintenanceTask belongs.
     * @return The created MaintenanceTask.
     */
    MaintenanceTask addMaintenanceTask(MaintenanceTask maintenanceTask);

    /**
     * Updates a MaintenanceTask with the provided data.
     *
     * @param id          The unique id of the MaintenanceTask to be updated.
     * @param maintenanceTask    The updated data for the MaintenanceTask.
     * @return The updated MaintenanceTask.
     */
    MaintenanceTask updateMaintenanceTask(Long id , MaintenanceTask maintenanceTask);

    /**
     * Deletes a MaintenanceTask by its unique id.
     *
     * @param id The unique id of the MaintenanceTask to be deleted.
     */
    void deleteMaintenanceTask(Long id);

    MaintenanceTask openMaintenanceTask(Long id);

    /**
     * Closes a MaintenanceTask.
     *
     * @param id The unique id of the MaintenanceTask to be closed.
     */
    MaintenanceTask closeMaintenanceTask(Long id);

    /**
     * Finds a MaintenanceTask by its unique id.
     *
     * @param id The unique id of the MaintenanceTask to be retrieved.
     * @return An Optional containing the found MaintenanceTask, or an empty Optional if not found.
     */
    Optional<MaintenanceTask> findMaintenanceTaskByID(Long id);

    /**
     * Finds all MaintenanceTask that have the specified assignee.
     *
     * @param id The unique id of the Assignee for which the tasks need to be found.
     * @return A list of MaintenanceTask of the Assignee specified by id
     */
    List<MaintenanceTask>findMaintenanceTasksByAssignee(Long id);
}
