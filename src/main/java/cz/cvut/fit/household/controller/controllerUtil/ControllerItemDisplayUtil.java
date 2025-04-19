package cz.cvut.fit.household.controller.controllerUtil;

import cz.cvut.fit.household.datamodel.entity.item.ItemFilterDTO;
import cz.cvut.fit.household.datamodel.enums.QuantityType;
import cz.cvut.fit.household.service.interfaces.CategoryService;
import cz.cvut.fit.household.service.interfaces.ItemService;
import cz.cvut.fit.household.service.interfaces.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ControllerItemDisplayUtil {

    private final  SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    private final LocationService locationService;
    private final CategoryService categoryService;
    private final ItemService itemService;

    public String buildQueryParams(ItemFilterDTO filter) {
        StringBuilder params = new StringBuilder();

        if (filter.getTitle() != null) {
            params.append("&title=").append(filter.getTitle());
        }
        if (filter.getLocation() != null) {
            params.append("&location=").append(filter.getLocation().getId());
        }
        if (filter.getQuantityType() != null) {
            params.append("&quantityType=").append(filter.getQuantityType());
        }
        if (filter.getMaxQuantityMin() != null) {
            params.append("&maxQuantityMin=").append(filter.getMaxQuantityMin());
        }
        if (filter.getMaxQuantityMax() != null) {
            params.append("&maxQuantityMax=").append(filter.getMaxQuantityMax());
        }
        if (filter.getCurrentQuantityMin() != null) {
            params.append("&currentQuantityMin=").append(filter.getCurrentQuantityMin());
        }
        if (filter.getCurrentQuantityMax() != null) {
            params.append("&currentQuantityMax=").append(filter.getCurrentQuantityMax());
        }
        if (filter.getCategory() != null) {
            params.append("&category=").append(filter.getCategory().getId());
        }
        if (filter.getExpirationMin() != null) {
            params.append("&dateMin=").append(filter.getExpirationMin());
        }
        if (filter.getExpirationMax() != null) {
            params.append("&dateMax=").append(filter.getExpirationMax());
        }

        return params.toString();
    }

    public void setUpFilter(ItemFilterDTO finalFilter,
                             Optional<String> title,
                             Optional<String> filterCategory,
                             Optional<String> filterLocation,
                             Optional<String> quantityType,
                             Optional<String> maxQuantityMin,
                             Optional<String> maxQuantityMax,
                             Optional<String> currentQuantityMin,
                             Optional<String> currentQuantityMax,
                             Optional<String> dateMin,
                             Optional<String> dateMax) {
        if (title.isPresent()) {
            if (!title.get().isEmpty()) {
                finalFilter.setTitle(title.get());
            } else {
                finalFilter.setTitle(null);
            }
        }
        if (quantityType.isPresent()) {
            if (!quantityType.get().isEmpty()) {
                finalFilter.setQuantityType(QuantityType.valueOf(quantityType.get()));
            }
        }
        if (maxQuantityMin.isPresent()) {
            if (!maxQuantityMin.get().isEmpty()) {
                finalFilter.setMaxQuantityMin(Double.parseDouble(maxQuantityMin.get()));
            } else {
                finalFilter.setMaxQuantityMin(0.0);
            }
        }
        if (maxQuantityMax.isPresent()) {
            if (!maxQuantityMax.get().isEmpty()) {
                finalFilter.setMaxQuantityMax(Double.parseDouble(maxQuantityMax.get()));
            } else {
                finalFilter.setMaxQuantityMax(itemService.getHighestMaxQuantity());
            }
        }
        if (currentQuantityMin.isPresent()) {
            if (!currentQuantityMin.get().isEmpty()) {
                finalFilter.setCurrentQuantityMin(Double.parseDouble(currentQuantityMin.get()));
            } else {
                finalFilter.setCurrentQuantityMin(0.0);
            }
        }
        if (currentQuantityMax.isPresent()) {
            if (!currentQuantityMax.get().isEmpty()) {
                finalFilter.setCurrentQuantityMax(Double.parseDouble(currentQuantityMax.get()));
            } else {
                finalFilter.setCurrentQuantityMax(itemService.getHighestCurrentQuantity());
            }
        }
        if (filterLocation.isPresent()) {
            if (!filterLocation.get().isEmpty()) {
                finalFilter.setLocation(locationService.findLocationById(Long.parseLong(filterLocation.get())).orElse(null));
            } else {
                finalFilter.setLocation(null);
            }
        }
        if (filterCategory.isPresent()) {
            if (!filterCategory.get().isEmpty()) {
                finalFilter.setCategory(categoryService.getCategoryById(Long.parseLong(filterCategory.get())));
            } else {
                finalFilter.setCategory(null);
            }
        }
        if (dateMin.isPresent()) {
            try {
                if (!dateMin.get().isEmpty()) {
                    finalFilter.setExpirationMin(formatter.parse(dateMin.get()));
                } else {
                    finalFilter.setExpirationMin(null);
                }
            } catch (ParseException e) {
                log.error("Could not parse minimum expiration date {}", dateMin.get(), e);
            }
        }
        if (dateMax.isPresent()) {
            try {
                if (!dateMax.get().isEmpty()) {
                    finalFilter.setExpirationMax(formatter.parse(dateMax.get()));
                } else {
                    finalFilter.setExpirationMax(null);
                }
            } catch (ParseException e) {
                log.error("Could not parse maximum expiration date {}", dateMax.get(), e);
            }
        }
    }
}
