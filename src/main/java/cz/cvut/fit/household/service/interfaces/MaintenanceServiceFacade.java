package cz.cvut.fit.household.service.interfaces;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceCreationDTO;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceStateDTO;

import java.util.List;

public interface MaintenanceServiceFacade {

    /**
     * Hides the implementation of retrieving a household by specific id.
     *
     * @param id id of the household to retrieve.
     * @param exceptionMsg Message to be passed in case of thrown exception.
     *
     * @return a household, if household with given id is present, otherwise throw an exception.
     * @see Household
     */
    Household getHouseHoldById(Long id, String exceptionMsg);

    /**
     * Hides the implementation of checking if the household has an owner
     *
     * @param houseHold household to check
     * @return boolean true if household has owner, false if it does not
     * @see Household
     */
    boolean isHouseholdOwner(Household houseHold);

    /**
     * Hides the implementation of getting a list of active memberships
     *
     * @return a list of memberships that have their MembershipStatus set to ACTIVE
     * @see Membership
     */
    List<Membership> getMembershipList();

    /**
     * Hides the implementation of getting a list of active memberships for a specific household
     *
     * @param household household for which get the list of memberships
     * @return a list of memberships belonging to specific household that have their MembershipStatus set to ACTIVE
     * @see Membership
     */
    List<Membership> getMembershipList(Household household);

    /**
     * Hides the implementation of retrieving a specific maintenance by its id
     *
     * @param id  id of maintenance to be retrieved
     * @param exceptionMsg Message to be passed in case of thrown exception.
     * @return maintenance, if the maintenance with given id is present, otherwise throw an exception.
     * @see Maintenance
     */
    Maintenance getMaintenanceById(Long id, String exceptionMsg);

    /**
     * Hides the implementation of adding a new maintenance
     *
     * @param updatedMaintenance DTO of new maintenance to add
     * @param houseHold household to which the maintenance will be added
     * @return newly added maintenance, if the operation was successful
     * @see Maintenance
     * @see MaintenanceCreationDTO
     */
    Maintenance addMaintenance(MaintenanceCreationDTO updatedMaintenance, Household houseHold);

    /**
     * Hides the implementation of updating the maintenance specified by id
     *
     * @param maintenanceId id of maintenance to update
     * @param updatedMaintenance DTO of the updated maintenance
     * @param maintenanceOld maintenance before update (needed for logging)
     * @param houseHold household to which the maintenance belongs to
     * @return updated maintenance, if the operation was successful
     * @see Maintenance
     * @see MaintenanceCreationDTO
     */
    Maintenance updateMaintenance(Long maintenanceId, MaintenanceCreationDTO updatedMaintenance, Maintenance maintenanceOld, Household houseHold);

    /**
     * Hides the implementation of deleting a maintenance with specified id
     *
     * @param maintenanceId id of the maintenance to delete
     * @param householdId id of the household the maintenance to delete belongs to
     */
    void deleteMaintenance(Long maintenanceId, Long householdId);

    /**
     * Hides the implementation of changing the maintenance state (can be depreciated)
     *
     * @param updatedMaintenanceStateDTO DTO of the updated state maintenance
     * @param maintenanceId id of the maintenance to update state
     * @param maintenance old maintenance
     * @return maintenance with updated state, if operation was successful
     * @see Maintenance
     */
    Maintenance changeMaintenanceState(MaintenanceStateDTO updatedMaintenanceStateDTO, Long maintenanceId, Maintenance maintenance);

    /**
     * Hides the implementation of stopping the generation of maintenance task
     *
     * @param maintenance maintenance for which the task generation needs to stop
     * @return maintenance for which the task generation stopped
     */
    Maintenance stopGeneratingMaintenance(Maintenance maintenance);

    /**
     * Interface to automatically generate maintenance tasks after creation
     *
     * @param maintenance
     * @param update
     */
    void generateMaintenanceTasks(Maintenance maintenance, boolean update);
}
