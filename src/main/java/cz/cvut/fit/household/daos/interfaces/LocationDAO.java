/**
 * The LocationDAO interface is a Data Access Object that is used to manage Location entities in the data storage.
 * It provides methods for saving, getting, and deleting locations.
 *
 * @see cz.cvut.fit.household.datamodel.entity.location.Location
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.location.Location;

import java.util.List;
import java.util.Optional;

public interface LocationDAO {

    /**
     * Saves the given Location entity in the data storage.
     *
     * @param location The Location object to be saved.
     * @return The saved Location object.
     */
    Location saveLocation(Location location);

    /**
     * Retrieves a list of all Location entities in the data storage.
     *
     * @return A list of Location objects representing all locations.
     */
    List<Location> findAllLocations();

    /**
     * Finds a Location entity by its unique id.
     *
     * @param id The unique id of the Location to be retrieved.
     * @return An Optional containing the found Location, or an empty Optional if not found.
     */
    Optional<Location> findLocationById(Long id);

    /**
     * Deletes a Location entity from the data storage by its unique id.
     *
     * @param id The unique id of the Location to be deleted.
     */
    void deleteLocationById(Long id);
}
