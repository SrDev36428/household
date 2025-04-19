package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.HouseholdDAO;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.repository.household.jpa.HouseHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HouseholdDAOImpl implements HouseholdDAO {

    private final HouseHoldRepository houseHoldRepository;

    @Override
    public Household saveHousehold (Household household) {
        return houseHoldRepository.save(household);
    }

    @Override
    public List<Household> findAllHouseholds() {
        return houseHoldRepository.findAll();
    }

    @Override
    public Optional<Household> findHouseholdById (Long id) {
        return houseHoldRepository.findById(id);
    }

    @Override
    public void deleteHouseholdById (Long id) {
        houseHoldRepository.deleteById(id);
    }

}
