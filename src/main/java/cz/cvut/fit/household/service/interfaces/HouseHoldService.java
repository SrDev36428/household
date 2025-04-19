package cz.cvut.fit.household.service.interfaces;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.HouseholdCreationDTO;

import java.util.List;
import java.util.Optional;

public interface HouseHoldService {

    /**
     * Create or update household.
     *
     * @param houseHold which has to be saved
     * @return freshly saved household
     */
    Household createHousehold(HouseholdCreationDTO houseHold);

    Household updateHousehold(HouseholdCreationDTO newHouseHold, Long id);

    /**
     * Retrieve all existing households.
     *
     * @return all existed households in database
     */
    List<Household> findAllHouseholds();

    /**
     * Retrieve all households where user with given username is a member.
     *
     * @param username of member{@link Membership}
     * @return list of households, which linked to given username
     * @see Membership
     */
    List<Household> findHouseholdsByUsername(String username);

    /**
     * Retrieve all memberships of household with given id.
     *
     * @param id of the household
     * @return list of members in the household, with a given id
     * @see Membership
     */
    List<Membership> findMembershipsByHouseholdId(Long id);

    /**
     * Retrieve household with given id.
     *
     * @param id of the household
     * @return optional of household, if household with given id is present, otherwise empty optional.
     */
    Optional<Household> findHouseHoldById(Long id);

    /**
     * Delete household by id.
     *
     * @param id of the household
     */
    void deleteHouseholdById(Long id);

}
