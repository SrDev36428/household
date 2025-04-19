package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.ItemDAO;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemDAOImpl implements ItemDAO {

    private final ItemRepository itemRepository;

    @Override
    public Item saveItem (Item item) {
        return itemRepository.save(item);
    }

    @Override
    public List<Item> findAllItems() {
        return itemRepository.findAll();
    }

    @Override
    public Optional<Item> findItemById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public void deleteItemById(Long id) {
        itemRepository.deleteById(id);
    }

    @Override
    public List<Item> findItemsByHouseholdId(Long householdId) {
        return itemRepository.findAllItemsFromHousehold(householdId);
    }
}
