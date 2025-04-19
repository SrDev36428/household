package cz.cvut.fit.household.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.cvut.fit.household.daos.MembershipDAOImpl;
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
import cz.cvut.fit.household.exception.NonExistentEntityException;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MembershipServiceImplTest {

    @Mock
    private MembershipDAOImpl membershipDAO;

    @InjectMocks
    private MembershipServiceImpl membershipService;

    User user1 = new User("user1","1","User","1","user1@gmail.com",new ArrayList<>());
    User user2 = new User("user2","2","User","2","user2@gmail.com",new ArrayList<>());
    Household household1 = new Household(1L,"user1 household", "", new ArrayList<>(),new ArrayList<>(),new ArrayList<>(), new ArrayList<>());
    Household household2= new Household(2L,"user2 household", "", new ArrayList<>(),new ArrayList<>(),new ArrayList<>(), new ArrayList<>());
    Membership user1Membership= new Membership(1L, MembershipStatus.ACTIVE, MembershipRole.REGULAR, user1,household1);
    Membership user2Membership= new Membership(2L, MembershipStatus.ACTIVE, MembershipRole.REGULAR, user2,household2);
    List<Membership> user1Memberships = Collections.singletonList(user1Membership);
    List<Membership> user2Memberships = Collections.singletonList(user2Membership);
    List<Membership> memberships = Arrays.asList(user1Membership,user2Membership);

    @Before
    public void setup() {
        when(membershipDAO.saveMembership(user1Membership)).thenReturn(user1Membership);
        when(membershipDAO.saveMembership(user2Membership)).thenReturn(user2Membership);
        when(membershipDAO.findMembershipById(user1Membership.getId())).thenReturn(java.util.Optional.ofNullable(user1Membership));
        when(membershipDAO.findMembershipById(user2Membership.getId())).thenReturn(java.util.Optional.ofNullable(user2Membership));
        when(membershipDAO.findAllMemberships()).thenReturn(memberships);
        when(membershipDAO.findMembershipsByUsername(user2.getUsername())).thenReturn(user2Memberships);

    }

    @Test
    public void createMembership() {
        Membership membership = membershipService.createMembership(user1Membership,user1,household1);
        assertSame(membership, user1Membership);
        assertEquals(household1.getMemberships(),user1Memberships);
    }

    @Test
    public void leaveHousehold() {
        user1.addMembership(user1Membership);
        membershipService.leaveHousehold(1L);
        assertEquals(MembershipStatus.DISABLED, user1Membership.getStatus());
    }

    @Test(expected = NonExistentEntityException.class)
    public void leaveHouseholdWithoutMemberShip() {
        membershipService.leaveHousehold(3L);

    }

    @Test
    public void findAllMemberships() {
        List<Membership> foundMemberships = membershipService.findAllMemberships();
        assertSame(foundMemberships,memberships);
    }

    @Test
    public void findMembershipsByUsername() {
        List<Membership> foundMemberships = membershipService.findMembershipsByUsername(user2.getUsername());
        assertSame(foundMemberships,user2Memberships);
    }

    @Test
    public void acceptInvitation() {
        user2Membership.setStatus(MembershipStatus.PENDING);
        membershipService.acceptInvitation(user2Membership.getId());
        assertEquals(MembershipStatus.ACTIVE, user2Membership.getStatus());
    }

    @Test
    public void declineInvitation() {
        user2Membership.setStatus(MembershipStatus.PENDING);
        membershipService.declineInvitation(user2Membership.getId());
        assertEquals(MembershipStatus.DISABLED, user2Membership.getStatus());
    }

    @Test
    public void acceptInvitationOfNonExistentMember() {
        assertThrows(NonExistentEntityException.class, () -> {
            membershipService.declineInvitation(400L);
        });
        assertAll(() -> assertEquals(MembershipStatus.ACTIVE, user2Membership.getStatus()));

    }

    @Test
    public void declineInvitationOfNonExistentMember() {

        assertThrows(NonExistentEntityException.class, () -> {
            membershipService.declineInvitation(400L);
        });
        assertAll(() -> assertEquals(MembershipStatus.ACTIVE, user2Membership.getStatus()));


    }



}
