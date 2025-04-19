/**
 * The ItemDAO interface is a Data Access Object that is used to manage Item entities in the data storage.
 * It provides methods for saving, getting, and deleting items.
 *
 * @see cz.cvut.fit.household.datamodel.entity.item.Item
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.Item;

import java.util.List;
import java.util.Optional;

public interface ItemDAO {

    /**
     * Saves the given Item entity in the data storage.
     *
     * @param item The Item object to be saved.
     * @return The saved Item object.
     */
    Item saveItem(Item item);

    /**
     * Retrieves a list of all Item entities in the data storage.
     *
     * @return A list of Item objects representing all items.
     */
    List<Item> findAllItems();

    /**
     * Finds an Item entity by its unique id.
     *
     * @param id The unique id of the Item to be retrieved.
     * @return An Optional containing the found Item, or an empty Optional if not found.
     */
    Optional<Item> findItemById(Long id);

    /**
     * Deletes an Item entity from the data storage by its unique id.
     *
     * @param id The unique id of the Item to be deleted.
     */
    void deleteItemById(Long id);

    /**
     * Finds all Items in a Household by its id.
     *
     * @param id The unique id of the Household the items of which needs to be found.
     */
    List<Item> findItemsByHouseholdId(Long id);
}
