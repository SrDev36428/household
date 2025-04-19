package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.controller.controllerUtil.ControllerItemDisplayUtil;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.entity.location.LocationCreationDTO;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(LocationsController.class)
@ContextConfiguration
public class LocationsControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private HouseHoldService houseHoldService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private ControllerItemDisplayUtil controllerItemDisplayUtil;

    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(),new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    Location location = new Location(1L,household1, new ArrayList<>(),null,new ArrayList<>(),"location_1","description");
    LocationCreationDTO locationCreationDTO = new LocationCreationDTO("location_1","description");

    @Before
    public void setup() {
        household1.setLocations(Collections.singletonList(location));
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(java.util.Optional.ofNullable(household1));
        when(locationService.findLocationById(location.getId())).thenReturn(java.util.Optional.ofNullable(location));
    }

    @Test
    public void renderAddLocationPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/add", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("locations/addLocation"));
    }
    @Test
    public void renderLocationInfoPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}", household1.getId(),location.getId())
                        .flashAttr("mainLocation", location))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("locations/locationDetail"));

        verify(locationService,times(1)).findLocationById(location.getId());
    }
    @Test
    public void renderLocationsPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations", household1.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("locations/householdLocations"));

        verify(houseHoldService,times(1)).findHouseHoldById(household1.getId());
    }
    @Test
    public void addLocationTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/locations/add", household1.getId())
                        .flashAttr("location",locationCreationDTO))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/household/" + household1.getId() + "/locations"));

        verify(houseHoldService,times(1)).findHouseHoldById(household1.getId());
        verify(locationService,times(1)).addLocation(locationCreationDTO, household1, null);
    }
    @Test
    public void deleteLocationTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/delete", household1.getId(),location.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        verify(locationService,times(1)).deleteLocationById(location.getId());
    }
    @Test
    public void returnToMainLocationTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/return", household1.getId(),location.getId()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        verify(locationService,times(1)).findLocationById(location.getId());
    }
    @Test
    public void renderEditingPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/edit", household1.getId(),location.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("locations/edit/locationEdit"));

        verify(locationService,times(1)).findLocationById(location.getId());
    }
    @Test
    public void performEditingTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/locations/{locationId}/edit", household1.getId(),location.getId())
                        .flashAttr("updatedLocation",location))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());

        verify(locationService,times(1)).updateLocation(eq(location.getId()),any(LocationCreationDTO.class));

    }
//    @Test
//    public void locationItemsViewTest() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/items/view", household1.getId(),location.getId()))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(view().name("items/LocationItemsView"));
//
//        verify(locationService,times(1)).findLocationById(location.getId());
//    }




}
