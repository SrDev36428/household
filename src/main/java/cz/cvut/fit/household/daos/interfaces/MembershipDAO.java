/**
 * The MembershipDAO interface is a Data Access Object that is used to manage Membership entities in the data storage.
 * It provides methods for saving, retrieving, filtering, and deleting memberships.
 *
 * @see cz.cvut.fit.household.datamodel.entity.Membership
 * @see cz.cvut.fit.household.repository.filter.MembershipFilter
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.repository.filter.MembershipFilter;

import java.util.List;
import java.util.Optional;

public interface MembershipDAO {

    /**
     * Saves the given Membership entity in the data storage.
     *
     * @param membership The Membership object to be saved.
     * @return The saved Membership object.
     */
    Membership saveMembership(Membership membership);

    /**
     * Filters memberships based on the provided filter criteria.
     *
     * @param membershipFilter The filter criteria to apply.
     * @return A list of Membership objects that match the filter criteria.
     */
    List<Membership> filterMemberships(MembershipFilter membershipFilter);

    /**
     * Finds a Membership entity by its unique id.
     *
     * @param id The unique id of the Membership to be retrieved.
     * @return An Optional containing the found Membership, or an empty Optional if not found.
     */
    Optional<Membership> findMembershipById(Long id);

    /**
     * Retrieves a list of all Membership entities in the data storage.
     *
     * @return A list of Membership objects representing all memberships.
     */
    List<Membership> findAllMemberships();

    /**
     * Retrieves a list of memberships associated with the specified username.
     *
     * @param username The username for which memberships are to be retrieved.
     * @return A list of Membership objects associated with the specified username.
     */
    List<Membership> findMembershipsByUsername(String username);
}
