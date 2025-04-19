package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.interfaces.HouseholdDAO;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.HouseholdCreationDTO;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class HouseHoldServiceImpl implements HouseHoldService {

    private final HouseholdDAO householdDAO;


    @Override
    public Household createHousehold(HouseholdCreationDTO updatedHouseHold) {
        Household household = new Household();
        log.debug("Creating household with title - {} and description - {}", updatedHouseHold.getTitle(), updatedHouseHold.getDescription());
        household.setTitle(updatedHouseHold.getTitle());
        household.setDescription(updatedHouseHold.getDescription());
        return householdDAO.saveHousehold(household);
    }

    @Override
    public Household updateHousehold(HouseholdCreationDTO newHouseHold, Long id) {
        Household household = findHouseHoldById(id)
                .orElseThrow(() ->{
                    log.error("Could not find household with id - {}", id);
                    return new NonExistentEntityException("Not exist household with id: " + id);});

        household.setTitle(newHouseHold.getTitle());
        household.setDescription(newHouseHold.getDescription());
        log.debug("Updating  household {} with  new title - {} and new description - {}", household, newHouseHold.getTitle(), newHouseHold.getDescription());
        return householdDAO.saveHousehold(household);
    }

    @Override
    public List<Household> findAllHouseholds() {
        return householdDAO.findAllHouseholds();
    }

    @Override
    public List<Household> findHouseholdsByUsername(String username) {
        List<Household> households = householdDAO.findAllHouseholds();
        List<Household> resultHouseholds = new ArrayList<>();

        for (Household houseHold : households) {
            for (Membership membership : houseHold.getMemberships()) {
                if (membership.getUser().getUsername().equals(username)) {
                    resultHouseholds.add(houseHold);
                }
            }
        }

        return resultHouseholds;
    }

    @Override
    public Optional<Household> findHouseHoldById(Long id) {
        return householdDAO.findHouseholdById(id);
    }

    @Override
    public List<Membership> findMembershipsByHouseholdId(Long id) {
        Optional<Household>householdOptional =   householdDAO.findHouseholdById(id);
        if(householdOptional.isPresent()){
            return householdOptional.get().getMemberships();
        }else{
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteHouseholdById(Long id) {
        householdDAO.deleteHouseholdById(id);
    }
}
