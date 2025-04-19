package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceCreationDTO;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTaskCreationDTO;
import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.datamodel.enums.RecurringType;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(MaintenanceController.class)
@ContextConfiguration

public class MaintenanceControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @Qualifier("maintenanceServiceFacadeImpl")
    private MaintenanceServiceFacade maintenanceServiceFacade;

    User user1 = new User("User1", "User1", "User1Name", "User1Surname", "user1@mail.com", new ArrayList<>());
    Household household1 = new Household(1L, "user1 household", "", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    Membership membership1 = new Membership(1L, MembershipStatus.ACTIVE, MembershipRole.OWNER, user1, household1);
    Maintenance maintenanceDaily = new Maintenance(
            1L,
            "Daily maintenance",
            "Daily description",
            membership1,
            membership1,
            household1,
            Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(LocalDate.now().atStartOfDay().plusDays(14).atZone(ZoneId.systemDefault()).toInstant()),
            false,
            true,
            new ArrayList<>(),
            new ArrayList<>());
    RecurringPattern dailyPattern = new RecurringPattern(1L, 0, 0, 1, 0, LocalTime.of(10, 0), maintenanceDaily, RecurringType.DAILY);

    Maintenance maintenanceWeekly = new Maintenance(2L,
            "Weekly maintenance",
            "Weekly description",
            membership1,
            membership1,
            household1,
            Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(LocalDate.now().atStartOfDay().plusDays(20).atZone(ZoneId.systemDefault()).toInstant()),
            false,
            true,
            new ArrayList<>(),
            new ArrayList<>());
    RecurringPattern weeklyMonday = new RecurringPattern(2L, 0, 0, 1, 0, LocalTime.of(10, 0), maintenanceWeekly, RecurringType.WEEKLY);
    RecurringPattern weeklyTuesday = new RecurringPattern(3L, 0, 1, 1, 0, LocalTime.of(10, 0), maintenanceWeekly, RecurringType.WEEKLY);
    RecurringPattern weeklyWednesday = new RecurringPattern(4L, 0, 2, 1, 0, LocalTime.of(10, 0), maintenanceWeekly, RecurringType.WEEKLY);
    RecurringPattern weeklyThursday = new RecurringPattern(5L, 0, 3, 1, 0, LocalTime.of(10, 0), maintenanceWeekly, RecurringType.WEEKLY);
    RecurringPattern weeklyFriday = new RecurringPattern(6L, 0, 4, 1, 0, LocalTime.of(10, 0), maintenanceWeekly, RecurringType.WEEKLY);
    RecurringPattern weeklySaturday = new RecurringPattern(7L, 0, 5, 1, 0, LocalTime.of(10, 0), maintenanceWeekly, RecurringType.WEEKLY);
    RecurringPattern weeklySunday = new RecurringPattern(8L, 0, 6, 1, 0, LocalTime.of(10, 0), maintenanceWeekly, RecurringType.WEEKLY);

    Maintenance maintenanceMonthly = new Maintenance(
            3L,
            "Monthly maintenance",
            "Monthly description",
            membership1,
            membership1,
            household1,
            Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(LocalDate.now().atStartOfDay().plusDays(40).atZone(ZoneId.systemDefault()).toInstant()),
            false,
            true,
            new ArrayList<>(),
            new ArrayList<>());
    RecurringPattern monthlyPattern = new RecurringPattern(9L, 1, 0, 1, 0, LocalTime.of(10, 0), maintenanceMonthly, RecurringType.MONTHLY);

    Maintenance maintenanceYearly = new Maintenance(4L,
            "Yearly maintenance",
            "Monthly description",
            membership1,
            membership1,
            household1,
            Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(LocalDate.now().atStartOfDay().plusYears(2).atZone(ZoneId.systemDefault()).toInstant()),
            false,
            true,
            new ArrayList<>(),
            new ArrayList<>());
    RecurringPattern yearlyPattern = new RecurringPattern(10L, 2, 0, 1, 2, LocalTime.of(10, 0), maintenanceYearly, RecurringType.YEARLY);

    List<Maintenance> maintenanceList = Arrays.asList(maintenanceDaily, maintenanceWeekly, maintenanceMonthly, maintenanceYearly);

    MaintenanceCreationDTO maintenanceCreationDTODaily = new MaintenanceCreationDTO(
            maintenanceDaily.getTitle(),
            maintenanceDaily.getDescription(),
            maintenanceDaily.getAssignee(),
            maintenanceDaily.getCreator(),
            RecurringType.DAILY,
            Date.from(LocalDate.now().atStartOfDay().plusDays(20).atZone(ZoneId.systemDefault()).toInstant()),
            1,
            LocalTime.of(10, 0),
            0,
            "0000000",
            0,
            Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()),
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
    );
    MaintenanceCreationDTO maintenanceCreationDTOWeekly = new MaintenanceCreationDTO(
            maintenanceWeekly.getTitle(),
            maintenanceWeekly.getDescription(),
            maintenanceWeekly.getAssignee(),
            maintenanceWeekly.getCreator(),
            RecurringType.WEEKLY,
            maintenanceWeekly.getEndDate(),
            1,
            LocalTime.of(10, 0),
            0,
            "1111111",
            0,
            maintenanceWeekly.getStartDate(),
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
    );
    MaintenanceCreationDTO maintenanceCreationDTOMonthly = new MaintenanceCreationDTO(
            maintenanceMonthly.getTitle(),
            maintenanceMonthly.getDescription(),
            maintenanceMonthly.getAssignee(),
            maintenanceMonthly.getCreator(),
            RecurringType.MONTHLY,
            maintenanceMonthly.getEndDate(),
            1,
            LocalTime.of(10, 0),
            1,
            "0000000",
            0,
            maintenanceMonthly.getStartDate(),
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
    );
    MaintenanceCreationDTO maintenanceCreationDTOYearly = new MaintenanceCreationDTO(
            maintenanceYearly.getTitle(),
            maintenanceYearly.getDescription(),
            maintenanceYearly.getAssignee(),
            maintenanceYearly.getCreator(),
            RecurringType.YEARLY,
            maintenanceYearly.getEndDate(),
            1,
            LocalTime.of(10, 0),
            2,
            "0000000",
            2,
            maintenanceYearly.getStartDate(),
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
    );

    Maintenance updatedMaintenance = new Maintenance(maintenanceWeekly.getId(), "Updated maintenance", "UpdatedMaintenance", membership1, membership1, household1, maintenanceDaily.getStartDate(), maintenanceDaily.getEndDate(), false, true, new ArrayList<>(), new ArrayList<>());

    MaintenanceCreationDTO updatedMaintenanceDTO = new MaintenanceCreationDTO(
            updatedMaintenance.getTitle(),
            updatedMaintenance.getDescription(),
            membership1,
            membership1,
            RecurringType.DAILY,
            updatedMaintenance.getEndDate(),
            1,
            LocalTime.of(10, 0),
            0,
            "0000000",
            0,
            updatedMaintenance.getStartDate(),
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
    );

    private static final String HOUSEHOLD_WITH_ID = "Household With id: ";
    private static final String MAINTENANCE_WITH_ID = "Maintenance With id: ";
    private static final String DOES_NOT_EXIST = " doesn't exist";

    private String getMaintenanceNotExitsIdMsg(Long id) {
        return MAINTENANCE_WITH_ID + id + DOES_NOT_EXIST;
    }

    private String getHouseholdNotExistIdMsg(Long id) {
        return HOUSEHOLD_WITH_ID + id + DOES_NOT_EXIST;
    }

    @Before
    public void setUp() {

        maintenanceDaily.addRecurringPattern(dailyPattern);

        maintenanceWeekly.addRecurringPattern(weeklyMonday);
        maintenanceWeekly.addRecurringPattern(weeklyTuesday);
        maintenanceWeekly.addRecurringPattern(weeklyWednesday);
        maintenanceWeekly.addRecurringPattern(weeklyThursday);
        maintenanceWeekly.addRecurringPattern(weeklyFriday);
        maintenanceWeekly.addRecurringPattern(weeklySaturday);
        maintenanceWeekly.addRecurringPattern(weeklySunday);

        maintenanceMonthly.addRecurringPattern(monthlyPattern);
        maintenanceYearly.addRecurringPattern(yearlyPattern);

        household1.setMemberships(Collections.singletonList(membership1));
        household1.setMaintenances(maintenanceList);

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(maintenanceServiceFacade.getHouseHoldById(household1.getId(), getHouseholdNotExistIdMsg(household1.getId()))).thenReturn(household1);
        when(maintenanceServiceFacade.getHouseHoldById(0L, getHouseholdNotExistIdMsg(0L))).thenThrow(new NonExistentEntityException(getHouseholdNotExistIdMsg(0L)));

        when(maintenanceServiceFacade.isHouseholdOwner(household1)).thenReturn(true);

        when(maintenanceServiceFacade.getMembershipList()).thenReturn(Collections.singletonList(membership1));

        when(maintenanceServiceFacade.getMaintenanceById(maintenanceDaily.getId(), getMaintenanceNotExitsIdMsg(maintenanceDaily.getId()))).thenReturn(maintenanceDaily);
        when(maintenanceServiceFacade.getMaintenanceById(maintenanceWeekly.getId(), getMaintenanceNotExitsIdMsg(maintenanceWeekly.getId()))).thenReturn(maintenanceWeekly);
        when(maintenanceServiceFacade.getMaintenanceById(maintenanceMonthly.getId(), getMaintenanceNotExitsIdMsg(maintenanceMonthly.getId()))).thenReturn(maintenanceMonthly);
        when(maintenanceServiceFacade.getMaintenanceById(maintenanceYearly.getId(), getMaintenanceNotExitsIdMsg(maintenanceYearly.getId()))).thenReturn(maintenanceYearly);

        when(maintenanceServiceFacade.getMembershipList(household1)).thenReturn(Collections.singletonList(membership1));

        when(maintenanceServiceFacade.addMaintenance(maintenanceCreationDTODaily, household1)).thenReturn(maintenanceDaily);
        when(maintenanceServiceFacade.addMaintenance(maintenanceCreationDTOWeekly, household1)).thenReturn(maintenanceWeekly);
        when(maintenanceServiceFacade.addMaintenance(maintenanceCreationDTOMonthly, household1)).thenReturn(maintenanceMonthly);
        when(maintenanceServiceFacade.addMaintenance(maintenanceCreationDTOYearly, household1)).thenReturn(maintenanceYearly);

        when(maintenanceServiceFacade.updateMaintenance(maintenanceWeekly.getId(), updatedMaintenanceDTO, maintenanceWeekly, household1)).thenReturn(updatedMaintenance);


    }

    @Test
    public void getMaintenancesPage() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/maintenance", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("maintenance/maintenanceView"));
    }

    @Test
    public void getMaintenancesPageFail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/maintenance", 0L))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));
    }

    @Test
    public void getAddMaintenancePage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/maintenance/add", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("maintenance/addMaintenance"));

    }

    @Test
    public void postAddMaintenancePageDaily() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/maintenance", household1.getId())
                        .flashAttr("updatedMaintenance", maintenanceCreationDTODaily))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/maintenance"));
        verify(maintenanceServiceFacade, times(1)).getHouseHoldById(household1.getId(), getHouseholdNotExistIdMsg(household1.getId()));
        verify(maintenanceServiceFacade, times(1)).addMaintenance(maintenanceCreationDTODaily, household1);
        assertEquals(maintenanceServiceFacade.getMaintenanceById(maintenanceDaily.getId(), getMaintenanceNotExitsIdMsg(maintenanceDaily.getId())).getRecurringPatterns().size(), 1);
        assertEquals(maintenanceServiceFacade.getMaintenanceById(maintenanceDaily.getId(), getMaintenanceNotExitsIdMsg(maintenanceDaily.getId())).getRecurringPatterns().get(0).getRecurringType(), RecurringType.DAILY);
    }

    @Test
    public void postAddMaintenancePageWeekly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/maintenance", household1.getId())
                        .flashAttr("updatedMaintenance", maintenanceCreationDTOWeekly))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/maintenance"));
        verify(maintenanceServiceFacade, times(1)).getHouseHoldById(household1.getId(), getHouseholdNotExistIdMsg(household1.getId()));
        verify(maintenanceServiceFacade, times(1)).addMaintenance(maintenanceCreationDTOWeekly, household1);
        assertEquals(maintenanceServiceFacade.getMaintenanceById(maintenanceWeekly.getId(), getMaintenanceNotExitsIdMsg(maintenanceWeekly.getId())).getRecurringPatterns().size(), 7);
        assertEquals(maintenanceServiceFacade.getMaintenanceById(maintenanceWeekly.getId(), getMaintenanceNotExitsIdMsg(maintenanceWeekly.getId())).getRecurringPatterns().get(0).getRecurringType(), RecurringType.WEEKLY);
    }

    @Test
    public void postAddMaintenancePageMonthly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/maintenance", household1.getId())
                        .flashAttr("updatedMaintenance", maintenanceCreationDTOMonthly))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/maintenance"));
        verify(maintenanceServiceFacade, times(1)).getHouseHoldById(household1.getId(), getHouseholdNotExistIdMsg(household1.getId()));
        verify(maintenanceServiceFacade, times(1)).addMaintenance(maintenanceCreationDTOMonthly, household1);
        assertEquals(maintenanceServiceFacade.getMaintenanceById(maintenanceMonthly.getId(), getMaintenanceNotExitsIdMsg(maintenanceMonthly.getId())).getRecurringPatterns().size(), 1);
        assertEquals(maintenanceServiceFacade.getMaintenanceById(maintenanceMonthly.getId(), getMaintenanceNotExitsIdMsg(maintenanceMonthly.getId())).getRecurringPatterns().get(0).getRecurringType(), RecurringType.MONTHLY);
    }

    @Test
    public void postAddMaintenancePageYearly() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/maintenance", household1.getId())
                        .flashAttr("updatedMaintenance", maintenanceCreationDTOYearly))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/maintenance"));
        verify(maintenanceServiceFacade, times(1)).getHouseHoldById(household1.getId(), getHouseholdNotExistIdMsg(household1.getId()));
        verify(maintenanceServiceFacade, times(1)).addMaintenance(maintenanceCreationDTOYearly, household1);
        assertEquals(maintenanceServiceFacade.getMaintenanceById(maintenanceYearly.getId(), getMaintenanceNotExitsIdMsg(maintenanceYearly.getId())).getRecurringPatterns().size(), 1);
        assertEquals(maintenanceServiceFacade.getMaintenanceById(maintenanceYearly.getId(), getMaintenanceNotExitsIdMsg(maintenanceYearly.getId())).getRecurringPatterns().get(0).getRecurringType(), RecurringType.YEARLY);
    }

    @Test
    public void renderMaintenanceDetailsPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/maintenance/{maintenanceId}/detail", household1.getId(), maintenanceDaily.getId()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("maintenance/maintenanceDetails"));
    }

    @Test
    public void getEditMaintenanceInPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/maintenance/{maintenanceId}/detail/edit", household1.getId(), maintenanceWeekly.getId())
                        .flashAttr("maintenance", maintenanceWeekly))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("maintenance/edit/maintenanceEditIn"));
    }

    @Test
    public void postEditMaintenanceInPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/maintenance/{maintenanceId}/detail/edit", household1.getId(), maintenanceWeekly.getId())
                        .flashAttr("updatedMaintenance", updatedMaintenanceDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/maintenance/" + maintenanceWeekly.getId() + "/detail"));
        verify(maintenanceServiceFacade, times(1)).updateMaintenance(maintenanceWeekly.getId(), updatedMaintenanceDTO, maintenanceWeekly, household1);
    }

    @Test
    public void getEditMaintenancePage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/maintenance/{maintenanceId}/edit", household1.getId(), maintenanceWeekly.getId())
                        .flashAttr("maintenance", maintenanceWeekly))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("maintenance/edit/maintenanceEdit"));
    }

    @Test
    public void postEditMaintenancePage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/maintenance/{maintenanceId}/edit", household1.getId(), maintenanceWeekly.getId())
                        .flashAttr("updatedMaintenance", updatedMaintenanceDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/maintenance"));
        verify(maintenanceServiceFacade,times(1)).updateMaintenance(maintenanceWeekly.getId(), updatedMaintenanceDTO, maintenanceWeekly,household1);
    }

    @Test
    public void deleteMaintenance() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/maintenance/{maintenanceId}/delete", household1.getId(), maintenanceYearly.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/maintenance"));
        verify(maintenanceServiceFacade,times(1)).deleteMaintenance(maintenanceYearly.getId(),household1.getId());
    }

    @Test
    public void returnToMaintenance() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/maintenance/{maintenanceId}/return", household1.getId(),maintenanceDaily.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        verify(maintenanceServiceFacade,times(1)).getMaintenanceById(maintenanceDaily.getId(),getMaintenanceNotExitsIdMsg(maintenanceDaily.getId()));
    }

}
