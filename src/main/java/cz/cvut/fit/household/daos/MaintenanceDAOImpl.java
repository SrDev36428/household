package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.MaintenanceDAO;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.repository.maintenance.MaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MaintenanceDAOImpl implements MaintenanceDAO {

    private final MaintenanceRepository maintenanceRepository;

    @Override
    public Maintenance saveMaintenance(Maintenance maintenance) {
        return maintenanceRepository.save(maintenance);
    }

    @Override
    public void deleteMaintenanceById (Long id) {
        maintenanceRepository.deleteById(id);
    }

    @Override
    public Optional<Maintenance> findMaintenanceById(Long id) {
        return maintenanceRepository.findById(id);
    }

    @Override
    public List<Maintenance> getAllMaintenances() {
        return maintenanceRepository.findAll();
    }

}

