package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.LocationDAO;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LocationDAOImpl implements LocationDAO {

    private final LocationRepository locationRepository;

    @Override
    public Location saveLocation (Location location) {
        return locationRepository.save(location);
    }

    @Override
    public List<Location> findAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public Optional<Location>  findLocationById(Long id) {
        return locationRepository.findById(id);
    }

    @Override
    public void deleteLocationById(Long id) {
        locationRepository.deleteById(id);
    }

}
