package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.controller.controllerUtil.ControllerItemDisplayUtil;
import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.ItemCreationDTO;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.datamodel.enums.QuantityType;
import cz.cvut.fit.household.service.interfaces.CategoryService;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.ItemService;
import cz.cvut.fit.household.service.interfaces.LocationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(ItemsController.class)
@ContextConfiguration
public class ItemsControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private LocationService locationService;

    @MockBean
    private HouseHoldService houseHoldService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private ControllerItemDisplayUtil itemDisplayUtil;

    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    Category category1 = new Category(3L, household1, new ArrayList<>(), null, new ArrayList<>(), "newCategory", "nothing");
    List<Category> category = Collections.singletonList(category1);
    Category subcategory1 = new Category(4L, household1, new ArrayList<>(), category1, new ArrayList<>(), "newSubcategory", "nothing");
    List<Category> subcategory = Collections.singletonList(subcategory1);

    Item item = new Item(1L,subcategory1,null,"item","description",QuantityType.KILOGRAM, 10d, 0d, Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
    ItemCreationDTO itemCreationDto= new ItemCreationDTO(item.getTitle(),item.getDescription(),item.getCategory(),item.getQuantityType(),item.getMaxQuantity().toString(),item.getCurrentQuantity().toString(),item.getExpiration());
    Location location = new Location(1L,household1, Collections.singletonList(item),null,new ArrayList<>(),"location_1","description");

    @Before
    public void setup() {
        household1.setCategory(category);
        category1.setSubCategory(subcategory);
        item.setCategory(subcategory1);
        item.setLocation(location);
        household1.setLocations(Collections.singletonList(location));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(java.util.Optional.ofNullable(household1));
        when(locationService.findLocationById(location.getId())).thenReturn(java.util.Optional.ofNullable(location));
        when(categoryService.findCategoryById(subcategory1.getId())).thenReturn(Optional.ofNullable(subcategory1));
        when(itemService.findItemById(item.getId())).thenReturn(Optional.of(item));

    }

    @Test
    public void renderAddItemPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/items/add", household1.getId(),location.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("items/add-item"));
    }

    @Test
    public void renderItemsListTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/items", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("items/ItemsView"));
    }

    @Test
    public void renderLocationItemsList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/items", household1.getId(),location.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }

    @Test
    public void addItemTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/locations/{locationId}/items", household1.getId(),location.getId())
                        .flashAttr("item",itemCreationDto)
                        .flashAttr("categories", subcategory1)
                        .param("multipleCreation", "1"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/locations/" + location.getId() + "/"));
    }

    @Test
    public void deleteItem() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/items/{itemId}/delete", household1.getId(),location.getId(), item.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/locations/" + location.getId()));
        verify(itemService, times(1)).deleteItemById(item.getId());
    }

    @Test
    public void deleteItemFromCategory() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/{categoryId}/items/{itemId}/delete", household1.getId(),subcategory1.getId(), item.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/category/" + subcategory1.getId()));
        verify(itemService, times(1)).deleteItemById(item.getId());

    }

    @Test
    public void renderEditingPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/items/{itemId}/edit", household1.getId(),location.getId(), item.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("items/edit/itemEdit"));

    }

    @Test
    public void renderEditingPageFromCategory() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/{categoryId}/items/{itemId}/edit", household1.getId(),subcategory1.getId(), item.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("items/edit/itemEditFromCategory"));

    }
}
