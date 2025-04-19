package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;
import cz.cvut.fit.household.service.AuthorizationService;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.MaintenanceService;
import cz.cvut.fit.household.service.interfaces.MaintenanceTaskService;
import cz.cvut.fit.household.service.interfaces.MembershipService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(MaintenanceTaskController.class)
@ContextConfiguration
@Slf4j
public class MaintenanceTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HouseHoldService houseHoldService;

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private MaintenanceService maintenanceService;

    @MockBean
    private MaintenanceTaskService maintenanceTaskService;

    @MockBean
    private AuthorizationService authorizationService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock SecurityContext and Authentication
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testGetUserMaintenanceTasksView_successful() throws Exception {
        // Given
        Long householdId = 1L;
        Household household = new Household();
        household.setId(householdId);
        household.setTitle("Test Household");

        // Return the household when called
        given(houseHoldService.findHouseHoldById(householdId)).willReturn(Optional.of(household));

        // Mock membership retrieval
        Membership mockMembership = new Membership();
        mockMembership.setId(100L);
        mockMembership.setHousehold(household);

        // Return a single membership for the user in this household
        given(membershipService.findMembershipsByUsername("testUser"))
                .willReturn(java.util.Collections.singletonList(mockMembership));

        // Mock tasks
        MaintenanceTask task1 = new MaintenanceTask();
        task1.setTaskState(false);
        task1.setId(100L);

        // Return tasks for the membership
        given(maintenanceTaskService.findMaintenanceTasksByAssignee(100L))
                .willReturn(java.util.Collections.singletonList(task1));

        // When & Then
        mockMvc.perform(get("/household/{householdId}/maintenanceTask", householdId)
                        .param("showAll", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("maintenance/maintenanceTask/maintenanceTaskView"))
                .andExpect(model().attributeExists("tasks"))
                .andExpect(model().attribute("showAll", false))
                .andExpect(model().attributeExists("household"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testGetUserMaintenanceTasksView_householdNotFound() throws Exception {
        Long householdId = 999L;

        // Mock the service to simulate a non-existent household
        given(houseHoldService.findHouseHoldById(householdId)).willReturn(Optional.empty());

        // Perform the GET request
        mockMvc.perform(get("/household/{householdId}/maintenanceTask", householdId)
                        .with(csrf())) // Include CSRF token
                .andExpect(status().isNotFound()); // Expect a 404 status
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testGetEditMaintenanceTaskFromView_success() throws Exception {
        Long householdId = 1L;
        Long taskId = 10L;

        // Mock data
        Household household = new Household();
        household.setId(householdId);

        MaintenanceTask task = new MaintenanceTask();
        task.setId(taskId);
        task.setDeadline(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // Initialize and set Maintenance for the task
        Maintenance maintenance = new Maintenance();
        maintenance.setId(1L);
        maintenance.setTitle("Test Maintenance Title"); // Set a valid title
        task.setMaintenance(maintenance);

        // Mock service responses
        given(houseHoldService.findHouseHoldById(householdId)).willReturn(Optional.of(household));
        given(maintenanceTaskService.findMaintenanceTaskByID(taskId)).willReturn(Optional.of(task));
        given(maintenanceService.findMaintenanceById(1L)).willReturn(Optional.of(maintenance));


        // Perform the GET request
        mockMvc.perform(get("/household/{householdId}/maintenanceTask/{maintenanceTaskId}/edit", householdId, taskId))
                .andExpect(status().isOk())
                .andExpect(view().name("maintenance/maintenanceTask/maintenanceTaskEditFromView"))
                .andExpect(model().attributeExists("newMaintenanceTask")) // Ensure newMaintenanceTask is in the model
                .andExpect(model().attribute("householdId", householdId)) // Check householdId is passed correctly
                .andExpect(model().attributeExists("household")) // Ensure household data is in the model
                .andExpect(model().attributeExists("maintenance")) // Ensure maintenance data is in the model
                .andExpect(model().attributeExists("assignee"))// Ensure assignee data is in the model
                .andExpect(model().attributeExists("timeDeadline"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testPostEditMaintenanceTaskFromView_deadlineBeforeNow() throws Exception {
        Long householdId = 1L;
        Long taskId = 10L;

        // Mock data
        Household household = new Household();
        household.setId(householdId);

        MaintenanceTask existingTask = new MaintenanceTask();
        existingTask.setId(taskId);
        existingTask.setDeadline(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // Initialize and set Maintenance for the existing task
        Maintenance maintenance = new Maintenance();
        maintenance.setId(5L);
        maintenance.setTitle("Test Maintenance Title"); // Set a valid title
        existingTask.setMaintenance(maintenance);

        // Mock service responses
        given(houseHoldService.findHouseHoldById(householdId)).willReturn(Optional.of(household));
        given(maintenanceTaskService.findMaintenanceTaskByID(taskId)).willReturn(Optional.of(existingTask));
        given(maintenanceService.findMaintenanceById(5L)).willReturn(Optional.of(maintenance));

        // Attempt to edit with a past deadline
        MaintenanceTask updatedTask = new MaintenanceTask();
        updatedTask.setDeadline(Date.from(
                LocalDate.now().minusDays(1)
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        ));
        updatedTask.setMaintenance(maintenance); // Set Maintenance for the updated task
        String time = "10:30";
        // Perform the POST request
        mockMvc.perform(post("/household/{householdId}/maintenanceTask/{maintenanceTaskId}/edit", householdId, taskId)
                        .param("time", time)
                        .flashAttr("newMaintenanceTask", updatedTask)
                        .with(csrf())) // Include CSRF token
                .andExpect(status().isOk()) // stays on the same page due to validation error
                .andExpect(view().name("maintenance/maintenanceTask/maintenanceTaskEditFromView"))
                .andExpect(model().attributeExists("rejectDeadline")); // Check if validation error is added
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testDeleteMaintenanceTaskFromView_success() throws Exception {
        Long householdId = 1L;
        Long taskId = 10L;

        mockMvc.perform(get("/household/{householdId}/maintenanceTask/{maintenanceTaskId}/delete", householdId, taskId)
                        .param("showAll", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + householdId + "/maintenanceTask?showAll=false"));

        // Verify that the deleteMaintenanceTask method was called
        verify(maintenanceTaskService).deleteMaintenanceTask(taskId);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testGetMaintenanceTaskDetailInView_success() throws Exception {
        Long householdId = 1L;
        Long taskId = 10L;

        // Mock household
        Household household = new Household();
        household.setId(householdId);

        // Mock maintenance task
        MaintenanceTask task = new MaintenanceTask();
        task.setId(taskId);

        // Mock service responses
        given(houseHoldService.findHouseHoldById(householdId)).willReturn(Optional.of(household));
        given(maintenanceTaskService.findMaintenanceTaskByID(taskId)).willReturn(Optional.of(task));

        // Perform GET request and verify response
        mockMvc.perform(get("/household/{householdId}/maintenanceTask/{maintenanceTaskId}/detail", householdId, taskId))
                .andExpect(status().isOk())
                .andExpect(view().name("maintenance/maintenanceTask/maintenanceTaskDetails"))
                .andExpect(model().attributeExists("task")) // Check that the task is added to the model
                .andExpect(model().attributeExists("household")) // Check that the household is added to the model
                .andExpect(model().attribute("householdId", householdId)); // Validate householdId in the model
    }

    @Test
    public void testGetMaintenanceTaskDetailInView_notFound() throws Exception {
        Long householdId = 1L;
        Long taskId = 10L;

        given(houseHoldService.findHouseHoldById(householdId)).willReturn(Optional.empty());

        // Should throw NonExistentEntityException or handle via exception handler
        mockMvc.perform(get("/household/{householdId}/maintenanceTask/{maintenanceTaskId}/detail", householdId, taskId))
                .andExpect(status().is4xxClientError());
    }
}
