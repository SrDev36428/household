package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.LocationDAOImpl;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.entity.location.LocationCreationDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LocationServiceImplTest {

    @InjectMocks
    LocationServiceImpl locationService;

    @Mock
    LocationDAOImpl locationDAO;

    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(),new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    Location sublocation1 = new Location(2L,null,new ArrayList<>(),null,new ArrayList<>(),"title","description");
    Location sublocation2 = new Location(3L,null,new ArrayList<>(),null,new ArrayList<>(),"title","description");
    Location location = new Location(1L,null,new ArrayList<>(),null, Arrays.asList(sublocation1,sublocation2),"title","description");
    LocationCreationDTO locationCreationDTO = new LocationCreationDTO("title","description");

    List<Location> listOfLocations = Collections.singletonList(location);

    @Before
    public void setup () {
        sublocation1.setMainLocation(location);
        sublocation2.setMainLocation(location);
        when(locationDAO.findAllLocations()).thenReturn(listOfLocations);
        when(locationDAO.findLocationById(location.getId())).thenReturn(Optional.ofNullable(location));

    }

    @Test
    public void addLocation() {
        locationService.addLocation(locationCreationDTO, household1, null);
        verify(locationDAO,times(1)).saveLocation(any(Location.class));
    }

    @Test
    public void findLocations() {
        List<Location> result = locationService.findAllLocations();
        assertEquals(result,listOfLocations);
    }

    @Test
    public void findLocationById() {
        Optional<Location> result =  locationService.findLocationById(location.getId());
        assertEquals(result.get(),location);
    }

    @Test
    public void updateLocation() {
        Location updatedLocation = new Location(1L,null,new ArrayList<>(),null,new ArrayList<>(),"new_title","new_description");
        LocationCreationDTO updatedLocationCreationDTO = new LocationCreationDTO("new_title","new_description");
        locationService.updateLocation(location.getId(),updatedLocationCreationDTO);

        ArgumentCaptor<Location> argument = ArgumentCaptor.forClass(Location.class);
        verify(locationDAO).saveLocation(argument.capture());
        assertEquals(updatedLocation.getTitle(), argument.getValue().getTitle());
        assertEquals(location.getId(), argument.getValue().getId());

    }

    @Test
    public void findAllSublocations() {
        List<Location> result = locationService.findAllSubLocations(location);

        assertEquals(result,location.getSubLocations());
    }

}
