package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.interfaces.ItemDAO;
import cz.cvut.fit.household.daos.interfaces.LocationDAO;
import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.ItemCreationDTO;
import cz.cvut.fit.household.datamodel.entity.item.ItemFilterDTO;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.datamodel.entity.item.ItemRelocationDTO;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final LocationDAO locationDAO;
    private final ItemDAO itemDAO;


    private static final String ITEM_WITH_ID = "Item With id: ";
    private static final String DOES_NOT_EXIST = " doesn't exist";

    @Override
    public Item addItem(ItemCreationDTO itemDto, Location location) {
        Item item = new Item(itemDto);
        item.setLocation(location);
        item.setCategory(itemDto.getCategory());
        return itemDAO.saveItem(item);
    }

    @Override
    public Item updateItem(ItemCreationDTO updatedItem, Long id) {
        Item item = findItemById(id)
                .orElseThrow(() -> new NonExistentEntityException(ITEM_WITH_ID + id + DOES_NOT_EXIST));

        item.setTitle(updatedItem.getTitle());
        item.setDescription(updatedItem.getDescription());
        item.setExpiration(updatedItem.getExpiration());
        item.setQuantityType(updatedItem.getQuantityType());
        item.setMaxQuantity(Double.valueOf(updatedItem.getMaxQuantity()));
        item.setCurrentQuantity(Double.valueOf(updatedItem.getCurrentQuantity()));
        item.setCategory(updatedItem.getCategory());

        return itemDAO.saveItem(item);
    }

    @Override
    public Item increaseItemQuantity(String increaseQuantityText, Long id) {
        Item item = findItemById(id)
                .orElseThrow(() -> new NonExistentEntityException(ITEM_WITH_ID + id + DOES_NOT_EXIST));

        double result = Double.parseDouble(increaseQuantityText) + item.getCurrentQuantity();

        if(result > item.getMaxQuantity())
            throw new IllegalArgumentException("Quantity should not be more than maximal quantity");
        item.setCurrentQuantity(result);

        return itemDAO.saveItem(item);
    }

    @Override
    public Item decreaseItemQuantity(String decreaseQuantityText, Long id) {
        Item item = findItemById(id)
                .orElseThrow(() -> new NonExistentEntityException(ITEM_WITH_ID + id + DOES_NOT_EXIST));

        double result = item.getCurrentQuantity() - Double.parseDouble(decreaseQuantityText);

        if(result < 0 || Double.parseDouble(decreaseQuantityText) < 0 )
            throw new IllegalArgumentException("Quantity should not be less than 0");
        item.setCurrentQuantity(result);

        return itemDAO.saveItem(item);
    }

    @Override
    public Item relocateItem(ItemRelocationDTO itemRelocationDto) {
        Item item = findItemById(itemRelocationDto.getItemId())
                .orElseThrow(() -> new NonExistentEntityException(ITEM_WITH_ID + itemRelocationDto.getItemId() + DOES_NOT_EXIST));
        Location location = locationDAO.findLocationById(itemRelocationDto.getLocationId())
                .orElseThrow(() -> new NonExistentEntityException(ITEM_WITH_ID + itemRelocationDto.getLocationId() + DOES_NOT_EXIST));

        item.setLocation(location);

        return itemDAO.saveItem(item);
    }

    @Override
    public List<Item> findItemsByLocation(Location location) {
        List<Item> items = location.getItems();
        List<Location> children = location.getSubLocations();

        for (Location childLocation : children) {
            items.addAll(findItemsByLocation(childLocation));
        }

        return sortItemList(items);
    }

    private boolean isCategoryOrSubcategory(Category category, Category filterCategory) {
        if (category == filterCategory || category.getTitle().equals(filterCategory.getTitle())) {
            return true;
        }
        for (Category subcategory : filterCategory.getSubCategory()) {
            if (isCategoryOrSubcategory(category, subcategory)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLocationOrSubLocation(Location location, Location filterLocation) {
        if (location == filterLocation || location.getTitle().equals(filterLocation.getTitle())) {
            return true;
        }
        for (Location subLocation : filterLocation.getSubLocations()) {
            if (isLocationOrSubLocation(location, subLocation)) {
                return true;
            }
        }
        return false;

    }

    private List<Item> filterItemList(List<Item> items,ItemFilterDTO itemFilter) {
       return items.stream()
                .filter(item -> (itemFilter.getTitle() == null || item.getTitle().startsWith(itemFilter.getTitle())))
                .filter(item -> (itemFilter.getCategory() == null || isCategoryOrSubcategory(item.getCategory(), itemFilter.getCategory())))
                .filter(item -> (itemFilter.getLocation() == null || isLocationOrSubLocation(item.getLocation(), itemFilter.getLocation())))
                .filter(item -> (itemFilter.getQuantityType()==null || item.getQuantityType().equals(itemFilter.getQuantityType())))
                .filter(item -> (itemFilter.getExpirationMin()==null || item.getExpiration()==null ||!item.getExpiration().before(itemFilter.getExpirationMin())))
                .filter(item -> (itemFilter.getExpirationMax()==null || item.getExpiration()==null  || !item.getExpiration().after(itemFilter.getExpirationMax())))
                .filter(item -> (itemFilter.getCurrentQuantityMin() == null || item.getCurrentQuantity() >= itemFilter.getCurrentQuantityMin()))
                .filter(item -> (itemFilter.getCurrentQuantityMax() == null || item.getCurrentQuantity() <= itemFilter.getCurrentQuantityMax()))
                .filter(item -> (itemFilter.getMaxQuantityMin() == null || item.getMaxQuantity() >= itemFilter.getMaxQuantityMin()))
                .filter(item -> (itemFilter.getMaxQuantityMax() == null || item.getMaxQuantity() <= itemFilter.getMaxQuantityMax()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findAllItemsByHouseholdFiltered(Household household, ItemFilterDTO filterDTO) {
        List<Item> items = itemDAO.findItemsByHouseholdId(household.getId());
        return filterItemList(items,filterDTO);
    }

    @Override
    public List<Item> findItemsByLocationFiltered(Location location, ItemFilterDTO itemFilter) {
        List<Item> items = location.getItems();
        List<Location> children = location.getSubLocations();

        for (Location childLocation : children) {
            items.addAll(findItemsByLocation(childLocation));
        }

        return filterItemList(items, itemFilter);
    }

    @Override
    public Double getHighestMaxQuantity() {
        Double maxMaxQuantity = 0.0;
        List<Item>items = findItems();
        for(Item item : items){
            if(item.getMaxQuantity()>maxMaxQuantity){
                maxMaxQuantity = item.getMaxQuantity();
            }
        }
        return maxMaxQuantity;
    }

    @Override
    public Double getHighestCurrentQuantity() {
        Double maxCurrentQuantity = 0.0;
        List<Item>items = findItems();
        for(Item item : items){
            if(item.getCurrentQuantity()>maxCurrentQuantity){
                maxCurrentQuantity = item.getCurrentQuantity();
            }
        }
        return maxCurrentQuantity;
    }

    @Override
    public List<Item> findItemsByCategory(Category category) {
        List<Item> items = category.getItems();
        List<Category> children = category.getSubCategory();

        for (Category childCategory : children) {
            items.addAll(findItemsByCategory(childCategory));
        }

        return sortItemList(items);
    }

    @Override
    public List<Item> findItemsByCategoryFiltered(Category category, ItemFilterDTO itemFilter) {
        List<Item> items = category.getItems();
        List<Category> children = category.getSubCategory();

        for (Category childCategory : children) {
            items.addAll(findItemsByCategory(childCategory));
        }

        return filterItemList(items,itemFilter);
    }

    public List<Item> sortItemList(List<Item> items) {
        items.sort((i1, i2) -> {
            if (i1.getExpiration() == null && i2.getExpiration() == null) {
                return 0; // Both dates are null, consider them equal
            } else if (i1.getExpiration() == null) {
                return -1; // i1 has a null expiration, consider it earlier
            } else if (i2.getExpiration() == null) {
                return 1; // i2 has a null expiration, consider it earlier
            } else {
                return i1.getExpiration().compareTo(i2.getExpiration());
            }
        });

        return items;
    }

    @Override
    public List<Item> findItems() {
        return itemDAO.findAllItems();
    }

    @Override
    public Optional<Item> findItemById(Long id) {
        return itemDAO.findItemById(id);
    }

    @Override
    public void createMultipleItems(ItemCreationDTO item, Location location, Integer multipleCreation) {
        for (int i = 0; i < multipleCreation; i++) {
            addItem(item, location);
        }
    }

    @Override
    public void deleteItemById(Long id) {
        itemDAO.deleteItemById(id);
    }
}
