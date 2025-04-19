/**
 * The CategoryDAO interface is a Data Access Object that is used to manage Category entities in the data storage.
 * It provides methods for saving, getting, and deleting categories.
 *
 * @see cz.cvut.fit.household.datamodel.entity.category.Category
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.category.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDAO {

    /**
     * Saves the given Category entity in the data storage.
     *
     * @param category The Category object to be saved.
     * @return The saved Category object.
     */
    Category saveCategory(Category category);

    /**
     * Retrieves a list of all Category entities from the data storage.
     *
     * @return A list of Category objects representing all categories.
     */
    List<Category> findAllCategories();

    /**
     * Finds a Category entity by its unique id.
     *
     * @param id The unique id of the Category to be retrieved.
     * @return An Optional containing the found Category, or an empty Optional if not found.
     */
    Optional<Category> findCategoryById(Long id);

    /**
     * Deletes a Category entity from the data storage by its unique id.
     *
     * @param id The unique id of the Category to be deleted.
     */
    void deleteCategoryById(Long id);

    /**
     * Retrieves a Category entity by its unique id.
     *
     * @param id The unique id of the Category to be retrieved.
     * @return The Category object if found, or null if not found.
     */
    Category getCategoryById(Long id);
}
