package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.daos.LogDAOImpl;
import cz.cvut.fit.household.datamodel.entity.Log;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(LogsController.class)
@ContextConfiguration
public class LogsControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogDAOImpl logDAO;

    @MockBean
    private HouseHoldService houseHoldService;


    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(),new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    Log log = new Log(1L, household1.getId(), "new log");

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(java.util.Optional.ofNullable(household1));
        when(logDAO.findLogByHouseholdIdOrderByIdDesc(household1.getId())).thenReturn(Collections.singletonList(log));
    }

    @Test
    public void renderLogsPage() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/logs", household1.getId())
                        .flashAttr("logs", Collections.singletonList(log))
                        .flashAttr("household", household1))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("logs"))
                .andExpect(model().attribute("householdId", household1.getId()))
                .andExpect(model().attribute("logs", Collections.singletonList(log)));
    }

    @Test
    public void deleteAllLogs() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/logs/delete", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/household/" + household1.getId() + "/logs"));

        verify(logDAO).deleteAllLogsByHouseholdId(household1.getId());

    }
}