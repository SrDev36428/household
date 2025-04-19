package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.category.CategoryCreationDTO;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.datamodel.entity.item.ItemFilterDTO;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.service.AuthorizationService;
import cz.cvut.fit.household.service.interfaces.CategoryService;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.ItemService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@RunWith(SpringRunner.class)
@WebMvcTest(SubCategoryController.class)
@ContextConfiguration
public class SubCategoryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;
    @MockBean
    private ItemService itemService;
    @MockBean
    private HouseHoldService houseHoldService;
    @MockBean
    private AuthorizationService authorizationService;

    User user1 = new User("user1","1","User","1","user1@gmail.com",new ArrayList<>());
    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(),new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    Category category = new Category(1L, household1, new ArrayList<>(), null, new ArrayList<>(), "category1", "category1 description");
    List<Category> category1 = Collections.singletonList(category);
    CategoryCreationDTO categoryCreationDTO = new CategoryCreationDTO("subcategory1", "subcategory1 description");
    Membership membership1 = new Membership(1L, MembershipStatus.ACTIVE, MembershipRole.OWNER,user1,household1);
    Category subCategory = new Category(3L, household1, new ArrayList<>(), category, new ArrayList<>(), "subcategory1", "subcategory1 description");
    List<Category> subCategory1 = Collections.singletonList(subCategory);
    ArrayList<Item>list = new ArrayList<>();
    Page<Item> itemPage = new PageImpl<Item>(list);

    @Before
    public void setUp() throws Exception {
        user1.setMemberships(Collections.singletonList(membership1));
        household1.setCategory(category1);
        household1.setMemberships(Collections.singletonList(membership1));
        category.setSubCategory(subCategory1);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        when(categoryService.findCategoryById(category.getId())).thenReturn(Optional.ofNullable(category));
        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(Optional.ofNullable(household1));
        when(categoryService.addCategory(categoryCreationDTO, null, category)).thenReturn(subCategory);

    }

    @Test
    public void addSubcategoryRejected() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/category/{categoryId}/subcategory/add", household1.getId(), category.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .flashAttr("category", categoryCreationDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("category/subCategory/addSubcategory"));
    }
    @Test
    public void addSubcategorySuccessful() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/category/{categoryId}/subcategory/add", household1.getId(), category.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .flashAttr("subCategory", categoryCreationDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/household/" + household1.getId() + "/category/" + category.getId()));

        verify(categoryService,times(1)).addCategory(categoryCreationDTO, null, category);

    }

    @Test
    public void getSubcategoryView() throws Exception{
        ItemFilterDTO itemFilterDTO = new ItemFilterDTO();
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/{categoryId}/subcategory/view", household1.getId(), category.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .flashAttr("category", category).flashAttr("items",itemPage).flashAttr("filter",itemFilterDTO))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("category/categoryDetails"));

    }

    @Test
    public void renderAddSubcategoryPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/{categoryId}/subcategory/add", household1.getId(), category.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .flashAttr("category", categoryCreationDTO))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("category/subCategory/addSubcategory"));
    }

    @Test
    public void deleteSubcategory() throws Exception{
        ItemFilterDTO itemFilterDTO = new ItemFilterDTO();
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/{categoryId}/subcategory/{subcategoryId}/delete", household1.getId(), category.getId(), subCategory.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .flashAttr("mainCategory", category).flashAttr("items",itemPage).flashAttr("filter",itemFilterDTO))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("category/categoryDetails"));
        verify(categoryService,times(1)).deleteCategoryById(subCategory.getId());
    }
}