package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.daos.interfaces.LogDAO;
import cz.cvut.fit.household.datamodel.entity.Log;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(LogsController.class)
public class LogsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private LogDAO logDAO;

    @MockBean
    private HouseHoldService houseHoldService;

    @MockBean
    private UserService userService;

    @MockBean
    private SecurityContext securityContext;

    @MockBean
    private Authentication authentication;

    private Household household1;
    private Log log;
    private User user;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        household1 = new Household(1L, "user1 household", "",
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        log = new Log(1L, household1.getId(), "new log");
        user = new User("testUser", "password", "test", "user", "test@email.com", new ArrayList<>());

        // Mock security context
        when(authentication.getName()).thenReturn("testUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock service responses
        when(userService.findUserByUsername("testUser")).thenReturn(Optional.of(user));
        when(houseHoldService.findHouseHoldById(household1.getId()))
                .thenReturn(Optional.of(household1));
        when(logDAO.findLogByHouseholdIdOrderByIdDesc(household1.getId()))
                .thenReturn(Collections.singletonList(log));
    }

    @Test
    public void renderLogsPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/logs", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("logs"))
                .andExpect(model().attribute("householdId", household1.getId()))
                .andExpect(model().attribute("username", user.getUsername()))
                .andExpect(model().attribute("email", user.getEmail()))
                .andExpect(model().attribute("logs", Collections.singletonList(log)));
    }

    @Test
    public void renderLogsPage_shouldThrowExceptionWhenUserNotFound() throws Exception {
        when(userService.findUserByUsername("testUser")).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/logs", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void renderLogsPage_shouldThrowExceptionWhenHouseholdNotFound() throws Exception {
        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/logs", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void deleteLogById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/logs/{logId}/delete",
                household1.getId(), log.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/household/" + household1.getId() + "/logs"));

        verify(logDAO).deleteLogById(log.getId());
    }
}