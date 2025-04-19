package cz.cvut.fit.household.repository.maintenance;

import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
}
