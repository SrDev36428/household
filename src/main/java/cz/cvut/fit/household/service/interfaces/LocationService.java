/**
 * The LocationService interface provides methods to manage Location entities in the application.
 * It includes operations for adding, updating, getting, and deleting locations, as well as
 * finding locations based on household association.
 *
 * @see cz.cvut.fit.household.datamodel.entity.household.Household
 * @see cz.cvut.fit.household.datamodel.entity.location.Location
 * @see cz.cvut.fit.household.datamodel.entity.location.LocationCreationDTO
 */
package cz.cvut.fit.household.service.interfaces;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.entity.location.LocationCreationDTO;

import java.util.List;
import java.util.Optional;

public interface LocationService {

    /**
     * Adds a new Location to the specified Household, optionally associating it with a main location.
     *
     * @param locationCreationDTO The data needed to create the new Location.
     * @param household           The Household to which the new Location belongs.
     * @param mainLocation        The main Location to associate with the new Location (can be null).
     * @return The created Location.
     */
    Location addLocation(LocationCreationDTO locationCreationDTO, Household household, Location mainLocation);

    /**
     * Retrieves a list of all Location entities in the application.
     *
     * @return A list of all Location objects.
     */
    List<Location> findAllLocations();

    /**
     * Retrieves a list of all sublocations of the given Location.
     *
     * @param location The Location for which to retrieve sublocations.
     * @return A list of sublocations.
     */
    List<Location> findAllSubLocations(Location location);

    /**
     * Finds a Location entity by its unique id.
     *
     * @param id The unique id of the Location to be retrieved.
     * @return An Optional containing the found Location, or an empty Optional if not found.
     */
    Optional<Location> findLocationById(Long id);

    /**
     * Deletes a Location entity by its unique id.
     *
     * @param id The unique id of the Location to be deleted.
     */
    void deleteLocationById(Long id);

    /**
     * Updates a Location entity with the provided data.
     *
     * @param locationId         The unique id of the Location to be updated.
     * @param updatedLocation    The updated data for the Location.
     * @return The updated Location.
     */
    Location updateLocation(Long locationId, LocationCreationDTO updatedLocation);

    /**
     * Finds all locations associated with the specified Household.
     *
     * @param householdId The unique id of the Household.
     * @return A list of Location objects associated with the Household.
     */
    List<Location> findLocationsInHousehold(Long householdId);
}
