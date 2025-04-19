package cz.cvut.fit.household.controller;


import com.google.common.util.concurrent.AtomicDouble;
import cz.cvut.fit.household.controller.controllerUtil.ControllerItemDisplayUtil;
import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.category.CategoryAggregationDTO;
import cz.cvut.fit.household.datamodel.entity.category.CategoryCreationDTO;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.datamodel.entity.item.ItemFilterDTO;
import cz.cvut.fit.household.datamodel.enums.QuantityType;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.AuthorizationService;
import cz.cvut.fit.household.service.interfaces.CategoryService;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.ItemService;
import cz.cvut.fit.household.service.interfaces.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/household")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final HouseHoldService houseHoldService;
    private final CategoryService categoryService;
    private final ItemService itemService;
    private final LocationService locationService;
    private final AuthorizationService authorizationService;
    private final ControllerItemDisplayUtil itemDisplayUtil;

    private static final String CATEGORY_ATTR = "category";
    private static final String HOUSEHOLD_ATTR = "household";
    private static final String ITEMS_ATTR = "items";
    private static final String HOUSEHOLD_ID_ATTR = "householdId";
    private static final String MAIN_CATEGORY_ATTR = "mainCategory";
    private static final String AVAILABLE_SUB_CATEGORY_ATTR = "availableSubCategory";
    private static final String HOUSEHOLD_WITH_ID = "Household With id: ";
    private static final String CATEGORY_WITH_ID = "Category With id: ";
    private static final String DOES_NOT_EXIST = " doesn't exist";
    private static final String OWNER_PERMISSION = "permission";
    private static final String REDIRECTION_HOUSEHOLD = "redirect:/household/";
    private static final String RETURN_CATEGORY_DETAILS = "category/categoryDetails";

    @GetMapping("/{householdId}/category/add")
    public String renderAddCategoryPage(@PathVariable Long householdId,
                                        Model model) {
        CategoryCreationDTO categoryCreationDTO = new CategoryCreationDTO();
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(CATEGORY_ATTR, categoryCreationDTO);
        return "category/addCategory";
    }

    @GetMapping("/{householdId}/category/{categoryId}")
    public String renderCategoryInfoPage(@RequestParam(value = "sort", defaultValue = "title") String sort,
                                         @PathVariable Long householdId,
                                         @PathVariable Long categoryId,
                                         @ModelAttribute("filter") ItemFilterDTO finalFilter,
                                         Model model,
                                         @RequestParam("page") Optional<Integer> page,
                                         @RequestParam("size") Optional<Integer> size,
                                         @RequestParam(value = "title", required = false) Optional<String> title,
                                         @RequestParam(value = "category", required = false) Optional<String> filterCategory,
                                         @RequestParam(value = "location", required = false) Optional<String> filterLocation,
                                         @RequestParam(value = "quantityType", required = false) Optional<String> quantityType,
                                         @RequestParam(value = "maxQuantityMin", required = false) Optional<String> maxQuantityMin,
                                         @RequestParam(value = "maxQuantityMax", required = false) Optional<String> maxQuantityMax,
                                         @RequestParam(value = "currentQuantityMin", required = false) Optional<String> currentQuantityMin,
                                         @RequestParam(value = "currentQuantityMax", required = false) Optional<String> currentQuantityMax,
                                         @RequestParam(value = "dateMin", required = false) Optional<String> dateMin,
                                         @RequestParam(value = "dateMax", required = false) Optional<String> dateMax

    ) {
        itemDisplayUtil.setUpFilter(finalFilter,
                title,
                filterCategory,
                filterLocation,
                quantityType,
                maxQuantityMin,
                maxQuantityMax,
                currentQuantityMin,
                currentQuantityMax,
                dateMin,
                dateMax);

        Double highMaxQuantity = itemService.getHighestMaxQuantity();
        Double highCurrentQuantity = itemService.getHighestCurrentQuantity();

        ItemFilterDTO defaultFilter = new ItemFilterDTO();
        defaultFilter.setMaxQuantityMax(highMaxQuantity);
        defaultFilter.setMaxQuantityMin(0.0);
        defaultFilter.setCurrentQuantityMax(highCurrentQuantity);
        defaultFilter.setCurrentQuantityMin(0.0);

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);

        int startItem = (currentPage - 1) * pageSize;

        Category category = categoryService.findCategoryById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with id - {} does not exist in household with id - {} ", categoryId, householdId);
                    return new NonExistentEntityException(CATEGORY_WITH_ID + categoryId + DOES_NOT_EXIST);
                });

        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        List<Item> items = itemService.findItemsByCategoryFiltered(category, finalFilter);

        items.sort((item1, item2) -> {
            switch (sort) {
                case "title":
                    return item1.getTitle().compareTo(item2.getTitle());
                case "-title":
                    return item2.getTitle().compareTo(item1.getTitle());
                case "category":
                    return item1.getCategory().getTitle().compareTo(item2.getCategory().getTitle());
                case "-category":
                    return item2.getCategory().getTitle().compareTo(item1.getCategory().getTitle());
                case "description":
                    return item1.getDescription().compareTo(item2.getDescription());
                case "-description":
                    return item2.getDescription().compareTo(item1.getDescription());
                case "expiration":
                    return Comparator.nullsLast((Timestamp d1, Timestamp d2) -> d1.compareTo(d2)).compare((Timestamp) item1.getExpiration(), (Timestamp) item2.getExpiration());
                case "-expiration":
                    return Comparator.nullsFirst((Timestamp d1, Timestamp d2) -> d2.compareTo(d1)).compare((Timestamp) item1.getExpiration(), (Timestamp) item2.getExpiration());
                case "maxQuantity":
                    return item1.getMaxQuantity().compareTo(item2.getMaxQuantity());
                case "-maxQuantity":
                    return item2.getMaxQuantity().compareTo(item1.getMaxQuantity());
                case "currentQuantity":
                    return item1.getCurrentQuantity().compareTo(item2.getCurrentQuantity());
                case "-currentQuantity":
                    return item2.getCurrentQuantity().compareTo(item1.getCurrentQuantity());
                case "location":
                    return item1.getLocation().getTitle().compareTo(item2.getLocation().getTitle());
                case "-location":
                    return item2.getLocation().getTitle().compareTo(item1.getLocation().getTitle());
            }

            return 0;
        });

        while (items.size() < startItem && currentPage > 1) {
            currentPage--; // Move to the previous page
            startItem = (currentPage - 1) * pageSize;
        }

        List<Item> list;
        if (items.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, items.size());
            list = items.subList(startItem, toIndex);
        }

        Page<Item> itemPage = new PageImpl<Item>(list, PageRequest.of(currentPage - 1, pageSize), items.size());

        int totalPages = itemPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        AtomicDouble kilo = new AtomicDouble(0);
        AtomicDouble totalKilo = new AtomicDouble(0);
        AtomicDouble piece = new AtomicDouble(0);
        AtomicDouble totalPiece = new AtomicDouble(0);
        AtomicDouble liter = new AtomicDouble(0);
        AtomicDouble totalLiter = new AtomicDouble(0);
        categoryService.calculateQuantity(items, kilo, totalKilo, piece, totalPiece, liter, totalLiter);
        List<Category> subcategories = categoryService.findAllSubCategory(category);
        List<CategoryAggregationDTO> aggregatedSubcategories = categoryService.aggregateCategories(subcategories);

        model.addAttribute(OWNER_PERMISSION, authorizationService.isOwner(houseHold));
        model.addAttribute(HOUSEHOLD_ATTR, houseHold);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute("kilo", kilo);
        model.addAttribute("piece", piece);
        model.addAttribute("liter", liter);
        model.addAttribute("tkilo", totalKilo);
        model.addAttribute("tpiece", totalPiece);
        model.addAttribute("tliter", totalLiter);
        model.addAttribute(MAIN_CATEGORY_ATTR, category);
        model.addAttribute(AVAILABLE_SUB_CATEGORY_ATTR, aggregatedSubcategories);
        model.addAttribute("pageNumber", currentPage);
        model.addAttribute("sort", sort);
        model.addAttribute(ITEMS_ATTR, itemPage);
        model.addAttribute("locations", locationService.findLocationsInHousehold(householdId));
        model.addAttribute("categories", categoryService.findwithHousehold(householdId));
        model.addAttribute("queryParams", itemDisplayUtil.buildQueryParams(finalFilter));
        model.addAttribute("filter", finalFilter);

        return RETURN_CATEGORY_DETAILS;
    }

    @GetMapping("/{householdId}/category")
    public String renderCategoryPage(@PathVariable Long householdId, Model model) {
        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST));

        model.addAttribute(OWNER_PERMISSION, authorizationService.isOwner(houseHold));
        model.addAttribute(HOUSEHOLD_ATTR, houseHold);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute("availableCategory", houseHold.getCategory());
        return "category/householdCategory";
    }

    @PostMapping("/{householdId}/category/add")
    public String addCategory(@PathVariable Long householdId,
                              @Valid @ModelAttribute("category") CategoryCreationDTO category,
                              BindingResult result,
                              Model model) {
        log.debug("Creating category - {}  in household with id - {}", category, householdId);

        if (result.hasErrors()) {
            log.error(result.getAllErrors().toString());
            model.addAttribute(CATEGORY_ATTR, category);
            return "category/addCategory";
        }

        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        categoryService.addCategory(category, houseHold, null);
        log.info("Category - {} created successfully in household with id - {}", category, houseHold.getId());
        model.addAttribute(OWNER_PERMISSION, authorizationService.isOwner(houseHold));
        model.addAttribute(HOUSEHOLD_ATTR, houseHold);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute("availableCategory", houseHold.getCategory());
        return "redirect:/household/{householdId}/category";
    }

    @GetMapping("/{householdId}/category/{categoryId}/delete")
    public RedirectView deleteCategory(@PathVariable Long categoryId,
                                       @PathVariable String householdId) {

        log.debug("Deleting category with id - {} in household with id - {}", categoryId, householdId);
        Category category = categoryService.findCategoryById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with id - {} does not exist", categoryId);
                    return new NonExistentEntityException(CATEGORY_WITH_ID + categoryId + DOES_NOT_EXIST);
                });

        if (category.getMainCategory() == null) {
            categoryService.deleteCategoryById(category.getId());
            return new RedirectView("/" + HOUSEHOLD_ATTR + "/" + householdId + "/" + CATEGORY_ATTR);
        }

        categoryService.deleteCategoryById(category.getId());

        log.info("Category with id - {} has been deleted in household with id - {}", categoryId, householdId);
        return new RedirectView("/" + HOUSEHOLD_ATTR + "/" + householdId + "/" + CATEGORY_ATTR + "/" + category.getMainCategory().getId());
    }

    @GetMapping("/{householdId}/category/{categoryId}/return")
    public ModelAndView returnToMainCategory(@PathVariable Long householdId,
                                             @PathVariable Long categoryId,
                                             Model model) {


        Optional<Category> category = categoryService.findCategoryById(categoryId);

        if (!category.isPresent() || category.get().getMainCategory() == null) {
            return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + CATEGORY_ATTR, (Map<String, ?>) model);
        }

        return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + CATEGORY_ATTR + "/" +
                category.get().getMainCategory().getId(), (Map<String, ?>) model);
    }

    @GetMapping("/{householdId}/category/{categoryId}/edit")
    public String renderEditingPage(@PathVariable Long householdId,
                                    @PathVariable Long categoryId,
                                    Model model) {
        Category category = categoryService.findCategoryById(categoryId)
                .orElseThrow(() -> new NonExistentEntityException(CATEGORY_WITH_ID + categoryId + DOES_NOT_EXIST));

        CategoryCreationDTO newCategory = new CategoryCreationDTO();

        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(CATEGORY_ATTR, category);
        model.addAttribute("newCategory", newCategory);

        return "category/editCategory/categoryEdit";
    }

    @PostMapping("/{householdId}/category/{categoryId}/edit")
    public ModelAndView performEditing(@PathVariable Long householdId,
                                       @PathVariable Long categoryId,
                                       @ModelAttribute CategoryCreationDTO updatedCategory,
                                       Model model) {
        log.debug("Updating category with id - {} in household with id - {} with new category - {} ", categoryId, householdId, updatedCategory);
        Household household = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Category category = categoryService.updateCategory(categoryId, updatedCategory);
        log.info("Category with id - {} has been updated in household with id -{}", categoryId, householdId);
        model.addAttribute(OWNER_PERMISSION, authorizationService.isOwner(household));
        model.addAttribute(HOUSEHOLD_ATTR, household);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(CATEGORY_ATTR, category);

        return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + CATEGORY_ATTR + "/" + categoryId + "/return", (Map<String, ?>) model);
    }
}
