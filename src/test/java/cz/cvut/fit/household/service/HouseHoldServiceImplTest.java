package cz.cvut.fit.household.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cz.cvut.fit.household.daos.HouseholdDAOImpl;
import cz.cvut.fit.household.datamodel.entity.household.HouseholdCreationDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class HouseHoldServiceImplTest {

    @Mock
    HouseholdDAOImpl householdDAO;

    @InjectMocks
    HouseHoldServiceImpl houseHoldServiceImpl;

    User user1 = new User("user1","1","User","1","user1@gmail.com",new ArrayList<>());
    User user2 = new User("user2","2","User","2","user2@gmail.com",new ArrayList<>());
    Membership user1Membership= new Membership(1L, MembershipStatus.ACTIVE, MembershipRole.REGULAR, user1,null);
    Membership user2Membership= new Membership(2L, MembershipStatus.ACTIVE, MembershipRole.REGULAR, user2,null);
    List<Membership> user1Memberships = Collections.singletonList(user1Membership);
    List<Membership> user2Memberships = Collections.singletonList(user2Membership);
    Household household1 = new Household(1L,"user1 household", "",user1Memberships,null,null, null);
    HouseholdCreationDTO householdCreationDTO1 = new HouseholdCreationDTO("user1 household", "");
    Household household2= new Household(2L,"user2 household", "", user2Memberships,null,null, null);
//    List<Membership> memberships = Arrays.asList(user1Membership,user2Membership);
    List<Household> listOfHouseholds = Arrays.asList(household1,household2);
//    List<Household> listOfUser1Households = Collections.singletonList(household1);

    @Before
    public void setup() {
        user1Membership.setHousehold(household1);
        user2Membership.setHousehold(household2);
        when(householdDAO.findAllHouseholds()).thenReturn(listOfHouseholds);
        when(householdDAO.findHouseholdById(1L)).thenReturn(Optional.of(household1));
        when(householdDAO.findHouseholdById(household1.getId())).thenReturn(Optional.ofNullable(household1));
    }

    @Test
    public void findAllHouseHoldsOfTheUser() {
        List<Household> households= houseHoldServiceImpl.findHouseholdsByUsername("user1");
        Long id1=1L;
        assertEquals("different size of the household user1 list", 1, households.size());
        for(Household household : households) {
            Long id= household.getId();
            assertEquals(id1,id);
        }

    }

    @Test
    public void doNotFindHouseHoldOfNonExistingUser() {
        List<Household> households= houseHoldServiceImpl.findHouseholdsByUsername("user3");
//        Long id1=1L;
        assertEquals("different size of the household user3 list", 0, households.size());
    }

    @Test
    public void findHouseHoldById() {
        Optional<Household> household = houseHoldServiceImpl.findHouseHoldById(1L);
        assertTrue("household doesn't exist",household.isPresent());
        assertEquals("user1 household", household.get().getTitle());
    }

    @Test
    public void findMembershipsByHouseholdId() {
        List<Membership> memberships= houseHoldServiceImpl.findMembershipsByHouseholdId(household1.getId());
        assertEquals(household1.getMemberships(),memberships);
    }

    @Test
    public void updateHousehold() {
        Household newHousehold =  new Household(household1.getId(), "new title", "new description", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        houseHoldServiceImpl.updateHousehold(householdCreationDTO1,newHousehold.getId());

        verify(householdDAO, times(1)).saveHousehold(any(Household.class));
    }

    @Test
    public void findHouseholdsByUsername() {
        List<Household> result = houseHoldServiceImpl.findHouseholdsByUsername(user1.getUsername());
        assertEquals(result,Collections.singletonList(household1));
    }

}
