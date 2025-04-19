package cz.cvut.fit.household.service.interfaces;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.repository.filter.MembershipFilter;

import java.util.List;
import java.util.Optional;

public interface MembershipService {

    /**
     * Creating membership of specific user {@link User}, and specific
     * household {@link Household}. Method adds new membership to user and household,
     * then it saves membership in database
     *
     * @param membership membership which is going to be added to database
     * @param user author of the membership
     * @param houseHold houseHold
     * @return created membership
     * @see User
     * @see Household
     */
    Membership createMembership(Membership membership, User user, Household houseHold);

    /**
     * Retrieve all existing memberships.
     *
     * @return all existed memberships in database
     */
    List<Membership> findAllMemberships();

    /**
     * @param username of needed memberships
     * @return list of memberships, which have matches with given username
     */
    List<Membership> findMembershipsByUsername(String username);

    Optional<Membership> findMembershipById(Long membershipId);

    /**
     * Searching for the membership, using id. Updating the status {@link MembershipStatus}
     * of it to <b>"ACTIVE"</b>.
     *
     * @param membershipId is an id of needed membership
     * @throws NonExistentEntityException
     * @see MembershipStatus
     * @see Household
     */
    void acceptInvitation(Long membershipId);

    /**
     * Searching for the membership, using id. Updating the status {@link MembershipStatus}
     * of it to <b>"DISABLE"</b>.
     *
     * @param membershipId is an id of needed membership
     * @throws NonExistentEntityException
     * @see MembershipStatus
     * @see Household
     */
    void declineInvitation(Long membershipId);

    /**
     * Searching for the membership, using id. Updating the status {@link MembershipStatus}
     * of it to <b>"DISABLE"</b>.
     *
     * @param id of needed membership
     * @throws NonExistentEntityException
     * @see MembershipStatus
     * @see Household
     */
    void leaveHousehold(Long id);

    /**
     * Searching for the members matching any parameters you want.
     *
     * @param membershipFilter is a format with right parameters
     * @return list of members, which matched with given filter-format
     */
    List<Membership> filterMemberships(MembershipFilter membershipFilter);

    //exclusion description in the impl file.
//    Membership findMembershipsById(Long membershipId);
}
