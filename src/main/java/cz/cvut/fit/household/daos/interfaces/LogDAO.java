/**
 * The LogDAO interface is responsible for managing Log entities in the data storage.
 * It provides methods for saving, deleting, and retrieving logs.
 *
 * @see cz.cvut.fit.household.datamodel.entity.Log
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.Log;

import java.util.List;

public interface LogDAO {

    /**
     * Saves the given Log entity in the data storage.
     *
     * @param log The Log object to be saved.
     * @return The saved Log object.
     */
    Log saveLog(Log log);

    /**
     * Deletes a specific log by its unique id.
     *
     * @param id The unique id of the log to be deleted.
     */
    void deleteLogById(Long id);

    /**
     * Retrieves a list of Log entities for a specific household, ordered by ID in descending order.
     *
     * @param id The unique id of the household for which logs should be retrieved.
     * @return A list of Log objects for the specified household, ordered by ID in descending order.
     */
    List<Log> findLogByHouseholdIdOrderByIdDesc(Long id);
}
