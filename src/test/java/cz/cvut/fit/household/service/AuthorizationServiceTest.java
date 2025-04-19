package cz.cvut.fit.household.service;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationServiceTest {

    @Mock
    HouseHoldService houseHoldService;

    @Mock
    Authentication authenticationOfTheOwner;

    @Mock
    SecurityContext securityContext;

    @InjectMocks
    AuthorizationService authorizationService;

    User userOwner = new User("user1","1","User","1","user1@gmail.com",new ArrayList<>());
    User userRegular = new User("user2","2","User","2","user2@gmail.com",new ArrayList<>());
    Membership user2Membership= new Membership(2L, MembershipStatus.ACTIVE, MembershipRole.REGULAR, userRegular,null);
    Membership user1Membership= new Membership(1L, MembershipStatus.ACTIVE, MembershipRole.OWNER, userOwner,null);
    List<Membership> user1Memberships = Arrays.asList(user1Membership, user2Membership);
    Household household1 = new Household(1L,"user1 household", "",user1Memberships,null, null, null);

    @Before
    public void setup() {
        user1Membership.setHousehold(household1);
        user2Membership.setHousehold(household1);
        when(securityContext.getAuthentication()).thenReturn(authenticationOfTheOwner);
        when(authenticationOfTheOwner.getName()).thenReturn(userOwner.getUsername());
        SecurityContextHolder.setContext(securityContext);
        when(houseHoldService.findHouseHoldById(household1.getId())).thenReturn(java.util.Optional.ofNullable(household1));
    }

    @Test
    public void isOwnerOfTheHouseholdTest() {
        boolean isOwner = authorizationService.isOwner(household1);
        assertTrue(isOwner);
    }

    @Test
    public void isNotOwnerOfTheHouseholdTest() {
        Household household2 = new Household(1L,"user1 household", "", Collections.singletonList(user2Membership),null, null, null);
        boolean isNotOwner = authorizationService.isOwner(household2);
        assertFalse(isNotOwner);
    }

    @Test
    public void canKick() {
        boolean canKick = authorizationService.canKick(household1.getId(),userRegular.getUsername());
        assertTrue(canKick);
    }

    @Test
    public void canNotKick() {
        boolean canKick = authorizationService.canKick(household1.getId(),userOwner.getUsername());
        assertFalse(canKick);
    }
}
