package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.controller.controllerUtil.ControllerItemDisplayUtil;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.ItemFilterDTO;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.datamodel.entity.location.LocationCreationDTO;
import cz.cvut.fit.household.datamodel.enums.QuantityType;
import cz.cvut.fit.household.exception.NonExistentEntityException;
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
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/household")
@RequiredArgsConstructor
@Slf4j
public class LocationsController {

    private final HouseHoldService houseHoldService;
    private final LocationService locationService;
    private final ItemService itemService;
    private final CategoryService categoryService;
    private final ControllerItemDisplayUtil itemDisplayUtil;

    private static final String HOUSEHOLD_NAME = "household";
    private static final String HOUSEHOLD_ID_ATTR = "householdId";
    private static final String LOCATION_ATTR = "location";
    private static final String ITEMS_ATTR = "items";
    private static final String REDIRECTION_HOUSEHOLD = "redirect:/household/";
    private static final String MAIN_LOCATION_ATTR = "mainLocation";
    private static final String AVAILABLE_SUB_LOCATIONS_ATTR = "availableSubLocations";
    private static final String HOUSEHOLD_WITH_ID = "Household With id: ";
    private static final String DOES_NOT_EXIST = " doesn't exist";
    private static final String RETURN_LOCATION_DETAILS = "locations/locationDetail";


    @GetMapping("/{householdId}/locations/add")
    public String renderAddLocationPage(@PathVariable Long householdId,
                                        Model model) {
        LocationCreationDTO location = new LocationCreationDTO();
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(LOCATION_ATTR, location);
        return "locations/addLocation";
    }

    @GetMapping("/{householdId}/locations/{locationId}")
    public String renderLocationInfoPage(@RequestParam(value = "sort", defaultValue = "title") String sort,
                                         @PathVariable Long householdId,
                                         @PathVariable Long locationId,
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
        log.debug("Finding location with id -{} in household with id - {} ", locationId, householdId);

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

        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Location location = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id - {} does not exist", locationId);
                    return new NonExistentEntityException("Location with id: " + locationId + DOES_NOT_EXIST);
                });

        List<Item> items = itemService.findItemsByLocationFiltered(location, finalFilter);

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

        model.addAttribute(HOUSEHOLD_NAME, houseHold);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(MAIN_LOCATION_ATTR, location);
        model.addAttribute(AVAILABLE_SUB_LOCATIONS_ATTR, locationService.findAllSubLocations(location));
        model.addAttribute("sort", sort);
        model.addAttribute("pageNumber", currentPage);
        model.addAttribute("categories", categoryService.findwithHousehold(householdId));
        model.addAttribute("locations", locationService.findLocationsInHousehold(householdId));
        model.addAttribute(ITEMS_ATTR, itemPage);
        model.addAttribute("queryParams", itemDisplayUtil.buildQueryParams(finalFilter));
        model.addAttribute("filter", finalFilter);

        return "locations/locationDetail";
    }

    @GetMapping("/{householdId}/locations")
    public String renderLocationsPage(@PathVariable Long householdId, Model model) {
        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST));

        model.addAttribute(HOUSEHOLD_NAME, houseHold);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute("availableLocations", houseHold.getLocations());
        return "locations/householdLocations";
    }

    @PostMapping("/{householdId}/locations/add")
    public String addLocation(@PathVariable Long householdId,
                              @Valid @ModelAttribute("location") LocationCreationDTO location,
                              BindingResult result,
                              Model model) {
        log.debug("Creating a new location - {} in household with id - {}", location, householdId);
        if (result.hasErrors()) {
            log.error(result.getAllErrors().toString());
            return "locations/addLocation";
        }

        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        locationService.addLocation(location, houseHold, null);
        log.info("Location  - {} has been created in household with id - {} ", location, householdId);

        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute("availableLocations", houseHold.getLocations());
        return "redirect:/household/{householdId}/locations";
    }

    @GetMapping("/{householdId}/locations/{locationId}/delete")
    public RedirectView deleteLocation(@PathVariable Long locationId,
                                       @PathVariable String householdId) {

        log.debug("Deleting location with id - {} in household with id - {}", locationId, householdId);
        Location location = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id - {} does not exist", locationId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        if (location.getMainLocation() == null) {
            locationService.deleteLocationById(location.getId());
            return new RedirectView("/household/" + householdId + "/locations");
        }

        locationService.deleteLocationById(location.getId());
        log.info("Location with id - {} has been deleted in household with id  - {} ", locationId, householdId);
        return new RedirectView("/household/" + householdId + "/" + LOCATION_ATTR + "s/" + location.getMainLocation().getId());
    }

    @GetMapping("/{householdId}/locations/{locationId}/return")
    public ModelAndView returnToMainLocation(@PathVariable Long householdId,
                                             @PathVariable Long locationId,
                                             Model model) {


        Optional<Location> location = locationService.findLocationById(locationId);

        if (!location.isPresent() || location.get().getMainLocation() == null) {
            return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/locations", (Map<String, ?>) model);
        }

        return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/locations/" +
                location.get().getMainLocation().getId(), (Map<String, ?>) model);
    }

    @GetMapping("/{householdId}/locations/{locationId}/edit")
    public String renderEditingPage(@PathVariable Long householdId,
                                    @PathVariable Long locationId,
                                    Model model) {

        Location location = locationService.findLocationById(locationId)
                .orElseThrow(() -> {
                    log.error("Location with id - {} does not exist", locationId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        LocationCreationDTO newLocation = new LocationCreationDTO();

        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(LOCATION_ATTR, location);
        model.addAttribute("newLocation", newLocation);

        return "locations/edit/locationEdit";
    }

    @PostMapping("/{householdId}/locations/{locationId}/edit")
    public ModelAndView performEditing(@PathVariable Long householdId,
                                       @PathVariable Long locationId,
                                       @ModelAttribute LocationCreationDTO updatedLocation,
                                       Model model) {
        log.debug("Editing location with id - {} in household with id - {} with updated location - {}", locationId, householdId, updatedLocation);
        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id - {} does not exist", householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        Location location = locationService.updateLocation(locationId, updatedLocation);
        log.info("Location with id -{} in household with id - {} has been updated as - {}", locationId, householdId, location);

        model.addAttribute("household", houseHold);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(LOCATION_ATTR, location);

        return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/locations/" + locationId + "/return", (Map<String, ?>) model);
    }

}
