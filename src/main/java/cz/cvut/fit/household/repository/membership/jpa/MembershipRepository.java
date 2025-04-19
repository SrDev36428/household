package cz.cvut.fit.household.repository.membership.jpa;

import cz.cvut.fit.household.datamodel.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository class for management of memberships entities in the database.
 */
@Repository
public interface MembershipRepository extends MembershipCustomRepository, JpaRepository<Membership, Long> {

    /**
     * Searching for members with given name
     *
     * @param searchTerm is username of needed member
     * @return a list of the memberships, whose username is matched with given
     */
    @Query(value = "select m from Membership m where m.user.username like %?1%")
    List<Membership> findMembershipsByUsername(String searchTerm);

    @Query(value = "select m from Membership m where m.id =: id")
    Membership findMembershipsWithId(@Param("id")Long membershipId);

}
