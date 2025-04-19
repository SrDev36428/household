package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.controller.controllerUtil.ControllerItemDisplayUtil;
import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.events.OnInventoryChangeEvent;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.*;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.CategoryService;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.ItemService;
import cz.cvut.fit.household.service.interfaces.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/household")
@RequiredArgsConstructor
@Slf4j
public class ItemsController {

    private final ItemService itemService;
    private final LocationService locationService;
    private final HouseHoldService houseHoldService;
    private final ApplicationEventPublisher eventPublisher;
    private final CategoryService categoryService;
    private final ControllerItemDisplayUtil itemDisplayUtil;

    private static final String ITEM_DTO_ATTR = "itemDto";
    private static final String MAIN_LOCATION_ATTR = "mainLocation";
    private static final String MULTIPLE_CREATION_ATTR = "multipleCreation";
    private static final String LOCATION_ATTR = "location";
    private static final String HOUSEHOLD_ATTR = "household";
    private static final String AVAILABLE_SUB_LOCATIONS_ATTR = "availableSubLocations";
    private static final String CATEGORY_ATTR = "category";
    private static final String ITEM_ATTR = "item";
    private static final String ITEMS_ATTR = "items";
    private static final String CATEGORIES_ATTR = "categories";
    private static final String ERROR_ATTR = "error";
    private static final String QUANTITY_ERROR_MSG = "Quantity shouldn't be greater that max quantity and less than zero";
    private static final String EXPIRATION_ATTR = "expiration";
    private static final String CURRENT_QUANTITY_ATTR = "currentQuantity";
    private static final String MAX_QUANTITY_ATTR = "maxQuantity";
    private static final String HOUSEHOLD_ID_ATTR = "householdId";
    private static final String HOUSEHOLD_WITH_ID = "Household With id: ";
    private static final String LOCATION_WITH_ID = "Location With id: ";
    private static final String ITEM_WITH_ID = "Item With id: ";
    private static final String DOES_NOT_EXIST = " doesn't exist";
    private static final String ITEM_WITH_TITLE = "Item With title: ";
    private static final String CATEGORY_WITH_ID = "Category With id: ";
    private static final String REDIRECTION_HOUSEHOLD = "redirect:/household/";
    private static final String RETURN_QUANTITY_EDIT_CATEGORY = "items/edit/quantityEditFromCategory";
    private static final String RETURN_ITEM_QUANTITY_EDIT = "items/edit/quantityEdit";
    private static final String RETURN_ADD_ITEM = "items/add-item";
    private static final String SORT_ATTR = "sort";
    private static final String PAGE_NUMBER_ATTR = "pageNumber";
    private static final String PAGE_NUMBERS_ATTR = "pageNumbers";

    @GetMapping("/{householdId}/locations/{locationId}/items/add")
    public String renderAddItemPage(@PathVariable Long householdId,
                                    @PathVariable Long locationId,
                                    Model model) {
        ItemCreationDTO item = new ItemCreationDTO();

        Integer multipleCreation = 1;

        List<Category> categories = categoryService.findwithHousehold(householdId);

        model.addAttribute(ITEM_ATTR, item);
        model.addAttribute(CATEGORIES_ATTR, categories);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(MULTIPLE_CREATION_ATTR, multipleCreation);
        model.addAttribute("locationId", locationId);

        return RETURN_ADD_ITEM;
    }

