package cz.cvut.fit.household.controller;

import java.util.ArrayList;
import java.util.Collections;

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

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.MembershipService;
import cz.cvut.fit.household.service.interfaces.UserService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(MembersController.class)
@ContextConfiguration
public class MembersControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;


    private MockMvc mockMvc;

    @MockBean
    private HouseHoldService houseHoldService;

    @MockBean
    private UserService userService;

    @MockBean
    private MembershipService membershipService;

    User user1 = new User("user1","1","User","1","user1@gmail.com",new ArrayList<>());
    User user2 = new User("user2","2","User","2","user2@gmail.com",new ArrayList<>());
    User userNotExisted = new User("user_not_exists",null,null,null,null,null);
    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(),null, null, null);
    Membership user1Membership= new Membership(1L, MembershipStatus.PENDING, MembershipRole.REGULAR, user1,household1);


    @Before
    public void setup() {
        user1.setMemberships(Collections.singletonList(user1Membership));
        household1.setMemberships(Collections.singletonList(user1Membership));
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        when(userService.findUserByUsername(user1.getUsername())).thenReturn(java.util.Optional.ofNullable(user1));
        when(userService.findUserByUsername(user2.getUsername())).thenReturn(java.util.Optional.ofNullable(user2));
        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(java.util.Optional.ofNullable(household1));
        when(userService.findUserByUsername(userNotExisted.getUsername())).thenThrow(RuntimeException.class);
        //when(membershipService.declineInvitation(user1Membership.getId());
        //when(authentication.getName()).thenReturn(user1.getUsername());
        //when(membershipService.acceptInvitation(user1Membership.getId())).thenAnswer(user1Membership.setStatus(MembershipStatus.ACTIVE));
    }

    @Test
    public void renderMembersPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/1/members")
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .flashAttr("household", household1))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(view().name("members/householdMembers"));
    }

    @Test
    public void renderInviteUserPage() throws Exception {
        mockMvc.perform(post("/household/1/invite")
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .with(csrf())
                        .param("username", user2.getUsername()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(view().name("members/inviteMember"));

    }

    @Test
    public void leaveHouseHold() throws Exception {
        mockMvc.perform(get("/household/{id}/delete",household1.getId()).with(user(user1.getUsername()).password("password")))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("pendingHouseholds"))
                .andExpect(model().attributeExists("activeHouseholds"));
        verify(userService,times(1)).findUserByUsername(user1.getUsername());
        verify(membershipService,times(1)).leaveHousehold(household1.getId());
    }

    @Test
    public void declineInvitationUserExist() throws Exception {

        mockMvc.perform(get("/household/1/invitation/1/decline")
                        .with(user("user1").password("password")))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("pendingHouseholds"))
                .andExpect(model().attributeExists("activeHouseholds"));

        verify(userService,times(1)).findUserByUsername(user1.getUsername());
        verify(membershipService,times(1)).declineInvitation(user1Membership.getId());

    }

    @Test
    public void acceptInvitation() throws Exception {
        mockMvc.perform(get("/household/{householdId}/invitation/{membershipId}/accept",household1.getId(),user1Membership.getId())
                        .with(user("user1").password("password")))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attributeExists("pendingHouseholds"))
                .andExpect(model().attributeExists("activeHouseholds"));

        verify(userService,times(1)).findUserByUsername(user1.getUsername());
        verify(membershipService,times(1)).acceptInvitation(user1Membership.getId());

    }

    @Test
    public void searchForUser() throws Exception {
        mockMvc.perform(post("/{householdId}/users/search",household1.getId())
                        .with(user(user1.getUsername()).password(user1.getPassword()))
                        .with(csrf())
                        .param("searchTerm",user1.getUsername()))
                .andExpect(status().isOk())
                .andExpect(view().name("members/inviteMember"));
        verify(userService,times(1)).findUsersBySearchTerm(user1.getUsername());
    }


}
