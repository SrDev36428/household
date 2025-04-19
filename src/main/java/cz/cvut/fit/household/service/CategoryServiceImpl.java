package cz.cvut.fit.household.service;

import com.google.common.util.concurrent.AtomicDouble;
import cz.cvut.fit.household.daos.interfaces.CategoryDAO;
import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.category.CategoryAggregationDTO;
import cz.cvut.fit.household.datamodel.entity.category.CategoryCreationDTO;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.datamodel.enums.QuantityType;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryDAO categoryDAO;


    @Override
    public Category addCategory(CategoryCreationDTO updatedCategory, Household household, Category mainCategory) {
        Category category = new Category();

        log.debug("Creating a category: title - {} , description - {} , household - {} , mainCategory - {}"
                , updatedCategory.getTitle() , updatedCategory.getDescription() , household , mainCategory);
        category.setTitle(updatedCategory.getTitle());
        category.setDescription(updatedCategory.getDescription());
        category.setHouseHolD(household);
        category.setMainCategory(mainCategory);
        return categoryDAO.saveCategory(category);
    }

    @Override
    public List<Category> findAllCategory() {
        return categoryDAO.findAllCategories();
    }

    @Override
    public List<Category> findAllSubCategory(Category mainCategory) {
        List<Category> allSubCategory = mainCategory.getSubCategory();
        List<Category> resultList = new ArrayList<>();

        for(Category subCategory : allSubCategory){
            resultList.add(subCategory);
            resultList.addAll(findAllSubCategory(subCategory));
        }

        return resultList;
    }

    @Override
    public Optional<Category> findCategoryById(Long id) {
        return categoryDAO.findCategoryById(id);
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryDAO.deleteCategoryById(id);
    }

    @Override
    public Category updateCategory(Long categoryId, CategoryCreationDTO updatedCategory) {
        Category category = findCategoryById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with id - {} does not exist " , categoryId);
                    return new NonExistentEntityException("Category with id: " + categoryId + " doesn't exist");});

        category.setTitle(updatedCategory.getTitle());
        category.setDescription(updatedCategory.getDescription());
        return categoryDAO.saveCategory(category);
    }

    @Override
    public Category getCategoryById(Long id){ return categoryDAO.getCategoryById(id); }

    @Override
    public List<Category>findwithHousehold(Long householdId){
        List<Category>category = categoryDAO.findAllCategories();
        Boolean check;
        List<Category>categories = new ArrayList<>();
        for(Category category1: category){
            if(category1.getHouseHolD() == null){
                check = findHousehold(category1, householdId);
                if(check.equals(true)){
                    categories.add(category1);
                }
            }
            else if(category1.getHouseHolD().getId().equals(householdId)) {
                categories.add(category1);
            }
        }
        return categories;

    }

    private boolean findHousehold(Category category, Long householdId) {
        while(category.getHouseHolD() == null){
            category = category.getMainCategory();
        }

        return category.getHouseHolD().getId().equals(householdId);
    }

    public void calculateQuantity(List<Item> items, AtomicDouble kilo, AtomicDouble totalKilo,
                                  AtomicDouble piece, AtomicDouble totalPiece,
                                  AtomicDouble liter, AtomicDouble totalLiter){
        for(Item item: items){
            if(item.getQuantityType() != null && item.getQuantityType().equals(QuantityType.KILOGRAM)){
                kilo.addAndGet(item.getCurrentQuantity());
                totalKilo.addAndGet(item.getMaxQuantity());
            }else if(item.getQuantityType() != null && item.getQuantityType().equals(QuantityType.LITRE)){
                liter.addAndGet(item.getCurrentQuantity());
                totalLiter.addAndGet(item.getMaxQuantity());
            }else if(item.getQuantityType() != null && item.getQuantityType().equals(QuantityType.PIECES)){
                piece.addAndGet(item.getCurrentQuantity());
                totalPiece.addAndGet(item.getMaxQuantity());
            }
        }
    }

    @Override
    public void categoryTotalAmount(Category category, AtomicDouble sumKilo,
                                    AtomicDouble sumLiter, AtomicDouble sumPiece) {
        List<Item> items = category.getItems();
        for(Item item: items){
            if(item.getQuantityType() != null && item.getQuantityType().getType().equals("kg")){
                sumKilo.addAndGet(item.getCurrentQuantity());
            }else if(item.getQuantityType() != null && item.getQuantityType().getType().equals("l")){
                sumLiter.addAndGet(item.getCurrentQuantity());
            }else if(item.getQuantityType() != null && item.getQuantityType().getType().equals("p")){
                sumPiece.addAndGet(item.getCurrentQuantity());
            }
        }
    }

    @Override
    public List<CategoryAggregationDTO> aggregateCategories (List<Category> categories) {
        List<CategoryAggregationDTO> aggregatedCategories = new ArrayList<>();

        for(Category category: categories) {
            CategoryAggregationDTO aggregatedCategory = new CategoryAggregationDTO();
            AtomicDouble sumKilo = new AtomicDouble(0);
            AtomicDouble sumLiter = new AtomicDouble(0);
            AtomicDouble sumPiece = new AtomicDouble(0);
            categoryTotalAmount(category, sumKilo, sumLiter, sumPiece);

            aggregatedCategory.setId(category.getId());
            aggregatedCategory.setHousehold(category.getHouseHolD());
            aggregatedCategory.setItems(category.getItems());
            aggregatedCategory.setMainCategory(category.getMainCategory());
            aggregatedCategory.setSubCategories(category.getSubCategory());
            aggregatedCategory.setTitle(category.getTitle());
            aggregatedCategory.setDescription(category.getDescription());
            aggregatedCategory.setSumKilo(sumKilo);
            aggregatedCategory.setSumLiter(sumLiter);
            aggregatedCategory.setSumPiece(sumPiece);

            aggregatedCategories.add(aggregatedCategory);
        }

        return aggregatedCategories;
    }

}