    @PostMapping("/{householdId}/locations/{locationId}/items")
    public String addItem(@PathVariable Long householdId,
                          @PathVariable Long locationId,
                          @Valid @ModelAttribute("item") ItemCreationDTO item,
                          BindingResult result,
                          @RequestParam(name = "multipleCreation", defaultValue = "1") Integer multipleCreation,
                          Model model) {

        log.debug("Creating a new item - {} in location with id - {} of household with id - {}", item, locationId, householdId);
        Household household = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Location location = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id - {} does not exist", locationId);
                    return new NonExistentEntityException(LOCATION_WITH_ID + locationId + DOES_NOT_EXIST);
                });

        List<Category> categories = categoryService.findwithHousehold(householdId);

        try {
            if (!item.getMaxQuantity().isEmpty() && !item.getCurrentQuantity().isEmpty()) {
                double maxQuantity = Double.parseDouble(item.getMaxQuantity());
                double curQuantity = Double.parseDouble(item.getCurrentQuantity());

                if (maxQuantity <= 0) {
                    result.rejectValue(MAX_QUANTITY_ATTR, ERROR_ATTR, "Maximum quantity should not be higher than zero (0)");
                }

                if (curQuantity < 0) {
                    result.rejectValue(CURRENT_QUANTITY_ATTR, ERROR_ATTR, "Current quantity should not be negative");
                }

                if ((maxQuantity < curQuantity)) {
                    result.rejectValue(CURRENT_QUANTITY_ATTR, ERROR_ATTR, "Current quantity is higher than maximum");
                }

                if ((maxQuantity < curQuantity) || curQuantity < 0) {
                    log.error("Current quantity is higher than maximum");
                    result.rejectValue(MAX_QUANTITY_ATTR, ERROR_ATTR, "Current quantity is higher than maximum");
                }
            }

            if (item.getExpiration() != null && item.getExpiration().before(Date.valueOf(LocalDate.now()))) {
                log.error("Expiration time is before current date - invalid expiration date");
                result.rejectValue(EXPIRATION_ATTR, ERROR_ATTR, "Invalid expiration date");
            }

            if (!result.getAllErrors().isEmpty()) {
                model.addAttribute(CATEGORIES_ATTR, categories);
                log.error(result.getAllErrors().toString());
                return RETURN_ADD_ITEM;
            }
            if (item.getCategory() == null) {
                setCategoryAndExpirationError(model, item);
                model.addAttribute(CATEGORIES_ATTR, categories);
                return RETURN_ADD_ITEM;
            }

            itemService.createMultipleItems(item, location, multipleCreation);
            log.info("Items - {} of {} multiples  have been created in location with id -{} of household with id - {}", item, multipleCreation, locationId, householdId);
            model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
            model.addAttribute(MAIN_LOCATION_ATTR, location);
            model.addAttribute(AVAILABLE_SUB_LOCATIONS_ATTR, locationService.findAllSubLocations(location));

            eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, multipleCreation + " " + ITEM_WITH_TITLE + item.getTitle() + " created. " + MAX_QUANTITY_ATTR + ": " + item.getMaxQuantity() + ", " + CURRENT_QUANTITY_ATTR + ": " + item.getCurrentQuantity() + ", " + EXPIRATION_ATTR + ": " + item.getExpiration()));
            return "redirect:/household/{householdId}/locations/{locationId}/";
        } catch (NumberFormatException e) {
            log.error("Error occurred during Creating item - {} location with id - {} of household - {}  :", item, locationId, householdId, e);
            result.rejectValue(MAX_QUANTITY_ATTR, ERROR_ATTR, "Field should contain numeric value");
            result.rejectValue(CURRENT_QUANTITY_ATTR, ERROR_ATTR, "Field should contain numeric value");
            model.addAttribute(CATEGORIES_ATTR, categories);
            setCategoryAndExpirationError(model, item);

            return RETURN_ADD_ITEM;
        }
    }

    @GetMapping("/{householdId}/items")
    public String renderItemsList(@RequestParam(value = "sort", defaultValue = "title") String sort, @PathVariable Long householdId,
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

        Household household = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        List<Item> items = new ArrayList<>();

        items = itemService.findAllItemsByHouseholdFiltered(household, finalFilter);

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
            model.addAttribute(PAGE_NUMBERS_ATTR, pageNumbers);
        }

        model.addAttribute(HOUSEHOLD_ATTR, household);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(ITEMS_ATTR, items);
        model.addAttribute(ITEMS_ATTR, itemPage);
        model.addAttribute(SORT_ATTR, sort);
        model.addAttribute(PAGE_NUMBER_ATTR, currentPage);
        model.addAttribute("defaultFilter", defaultFilter);
        model.addAttribute("locations", locationService.findLocationsInHousehold(householdId));
        model.addAttribute(CATEGORIES_ATTR, categoryService.findwithHousehold(householdId));
        model.addAttribute("queryParams", itemDisplayUtil.buildQueryParams(finalFilter));
        model.addAttribute("filter", finalFilter);

        return "items/ItemsView";
    }


    @GetMapping("/{householdId}/locations/{locationId}/items")
    public ModelAndView renderLocationItemsList(@PathVariable Long householdId,
                                                @PathVariable Long locationId,
                                                Model model) {

        return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + LOCATION_ATTR + "s/" + locationId, (Map<String, ?>) model);
    }

    @GetMapping("/{householdId}/locations/{locationId}/items/{itemId}/delete")
    public ModelAndView deleteItem(@PathVariable Long householdId,
                                   @PathVariable Long locationId,
                                   @PathVariable Long itemId,
                                   Model model) {
        log.debug("Deleting item - {} in location with id - {} of household with id - {}", itemId, locationId, householdId);
        Household household = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id -{} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Location location = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id - {} does not exist", locationId);
                    return new NonExistentEntityException(LOCATION_WITH_ID + locationId + DOES_NOT_EXIST);
                });
        Item item = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });

        itemService.deleteItemById(itemId);
        log.info("Item with id - {} has been deleted in location with id -{} of household with id - {}", itemId, locationId, householdId);

        eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, ITEM_WITH_TITLE + item.getTitle() + " deleted"));
        model.addAttribute(HOUSEHOLD_ATTR, household);
        model.addAttribute(LOCATION_ATTR, location);
        return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + LOCATION_ATTR + "s/" + locationId, (Map<String, ?>) model);
    }

    @GetMapping("/{householdId}/category/{categoryId}/items/{itemId}/delete")
    public ModelAndView deleteItemFromCategory(@PathVariable Long householdId,
                                               @PathVariable Long categoryId,
                                               @PathVariable Long itemId,
                                               Model model) {
        log.debug("Deleting item - {} of category with id - {} of household with id - {}", itemId, categoryId, householdId);
        Household household = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Category category = categoryService.findCategoryById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with id - {} does not exist", categoryId);
                    return new NonExistentEntityException(CATEGORY_WITH_ID + categoryId + DOES_NOT_EXIST);
                });
        Item item = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });
        itemService.deleteItemById(itemId);
        log.info("Item with id - {} of category with id - {} has been deleted in household with id - {}", itemId, categoryId, householdId);
        eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, ITEM_WITH_TITLE + item.getTitle() + " deleted"));
        model.addAttribute(HOUSEHOLD_ATTR, household);
        model.addAttribute(CATEGORY_ATTR, category);
        return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + CATEGORY_ATTR + "/" + categoryId, (Map<String, ?>) model);
    }

    boolean validateItem(ItemCreationDTO itemDTO, Model model) {
        boolean hasError = false;
        String mQuantity = itemDTO.getMaxQuantity();
        String cQuantity = itemDTO.getCurrentQuantity();
        java.util.Date expiration = itemDTO.getExpiration();
        Category category = itemDTO.getCategory();
        String title = itemDTO.getTitle();

        if (title.isEmpty()) {
            hasError = true;
            model.addAttribute("titleError", "Title should not be empty");
        }

        if (mQuantity.isEmpty()) {
            hasError = true;
            model.addAttribute("maximumQuantityError", "Maximum quantity should not be empty");
        }

        if (cQuantity.isEmpty()) {
            hasError = true;
            model.addAttribute("currentQuantityError", "Current quantity should not be empty");
        }

        if (expiration != null && expiration.before(Date.valueOf(LocalDate.now()))) {
            hasError = true;
            model.addAttribute("expirationError", "Invalid expiration date");
        }

        if (!itemDTO.getMaxQuantity().isEmpty() && !itemDTO.getCurrentQuantity().isEmpty()) {
            double maxQuantity = Double.parseDouble(itemDTO.getMaxQuantity());
            double curQuantity = Double.parseDouble(itemDTO.getCurrentQuantity());

            if (maxQuantity <= 0) {
                hasError = true;
                model.addAttribute("maximumQuantityError", "Maximum quantity should not be higher than zero (0)");
            }

            if (curQuantity < 0) {
                hasError = true;
                model.addAttribute("currentQuantityError", "Current quantity should not be negative");
            }

            if ((maxQuantity < curQuantity)) {
                hasError = true;
                model.addAttribute("currentQuantityError", "Current quantity should not exceed maximum quantity");
            }
        }

        if (Objects.isNull(category)) {
            hasError = true;
            model.addAttribute("categoryError", "Category cannot be empty");
        }

        return hasError;

    }

    @GetMapping("/{householdId}/locations/{locationId}/items/{itemId}/edit")
    public String renderEditingPage(@PathVariable Long householdId,
                                    @PathVariable Long locationId,
                                    @PathVariable Long itemId,
                                    Model model) {
        setEditingPage(householdId, locationId, itemId, model);

        return "items/edit/itemEdit";
    }

    @PostMapping("/{householdId}/locations/{locationId}/items/{itemId}/edit")
    public String performEditing(@PathVariable Long householdId,
                                 @PathVariable Long locationId,
                                 @PathVariable Long itemId,
                                 @Valid @ModelAttribute("item") ItemCreationDTO updatedItem,
                                 Model model) {
        log.debug("Editing an item with id - {} in location with id -{} of house hold -{} with new item - {}", itemId, locationId, householdId, updatedItem);
        Location location = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id - {} does not exist", locationId);
                    return new NonExistentEntityException(LOCATION_WITH_ID + locationId + DOES_NOT_EXIST);
                });

        Item oldItem = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });
        String oldTitle = oldItem.getTitle();
        String oldCategory = oldItem.getCategory().getTitle();
        Double oldCurrentQuantity = oldItem.getCurrentQuantity();
        Double oldMaxQuantity = oldItem.getMaxQuantity();
        java.util.Date expiration = oldItem.getExpiration();

        boolean hasError = validateItem(updatedItem, model);

        if (hasError) {
            setEditingPage(householdId, locationId, itemId, model);
            model.addAttribute(ITEM_ATTR, oldItem);
            return "items/edit/itemEdit";
        }

        Item item = itemService.updateItem(updatedItem, itemId);
        log.info("Item with id - {} has been updated as {} in location with id - {} of household with id - {}", itemId, item, locationId, householdId);

        eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, ITEM_WITH_TITLE + oldItem.getTitle() + " updated. Before: title: " + oldTitle + ", category: "
                + oldCategory + ", " + MAX_QUANTITY_ATTR + oldMaxQuantity + ", "
                + CURRENT_QUANTITY_ATTR + ": " + oldCurrentQuantity + ", " + EXPIRATION_ATTR + ": " + expiration));
        eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, ". Now: title: " + item.getTitle() + ", "
                + MAX_QUANTITY_ATTR + item.getMaxQuantity() + ", " + CURRENT_QUANTITY_ATTR + ": "
                + item.getCurrentQuantity() + ", " + EXPIRATION_ATTR + ": " + item.getExpiration()));


        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(MAIN_LOCATION_ATTR, location);
        model.addAttribute(AVAILABLE_SUB_LOCATIONS_ATTR, locationService.findAllSubLocations(location));

        model.addAttribute(ITEM_ATTR, item);
        return "redirect:/household/{householdId}/locations/{locationId}";

    }

    @GetMapping("/{householdId}/category/{categoryId}/items/{itemId}/edit")
    public String renderEditingPageFromCategory(@PathVariable Long householdId,
                                                @PathVariable Long categoryId,
                                                @PathVariable Long itemId,
                                                Model model) {
        setEditingPageFromCategory(householdId, categoryId, itemId, model);

        return "items/edit/itemEditFromCategory";
    }

    @PostMapping("/{householdId}/category/{categoryId}/items/{itemId}/edit")
    public String performEditingFromCategory(@PathVariable Long householdId,
                                             @PathVariable Long categoryId,
                                             @PathVariable Long itemId,
                                             @ModelAttribute ItemCreationDTO updatedItem,
                                             Model model) {
        log.debug("Editing an item with id - {} of category with id -{} of household - {} with new item - {}", itemId, categoryId, householdId, updatedItem);
        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Category category = categoryService.findCategoryById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with id - {} does not exist", categoryId);
                    return new NonExistentEntityException(CATEGORY_WITH_ID + categoryId + DOES_NOT_EXIST);
                });
        Item oldItem = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });

        String title = oldItem.getTitle();
        String prevCategory = oldItem.getCategory().getTitle();
        Double currentQuantity = oldItem.getCurrentQuantity();
        Double maxQuantity = oldItem.getMaxQuantity();
        java.util.Date expiration = oldItem.getExpiration();

        boolean hasErrors = validateItem(updatedItem, model);

        if (hasErrors) {
            setEditingPageFromCategory(householdId, categoryId, itemId, model);
            model.addAttribute(ITEM_ATTR, oldItem);
            return "items/edit/itemEditFromCategory";
        }

        Item item = itemService.updateItem(updatedItem, itemId);
        log.info("Item with id - {} of category with id - {} has been updated as {} in household with id - {}", itemId, categoryId, item, householdId);


        model.addAttribute(HOUSEHOLD_ATTR, houseHold);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute("mainCategory", category);
        model.addAttribute("availableSubCategories", categoryService.findAllSubCategory(category));

        eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, ITEM_WITH_TITLE + oldItem.getTitle() + " updated. Before: title: " + title + ", category:" + prevCategory + ", " + MAX_QUANTITY_ATTR + maxQuantity + ", " + CURRENT_QUANTITY_ATTR + ": " + currentQuantity + ", " + EXPIRATION_ATTR + ": " + expiration + ". Now: title: " + item.getTitle() + ", " + MAX_QUANTITY_ATTR + item.getMaxQuantity() + ", " + CURRENT_QUANTITY_ATTR + ": " + item.getCurrentQuantity() + ", " + EXPIRATION_ATTR + ": " + item.getExpiration()));
        return "redirect:/household/{householdId}/category/{categoryId}";

    }

    @GetMapping("/{householdId}/locations/{locationId}/items/{itemId}/change-quantity")
    public String renderEditQuantityPage(@PathVariable Long householdId,
                                         @PathVariable Long locationId,
                                         @PathVariable Long itemId,
                                         Model model) {
        setEditingPage(householdId, locationId, itemId, model);

        ItemQuantityDTO itemDto = new ItemQuantityDTO();
        model.addAttribute(ITEM_DTO_ATTR, itemDto);

        return RETURN_ITEM_QUANTITY_EDIT;
    }

    @PostMapping(value = "/{householdId}/locations/{locationId}/items/{itemId}/increase-quantity")
    public ModelAndView increaseQuantity(@PathVariable Long householdId,
                                         @PathVariable Long locationId,
                                         @PathVariable Long itemId,
                                         @Valid @ModelAttribute("itemDto") ItemQuantityDTO itemDto,
                                         BindingResult result,
                                         Model model) {
        setEditingPage(householdId, locationId, itemId, model);
        log.debug("Increasing quantity of an item with id -{}  in location with id - {} of household with id -{} with new item quantity - {}", itemId, locationId, householdId, itemDto);

        if (result.hasErrors()) {
            log.error(result.getAllErrors().toString());
            return new ModelAndView(RETURN_ITEM_QUANTITY_EDIT, (Map<String, ?>) model);
        }

        Household household = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });


        Location location = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id ={} does not exist", locationId);
                    return new NonExistentEntityException(LOCATION_WITH_ID + locationId + DOES_NOT_EXIST);
                });

        Item foundItem = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });

        try {
            Double changedAmount = Double.valueOf(itemDto.getCurrentQuantity());
            Item item = itemService.increaseItemQuantity(itemDto.getCurrentQuantity(), itemId);

            model.addAttribute(HOUSEHOLD_ATTR, household);
            model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
            model.addAttribute(LOCATION_ATTR, location);
            model.addAttribute(ITEM_ATTR, item);
            log.info("Increased quantity of an item with id -{}  in location with id - {} of household with id -{} with new item quantity - {}", itemId, locationId, householdId, itemDto);
            eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, "Quantity of item with title:- " + foundItem.getTitle() + " was increased. Was: " + Double.valueOf(foundItem.getCurrentQuantity() - changedAmount) + ", now:- " + item.getCurrentQuantity()));
            return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + LOCATION_ATTR + "s/" + locationId + "/items", (Map<String, ?>) model);
        } catch (RuntimeException e) {
            log.error("Error occurred during increase quantity of of an item with id -{}  in location with id - {} of household with id -{} with new item quantity - {} : {} ", itemId, locationId, householdId, itemDto, QUANTITY_ERROR_MSG);
            result.rejectValue(CURRENT_QUANTITY_ATTR, ERROR_ATTR, QUANTITY_ERROR_MSG);
            return new ModelAndView(RETURN_ITEM_QUANTITY_EDIT, (Map<String, ?>) model);
        }
    }

    @PostMapping(value = "/{householdId}/locations/{locationId}/items/{itemId}/decrease-quantity")
    public ModelAndView decreaseQuantity(@PathVariable Long householdId,
                                         @PathVariable Long locationId,
                                         @PathVariable Long itemId,
                                         @Valid @ModelAttribute("itemDto") ItemQuantityDTO itemDto,
                                         BindingResult result,
                                         Model model) {

        setEditingPage(householdId, locationId, itemId, model);

        log.debug("Decreasing quantity of an item with id -{}  in location with id - {} of household with id -{} with new item quantity - {}", itemId, locationId, householdId, itemDto);
        if (result.hasErrors()) {
            log.error(result.getAllErrors().toString());
            return new ModelAndView(RETURN_ITEM_QUANTITY_EDIT, (Map<String, ?>) model);
        }

        Household household = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });


        Location location = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id ={} does not exist", locationId);
                    return new NonExistentEntityException(LOCATION_WITH_ID + locationId + DOES_NOT_EXIST);
                });

        Item foundItem = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });

        try {
            Double changedAmount = Double.valueOf(itemDto.getCurrentQuantity());
            Item item = itemService.decreaseItemQuantity(itemDto.getCurrentQuantity(), itemId);

            model.addAttribute(HOUSEHOLD_ATTR, household);
            model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
            model.addAttribute(LOCATION_ATTR, location);
            model.addAttribute(ITEM_ATTR, item);

            log.info("Decreased quantity of an item with id -{}  in location with id - {} of household with id -{} with new item quantity - {}", itemId, locationId, householdId, itemDto);

            eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, "Quantity of item with title:- " + foundItem.getTitle() + " was decreased. Was: " + Double.valueOf(foundItem.getCurrentQuantity() + changedAmount) + ", now:- " + item.getCurrentQuantity()));
            return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + LOCATION_ATTR + "s/" + locationId + "/items", (Map<String, ?>) model);
        } catch (RuntimeException e) {
            log.error("Error occurred during decrease quantity of of an item with id -{}  in location with id - {} of household with id -{} with new item quantity - {} : {} ", itemId, locationId, householdId, itemDto, QUANTITY_ERROR_MSG);
            result.rejectValue(CURRENT_QUANTITY_ATTR, ERROR_ATTR, QUANTITY_ERROR_MSG);
            return new ModelAndView(RETURN_ITEM_QUANTITY_EDIT, (Map<String, ?>) model);
        }
    }

    @GetMapping("/{householdId}/category/{categoryId}/items/{itemId}/change-quantity")
    public String renderEditQuantityPageFromCategory(@PathVariable Long householdId,
                                                     @PathVariable Long categoryId,
                                                     @PathVariable Long itemId,
                                                     Model model) {
        setEditingPageFromCategory(householdId, categoryId, itemId, model);

        ItemQuantityDTO itemDto = new ItemQuantityDTO();
        model.addAttribute(ITEM_DTO_ATTR, itemDto);

        return RETURN_QUANTITY_EDIT_CATEGORY;
    }

    @PostMapping(value = "/{householdId}/category/{categoryId}/items/{itemId}/increase-quantity")
    public ModelAndView increaseQuantityFromCategory(@PathVariable Long householdId,
                                                     @PathVariable Long categoryId,
                                                     @PathVariable Long itemId,
                                                     @Valid @ModelAttribute("itemDto") ItemQuantityDTO itemDto,
                                                     BindingResult result,
                                                     Model model) {

        log.debug("Increasing quantity of an item with id -{}  of category with id - {} of household with id -{} with new item quantity - {}", itemId, categoryId, householdId, itemDto);
        setEditingPageFromCategory(householdId, categoryId, itemId, model);

        if (result.hasErrors()) {
            log.error(result.getAllErrors().toString());
            return new ModelAndView(RETURN_QUANTITY_EDIT_CATEGORY, (Map<String, ?>) model);
        }

        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Category category = categoryService.findCategoryById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with id - {} does not exist", categoryId);
                    return new NonExistentEntityException(CATEGORY_WITH_ID + categoryId + DOES_NOT_EXIST);
                });

        Item foundItem = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });

        try {
            Double changedAmount = Double.valueOf(itemDto.getCurrentQuantity());
            Item item = itemService.increaseItemQuantity(itemDto.getCurrentQuantity(), itemId);
            log.info("Increased quantity of an item with id -{}  of category with id - {} of household with id -{} with new item quantity - {}", itemId, categoryId, householdId, itemDto);
            model.addAttribute(HOUSEHOLD_ATTR, houseHold);
            model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
            model.addAttribute(CATEGORY_ATTR, category);
            model.addAttribute(ITEM_ATTR, item);

            eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, "Quantity of item with title: " + foundItem.getTitle() + " was increased. Was: " + Double.valueOf(foundItem.getCurrentQuantity() - changedAmount) + ", now: " + item.getCurrentQuantity()));
            return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + CATEGORY_ATTR + "/" + categoryId, (Map<String, ?>) model);
        } catch (RuntimeException e) {
            log.error("Error occurred during decrease quantity of of an item with id -{}  of category with id - {} of household with id -{} with new item quantity - {} : {} ", itemId, categoryId, householdId, itemDto, QUANTITY_ERROR_MSG);
            result.rejectValue(CURRENT_QUANTITY_ATTR, ERROR_ATTR, QUANTITY_ERROR_MSG);
            return new ModelAndView(RETURN_QUANTITY_EDIT_CATEGORY, (Map<String, ?>) model);
        }
    }

    @PostMapping(value = "/{householdId}/category/{categoryId}/items/{itemId}/decrease-quantity")
    public ModelAndView decreaseQuantityFromCategory(@PathVariable Long householdId,
                                                     @PathVariable Long categoryId,
                                                     @PathVariable Long itemId,
                                                     @Valid @ModelAttribute("itemDto") ItemQuantityDTO itemDto,
                                                     BindingResult result,
                                                     Model model) {

        log.debug("Increasing quantity of an item with id -{}  of category with id - {} of household with id -{} with new item quantity - {}", itemId, categoryId, householdId, itemDto);
        setEditingPageFromCategory(householdId, categoryId, itemId, model);
        if (result.hasErrors()) {
            log.error(result.getAllErrors().toString());
            return new ModelAndView(RETURN_QUANTITY_EDIT_CATEGORY, (Map<String, ?>) model);
        }

        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Category category = categoryService.findCategoryById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with id - {} does not exist", categoryId);
                    return new NonExistentEntityException(CATEGORY_WITH_ID + categoryId + DOES_NOT_EXIST);
                });

        Item foundItem = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });

        try {
            Double changedAmount = Double.valueOf(itemDto.getCurrentQuantity());
            Item item = itemService.decreaseItemQuantity(itemDto.getCurrentQuantity(), itemId);
            log.info("Decreased quantity of an item with id -{}  of category with id - {} of household with id -{} with new item quantity - {}", itemId, categoryId, householdId, itemDto);
            model.addAttribute(HOUSEHOLD_ATTR, houseHold);
            model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
            model.addAttribute(CATEGORY_ATTR, category);
            model.addAttribute(ITEM_ATTR, item);

            eventPublisher.publishEvent(new OnInventoryChangeEvent(householdId, "Quantity of item with title: " + foundItem.getTitle() + " was decreased. Was: " + Double.valueOf(foundItem.getCurrentQuantity() + changedAmount) + ", now: " + item.getCurrentQuantity()));
            return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + CATEGORY_ATTR + "/" + categoryId, (Map<String, ?>) model);
        } catch (RuntimeException e) {
            log.error("Error occurred during decrease quantity of of an item with id -{}  of category with id - {} of household with id -{} with new item quantity - {} : {} ", itemId, categoryId, householdId, itemDto, QUANTITY_ERROR_MSG);
            result.rejectValue(CURRENT_QUANTITY_ATTR, ERROR_ATTR, QUANTITY_ERROR_MSG);
            return new ModelAndView(RETURN_QUANTITY_EDIT_CATEGORY, (Map<String, ?>) model);
        }
    }

    @GetMapping("/{householdId}/locations/{locationId}/items/{itemId}/relocate")
    public String itemRelocationPageRender(@PathVariable Long householdId,
                                           @PathVariable Long locationId,
                                           @PathVariable Long itemId,
                                           Model model) {
        List<Location> availableLocations = locationService.findLocationsInHousehold(householdId);
        ItemRelocationDTO itemRelocationDto = new ItemRelocationDTO(itemId);

        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute("locationId", locationId);
        model.addAttribute("locations", availableLocations);
        model.addAttribute(ITEM_DTO_ATTR, itemRelocationDto);


        return "items/edit/relocationItem";
    }

    @PostMapping("/{householdId}/locations/{locationId}/items/{itemId}/relocate")
    public String relocateItem(@PathVariable Long householdId,
                               @PathVariable Long locationId,
                               @PathVariable Long itemId,
                               @ModelAttribute ItemRelocationDTO itemDto,
                               Model model) {
        log.debug("Relocating item with id - {}  in location  with id - {} of household with id - {}", itemId, locationId, householdId);
        Household household = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Item item = itemService.relocateItem(itemDto);
        log.info("Item with id - {} has been relocated to  location with id -{} of household with id - {}", itemId, locationId, householdId);
        Location oldLocation = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id ={} does not exist", locationId);
                    return new NonExistentEntityException(LOCATION_WITH_ID + locationId + DOES_NOT_EXIST);
                });

        model.addAttribute(HOUSEHOLD_ATTR, household);
        model.addAttribute(ITEM_ATTR, item);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(MAIN_LOCATION_ATTR, oldLocation);
        model.addAttribute(AVAILABLE_SUB_LOCATIONS_ATTR, oldLocation.getSubLocations());

        return "redirect:/household/{householdId}/locations/{locationId}";
    }

    @GetMapping("/{householdId}/category/{categoryId}/items/{itemId}/relocate")
    public String itemRelocationPageRenderFromCategory(@PathVariable Long householdId,
                                                       @PathVariable Long categoryId,
                                                       @PathVariable Long itemId,
                                                       Model model) {
        List<Location> availableLocations = locationService.findLocationsInHousehold(householdId);
        List<Category> availableCategories = categoryService.findwithHousehold(householdId);

        ItemRelocationDTO itemRelocationDto = new ItemRelocationDTO(itemId);

        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("locations", availableLocations);
        model.addAttribute(CATEGORIES_ATTR, availableCategories);
        model.addAttribute(ITEM_DTO_ATTR, itemRelocationDto);


        return "items/edit/relocationItemFromCategory";
    }

    @PostMapping("/{householdId}/category/{categoryId}/items/{itemId}/relocate")
    public String relocateItemFromCategory(@PathVariable Long householdId,
                                           @PathVariable Long categoryId,
                                           @PathVariable Long itemId,
                                           @ModelAttribute ItemRelocationDTO itemDto,
                                           Model model) {
        log.debug("Relocating item with id - {}  of category  with id - {} of household with id - {}", itemId, categoryId, householdId);

        Item item = itemService.relocateItem(itemDto);
        log.info("Item with id - {} has been Relocated to  category with id -{} of household with id - {}", itemId, categoryId, householdId);
        Location oldLocation = locationService.findLocationById(item.getLocation().getId())
                .orElseThrow(() -> {
                    log.error("Location with id - {} does not exist", item.getLocation().getId());
                    return new NonExistentEntityException(LOCATION_WITH_ID + item.getLocation().getId() + DOES_NOT_EXIST);
                });
        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });


        Category oldCategory = categoryService.findCategoryById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with id - {} does not exist", categoryId);
                    return new NonExistentEntityException(CATEGORY_WITH_ID + categoryId + DOES_NOT_EXIST);
                });

        model.addAttribute("oldLocation", oldLocation);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(HOUSEHOLD_ATTR, houseHold);
        model.addAttribute("mainCategory", oldCategory);
        model.addAttribute("availableSubCategories", oldCategory.getSubCategory());
        return "redirect:/household/{householdId}/category/{categoryId}";
    }

    private void setEditingPage(Long householdId,
                                Long locationId,
                                Long itemId,
                                Model model) {
        Location location = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id - {} does not exist ", locationId);
                    return new NonExistentEntityException(LOCATION_WITH_ID + locationId + DOES_NOT_EXIST);
                });
        Item item = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist ", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });

        List<Category> categories = categoryService.findwithHousehold(householdId);
        model.addAttribute(CATEGORIES_ATTR, categories);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(LOCATION_ATTR, location);
        model.addAttribute(ITEM_ATTR, item);
    }

    private void setEditingPageFromCategory(Long householdId,
                                            Long categoryId,
                                            Long itemId,
                                            Model model) {
        Category category = categoryService.findCategoryById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category with id - {} does not exist ", categoryId);
                    return new NonExistentEntityException(CATEGORY_WITH_ID + categoryId + DOES_NOT_EXIST);
                });
        Item item = itemService.findItemById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id - {} does not exist ", itemId);
                    return new NonExistentEntityException(ITEM_WITH_ID + itemId + DOES_NOT_EXIST);
                });

        List<Category> categories = categoryService.findwithHousehold(householdId);
        model.addAttribute(CATEGORIES_ATTR, categories);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(CATEGORY_ATTR, category);
        model.addAttribute(ITEM_ATTR, item);
    }

    private void setCategoryAndExpirationError(Model model, ItemCreationDTO item) {
        log.error("Category Error - category cannot be empty");
        if (Objects.isNull(item.getCategory())) {
            model.addAttribute("categoryError", "Category cannot be empty");

        }
    }

}
