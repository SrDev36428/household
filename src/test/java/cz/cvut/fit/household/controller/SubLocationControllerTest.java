package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.datamodel.entity.item.ItemFilterDTO;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.entity.location.LocationCreationDTO;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.ItemService;
import cz.cvut.fit.household.service.interfaces.LocationService;
import cz.cvut.fit.household.service.interfaces.MembershipService;
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

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@WebMvcTest(SubLocationController.class)
@ContextConfiguration
public class SubLocationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private MembershipService membershipService;

    @MockBean
    private HouseHoldService houseHoldService;

    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(),new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    Location location = new Location(1L,household1, new ArrayList<>(),null,new ArrayList<>(),"location_1","description");
    Location subLocation = new Location(2L,null, new ArrayList<>(),location,new ArrayList<>(),"subLocation","description2");

    LocationCreationDTO locationCreationDTO = new LocationCreationDTO("subLocation","description2");
    ArrayList<Item>list = new ArrayList<>();
    Page<Item> itemPage = new PageImpl<Item>(list);

    @Before
    public void setUp() throws Exception {
        household1.setLocations(Collections.singletonList(location));
        location.setSubLocations(Collections.singletonList(subLocation));
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(java.util.Optional.ofNullable(household1));
        when(locationService.findLocationById(location.getId())).thenReturn(java.util.Optional.ofNullable(location));
        when(locationService.addLocation(locationCreationDTO, null, location)).thenReturn(subLocation);

    }

    @Test
    public void addSubLocationRejected() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/locations/{locationId}/sublocations/add", household1.getId(), location.getId())
                        .flashAttr("location", locationCreationDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("locations/sub/addSubLocation"));

    }

    @Test
    public void addSubLocationSuccessful() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post("/household/{householdId}/locations/{locationId}/sublocations/add", household1.getId(), location.getId())
                        .flashAttr("subLocation", locationCreationDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/household/" + household1.getId() + "/locations/" + location.getId()));
        verify(locationService,times(1)).addLocation(locationCreationDTO, null, location);

    }

    @Test
    public void getSubLocationView() throws Exception{
        ItemFilterDTO itemFilterDTO = new ItemFilterDTO();
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/sublocations/view", household1.getId(), location.getId())
                        .flashAttr("location", location)
                        .flashAttr("household", household1)
                        .flashAttr("items",itemPage)
                        .flashAttr("filter",itemFilterDTO))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("locations/locationDetail"));

    }

    @Test
    public void renderAddSubLocationPage() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/sublocations/add", household1.getId(), location.getId())
                        .flashAttr("location", locationCreationDTO))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("locations/sub/addSubLocation"));

    }

    @Test
    public void deleteSubLocation() throws Exception{
        ItemFilterDTO itemFilterDTO = new ItemFilterDTO();
        mockMvc.perform(MockMvcRequestBuilders.get("/household/{householdId}/locations/{locationId}/sublocations/{sublocationId}/delete", household1.getId(), location.getId(), subLocation.getId())
                        .flashAttr("mainLocation", location)
                        .flashAttr("household", household1)
                        .flashAttr("items",itemPage)
                        .flashAttr("filter",itemFilterDTO))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("locations/locationDetail"));

        verify(locationService,times(1)).deleteLocationById(subLocation.getId());

    }
}