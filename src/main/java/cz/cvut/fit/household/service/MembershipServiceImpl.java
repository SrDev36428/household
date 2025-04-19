package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.interfaces.MembershipDAO;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.repository.filter.MembershipFilter;
import cz.cvut.fit.household.service.interfaces.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipServiceImpl implements MembershipService {

    private final MembershipDAO membershipDAO;


    @Override
    public Membership createMembership(Membership membership, User user, Household houseHold) {
        user.addMembership(membership);
        houseHold.addMembership(membership);
        log.debug("Creating membership: {} ", membership);

        return membershipDAO.saveMembership(membership);
    }

    @Override
    @Transactional
    public List<Membership> filterMemberships(MembershipFilter membershipFilter) {
        return membershipDAO.filterMemberships(membershipFilter);
    }

    @Override
    public void acceptInvitation(Long membershipId) {
        Membership membership = membershipDAO.findMembershipById(membershipId)
                .orElseThrow(() -> new NonExistentEntityException("Membership with id:- " + membershipId + " doesn't exist."));

        membership.setStatus(MembershipStatus.ACTIVE);
        log.debug("Setting membership  - {} status to active" , membership);
        membershipDAO.saveMembership(membership);
    }

    @Override
    public void declineInvitation(Long membershipId) {
        Membership membership = membershipDAO.findMembershipById(membershipId)
                .orElseThrow(() -> new NonExistentEntityException("Membership with id: " + membershipId + " doesn't exist"));
        membership.setStatus(MembershipStatus.DISABLED);
        log.info("Setting membership - {} status to disabled" , membership);
        membershipDAO.saveMembership(membership);
    }

    @Override
    public void leaveHousehold(Long id) {
        Membership membership = membershipDAO.findMembershipById(id)
                .orElseThrow(() -> new NonExistentEntityException("Membership with id: " + id + " doesn't exist"));

        membership.setStatus(MembershipStatus.DISABLED);
        membershipDAO.saveMembership(membership);
    }

    @Override
    public List<Membership> findAllMemberships() {
        return membershipDAO.findAllMemberships();
    }

    @Override
    public List<Membership> findMembershipsByUsername(String username) {
        return membershipDAO.findMembershipsByUsername(username);
    }

    @Override
    public Optional<Membership> findMembershipById(Long membershipId) {
        return membershipDAO.findMembershipById(membershipId);
    }

}
