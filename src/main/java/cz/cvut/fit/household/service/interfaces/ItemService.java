/**
 * The ItemService interface provides methods to manage Item entities in the application.
 * It includes operations for adding, updating, getting, deleting items, relocating items,
 * and finding items based on location, category, or globally.
 *
 * @see cz.cvut.fit.household.datamodel.entity.category.Category
 * @see cz.cvut.fit.household.datamodel.entity.item.ItemCreationDTO
 * @see cz.cvut.fit.household.datamodel.entity.item.ItemRelocationDTO
 * @see cz.cvut.fit.household.datamodel.entity.location.Location
 * @see cz.cvut.fit.household.datamodel.entity.item.Item
 */
package cz.cvut.fit.household.service.interfaces;

import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.ItemCreationDTO;
import cz.cvut.fit.household.datamodel.entity.item.ItemFilterDTO;
import cz.cvut.fit.household.datamodel.entity.item.ItemRelocationDTO;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.entity.item.Item;

import java.util.List;
import java.util.Optional;

public interface ItemService {

    /**
     * Adds a new Item to the specified Location.
     *
     * @param item     The data needed to create the new Item.
     * @param location The Location to which the new Item belongs.
     * @return The created Item.
     */
    Item addItem(ItemCreationDTO item, Location location);

    /**
     * Updates an existing Item with the provided data.
     *
     * @param updatedItem The updated data for the Item.
     * @param id          The unique id of the Item to be updated.
     * @return The updated Item.
     */
    Item updateItem(ItemCreationDTO updatedItem, Long id);

    /**
     * Increases the quantity of an Item.
     *
     * @param increaseQuantity The quantity to be added.
     * @param id               The unique id of the Item to be updated.
     * @return The updated Item.
     */
    Item increaseItemQuantity(String increaseQuantity, Long id);

    /**
     * Decreases the quantity of an Item.
     *
     * @param decreaseQuantity The quantity to be subtracted.
     * @param id               The unique id of the Item to be updated.
     * @return The updated Item.
     */
    Item decreaseItemQuantity(String decreaseQuantity, Long id);

    /**
     * Relocates an Item to a new Location.
     *
     * @param itemRelocationDto The data needed to relocate the Item.
     * @return The relocated Item.
     */
    Item relocateItem(ItemRelocationDTO itemRelocationDto);

    /**
     * Finds all items from a household that pass a specific filter.
     *
     * @param household household that the items belong to.
     * @param filterDTO the DTO with values the according to which the items are filtered
     * @return A list of items in the specified Household
     */
    List<Item>findAllItemsByHouseholdFiltered(Household household, ItemFilterDTO filterDTO);

    /**
     * Finds all Items in a given Location.
     *
     * @param location The Location for which to retrieve items.
     * @return A list of items in the specified Location.
     */
    List<Item> findItemsByLocation(Location location);

    /**
     * Finds all Items in a given Location that pass the filter.
     *
     * @param location The Location for which to retrieve items.
     * @param itemFilter the DTO with values the according to which the items are filtered
     * @return A list of items in the specified Location.
     */
    List<Item> findItemsByLocationFiltered(Location location, ItemFilterDTO itemFilter);

    /**
     * Finds all the highest max quantity an item can have.
     *
     * @return A double value of the highest max quantity.
     */
    Double getHighestMaxQuantity();

    /**
     * Finds all the highest current quantity an item can have.
     *
     * @return A double value of the highest current quantity.
     */
    Double getHighestCurrentQuantity();

    /**
     * Finds all Items in a given Category.
     *
     * @param category The Category for which to retrieve items.
     * @return A list of items in the specified Category.
     */
    List<Item> findItemsByCategory(Category category);

    /**
     * Finds all Items in a given Category that pass the filter.
     *
     * @param category The Category for which to retrieve items.
     * @param itemFilter the DTO with values the according to which the items are filtered
     * @return A list of items in the specified Category.
     */
    List<Item> findItemsByCategoryFiltered(Category category, ItemFilterDTO itemFilter);

    /**
     * Finds all Items in the application.
     *
     * @return A list of all Item objects.
     */
    List<Item> findItems();

    /**
     * Finds an Item entity by its unique id.
     *
     * @param id The unique id of the Item to be retrieved.
     * @return An Optional containing the found Item, or an empty Optional if not found.
     */
    Optional<Item> findItemById(Long id);

    /**
     * Creates multiple item entities with same properties.
     *
     * @param item ItemCreationDTO of the items to be created.
     * @param location Location where the items will be created under.
     * @param multipleCreation Number of items to be created.
     */
    void createMultipleItems(ItemCreationDTO item, Location location, Integer multipleCreation);

    /**
     * Deletes an Item entity by its unique id.
     *
     * @param id The unique id of the Item to be deleted.
     */
    void deleteItemById(Long id);

    /**
     * Sorts a list of items.
     *
     * @param items The list of items to be sorted.
     * @return A sorted list of items.
     */
    List<Item> sortItemList(List<Item> items);


}
