package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.MaintenanceTaskDAO;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;
import cz.cvut.fit.household.repository.maintenance.MaintenanceTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MaintenanceTaskDAOImpl implements MaintenanceTaskDAO {

    private final MaintenanceTaskRepository maintenanceTaskRepository;

    @Override
    public MaintenanceTask saveMaintenanceTask(MaintenanceTask maintenanceTask) {
        return maintenanceTaskRepository.save(maintenanceTask);
    }

    @Override
    public Optional<MaintenanceTask> findMaintenanceTaskById(Long id) {
        return maintenanceTaskRepository.findById(id);
    }

    @Override
    public void deleteMaintenanceTaskById(Long maintenanceTaskId) {
        maintenanceTaskRepository.deleteById(maintenanceTaskId);
    }

    @Override
    public List<MaintenanceTask> findAllMaintenanceTasks() {
        return maintenanceTaskRepository.findAll();
    }

    @Override
    public List<MaintenanceTask> findAllMaintenanceTasksByAssigneeId(Long assignee) {
        return maintenanceTaskRepository.getMaintenanceTasksByAssignee(assignee);
    }
}
