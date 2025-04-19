/**
 * The CategoryService interface provides methods to manage Category entities in the application.
 * It includes operations for adding, updating, getting, and deleting categories, as well as
 * calculating quantities related to items.
 *
 * @see cz.cvut.fit.household.datamodel.entity.category.Category
 * @see cz.cvut.fit.household.datamodel.entity.category.CategoryCreationDTO
 * @see cz.cvut.fit.household.datamodel.entity.household.Household
 * @see cz.cvut.fit.household.datamodel.entity.item.Item
 */
package cz.cvut.fit.household.service.interfaces;

import com.google.common.util.concurrent.AtomicDouble;
import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.category.CategoryAggregationDTO;
import cz.cvut.fit.household.datamodel.entity.category.CategoryCreationDTO;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.Item;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    /**
     * Adds a new Category to the specified Household, optionally associating it with a main category.
     *
     * @param categoryCreationDTO The data needed to create the new Category.
     * @param household           The Household to which the new Category belongs.
     * @param mainCategory        The main Category to associate with the new Category (can be null).
     * @return The created Category.
     */
    Category addCategory(CategoryCreationDTO categoryCreationDTO, Household household, Category mainCategory);

    /**
     * Retrieves a list of all Category entities in the application.
     *
     * @return A list of all Category objects.
     */
    List<Category> findAllCategory();

    /**
     * Retrieves a list of all subcategories of the given Category.
     *
     * @param category The Category for which to retrieve subcategories.
     * @return A list of subcategories.
     */
    List<Category> findAllSubCategory(Category category);

    /**
     * Finds a Category entity by its unique id.
     *
     * @param id The unique id of the Category to be retrieved.
     * @return An Optional containing the found Category, or an empty Optional if not found.
     */
    Optional<Category> findCategoryById(Long id);

    /**
     * Deletes a Category entity by its unique id.
     *
     * @param id The unique id of the Category to be deleted.
     */
    void deleteCategoryById(Long id);

    /**
     * Updates a Category entity with the provided data.
     *
     * @param categoryId     The unique id of the Category to be updated.
     * @param updatedCategory The updated data for the Category.
     * @return The updated Category.
     */
    Category updateCategory(Long categoryId, CategoryCreationDTO updatedCategory);

    /**
     * Retrieves a Category entity by its unique id.
     *
     * @param id The unique id of the Category to be retrieved.
     * @return The Category object if found, or null if not found.
     */
    Category getCategoryById(Long id);

    /**
     * Finds all categories associated with the specified Household.
     *
     * @param householdId The unique id of the Household.
     * @return A list of Category objects associated with the Household.
     */
    List<Category> findwithHousehold(Long householdId);

    /**
     * Calculates quantities based on the provided list of items.
     *
     * @param items       The list of items for which to calculate quantities.
     * @param kilo        AtomicDouble to store the total kilos.
     * @param totalKilo   AtomicDouble to store the total quantity in kilos.
     * @param piece       AtomicDouble to store the total pieces.
     * @param totalPiece  AtomicDouble to store the total quantity in pieces.
     * @param liter       AtomicDouble to store the total liters.
     * @param totalLiter  AtomicDouble to store the total quantity in liters.
     */
    void calculateQuantity(List<Item> items, AtomicDouble kilo, AtomicDouble totalKilo,
                           AtomicDouble piece, AtomicDouble totalPiece,
                           AtomicDouble liter, AtomicDouble totalLiter);

    /**
     * Calculates the amounts of items under the given category.
     *
     * @param category  The Category for which to calculate the sums of items under.
     * @param sumKilo   AtomicDouble to store the sum of kilos.
     * @param sumLiter   AtomicDouble to store the sum of liters.
     * @param sumPiece   AtomicDouble to store the sum of pieces.
     */
    void categoryTotalAmount(Category category, AtomicDouble sumKilo,
                             AtomicDouble sumLiter, AtomicDouble sumPiece);

    /**
     * Transforms the given list of categories into a list of CategoryAggregationDTOs.
     *
     * @param categories List of categories to transform into list of CategoryAggregationDTO.
     * @return A list of CategoryAggregationDTOs that is derived from the given list of categories.
     */
    List<CategoryAggregationDTO> aggregateCategories (List<Category> categories);

}
