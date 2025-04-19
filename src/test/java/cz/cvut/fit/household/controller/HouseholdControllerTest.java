package cz.cvut.fit.household.controller;

import java.util.ArrayList;
import java.util.Collections;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.HouseholdCreationDTO;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.service.AuthorizationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.MembershipService;
import cz.cvut.fit.household.service.interfaces.UserService;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@WebMvcTest(HouseholdController.class)
@ContextConfiguration
public class HouseholdControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;



    private MockMvc mockMvc;

    @MockBean
    private HouseHoldService houseHoldService;

    @MockBean
    private UserService userService;

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private Model model;

    @MockBean
    AuthorizationService authorizationService;

    User user1 = new User("user1","1","User","1","user1@gmail.com",new ArrayList<>());
    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(),null, null, null);
    HouseholdCreationDTO householdCreationDTO = new HouseholdCreationDTO("user1 household", "");
    Membership membership1 = new Membership(1L, MembershipStatus.ACTIVE, MembershipRole.OWNER,user1,household1);
    @Before
    public void setup() {
        user1.setMemberships(Collections.singletonList(membership1));
        household1.setMemberships(Collections.singletonList(membership1));
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        when(userService.findUserByUsername(user1.getUsername())).thenReturn(java.util.Optional.ofNullable(user1));
        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(java.util.Optional.ofNullable(household1));
        when(houseHoldService.updateHousehold(householdCreationDTO,household1.getId())).thenReturn(household1);
        when(authorizationService.isOwner(household1)).thenReturn(true);
        when(membershipService.findAllMemberships()).thenReturn(Collections.singletonList(membership1));
    }

    @Test
    public void houseHoldAddTest() throws Exception {
        mockMvc.perform(get("/households/add")
                        .with(user(user1.getUsername()).password(user1.getPassword())))
                .andDo(
                        MockMvcResultHandlers.print()
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("household/addHousehold"));
    }

    @Test
    public void createHousehold() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user1.getUsername(),"password");
        mockMvc.perform(post("/households/add")
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .with(csrf())
                        .flashAttr("houseHold", householdCreationDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));
    }

    @Test
    public void renderHouseholdEditPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/edit",household1.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword())))
                .andExpect(MockMvcResultMatchers.status().isOk());
//                .andExpect(view().name("household/edit/householdEdit"));
    }


    @Test
    public void editHousehold() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/edit",household1.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .with(csrf())
                        .flashAttr("household",householdCreationDTO))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("household/edit/householdEdit"));
    }









}
