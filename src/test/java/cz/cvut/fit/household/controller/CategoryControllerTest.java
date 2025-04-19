package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.controller.controllerUtil.ControllerItemDisplayUtil;
import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.category.CategoryCreationDTO;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.service.AuthorizationService;
import cz.cvut.fit.household.service.interfaces.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(CategoryController.class)
@ContextConfiguration
public class CategoryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HouseHoldService houseHoldService;


    @MockBean
    private CategoryService categoryService;

    @MockBean
    private AuthorizationService authorizationService;

    @MockBean
    private UserService userService;

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private LocationService locationService;

    @MockBean
    private ControllerItemDisplayUtil displayUtil;

    User user1 = new User("user1","1","User","1","user1@gmail.com",new ArrayList<>());
    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(),new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    Category category = new Category(1L, household1, new ArrayList<>(), null, new ArrayList<>(), "category1", "category1 description");
    List<Category> category1 = Collections.singletonList(category);
    CategoryCreationDTO categoryCreationDTO = new CategoryCreationDTO("category1", "category1 description");
    Membership membership1 = new Membership(1L, MembershipStatus.ACTIVE, MembershipRole.OWNER,user1,household1);
    Category subCategory = new Category(3L, household1, new ArrayList<>(), category, new ArrayList<>(), "subcategory1", "subcategory1 description");
    List<Category> subCategory1 = Collections.singletonList(subCategory);

    @Before
    public void setUp() {
        user1.setMemberships(Collections.singletonList(membership1));
        household1.setCategory(category1);
        household1.setMemberships(Collections.singletonList(membership1));
        category.setSubCategory(subCategory1);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        when(userService.findUserByUsername(user1.getUsername())).thenReturn(java.util.Optional.ofNullable(user1));
        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(java.util.Optional.ofNullable(household1));
        when(authorizationService.isOwner(household1)).thenReturn(true);
        when(categoryService.findCategoryById(category.getId())).thenReturn(java.util.Optional.ofNullable(category));
        when(membershipService.findAllMemberships()).thenReturn(Collections.singletonList(membership1));
        when(itemService.findItems()).thenReturn(null);
    }

    @Test
    public void renderAddCategoryPage() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/add", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("category/addCategory"));
    }

    @Test
    public void renderCategoryInfoPage() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/{categoryId}", household1.getId(), category.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("category/categoryDetails"));
        verify(houseHoldService,times(1)).findHouseHoldById(household1.getId());
    }

    @Test
    public void renderCategoryPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category", household1.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword())))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("category/householdCategory"));

        verify(houseHoldService,times(1)).findHouseHoldById(household1.getId());
    }

    @Test
    public void addCategory() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/category/add", household1.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .flashAttr("category", categoryCreationDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/category"));

        verify(houseHoldService,times(1)).findHouseHoldById(household1.getId());
        verify(categoryService,times(1)).addCategory(categoryCreationDTO, household1, null);
    }

    @Test
    public void deleteCategory() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/{categoryId}/delete", household1.getId(),category.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        verify(categoryService,times(1)).deleteCategoryById(category.getId());
    }

    @Test
    public void returnToMainCategory() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/{categoryId}/return", household1.getId(),category.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        verify(categoryService,times(1)).findCategoryById(category.getId());
    }

    @Test
    public void renderEditingPage() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/category/{categoryId}/edit", household1.getId(),category.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("category/editCategory/categoryEdit"));

        verify(categoryService,times(1)).findCategoryById(category.getId());
    }

    @Test
    public void performEditing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/category/{categoryId}/edit", household1.getId(),category.getId())
                        .flashAttr("updatedLocation", categoryCreationDTO))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        verify(categoryService,times(1)).updateCategory(eq(category.getId()),any(CategoryCreationDTO.class));
    }
}