package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.interfaces.MaintenanceTaskDAO;
import cz.cvut.fit.household.datamodel.entity.events.MaintenanceTaskCloseEvent;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;
import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.MaintenanceTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MaintenanceTaskServiceImpl implements MaintenanceTaskService {

    private final MaintenanceTaskDAO maintenanceTaskDAO;
    private final ApplicationEventPublisher eventPublisher;
    @Override
    public MaintenanceTask addMaintenanceTask(MaintenanceTask maintenanceTask) {
        log.debug("Creating a new maintenance task - {} for maintenance with id - {}" , maintenanceTask, maintenanceTask.getMaintenance().getId());
        return maintenanceTaskDAO.saveMaintenanceTask(maintenanceTask);
    }

    @Override
    public MaintenanceTask updateMaintenanceTask(Long id, MaintenanceTask maintenanceTask) {
        log.info("Maintenance task with id {} has been updated, with updated - {}", id, maintenanceTask);
        Optional<MaintenanceTask> optionalMaintenanceTask = maintenanceTaskDAO.findMaintenanceTaskById(id);
        MaintenanceTask oldMaintenanceTask = optionalMaintenanceTask.get();
        oldMaintenanceTask.setDeadline(maintenanceTask.getDeadline());
        oldMaintenanceTask.setMaintenance(maintenanceTask.getMaintenance());
        oldMaintenanceTask.setClosingDate(maintenanceTask.getClosingDate());
        oldMaintenanceTask.setTaskState(maintenanceTask.isTaskState());

        return maintenanceTaskDAO.saveMaintenanceTask(oldMaintenanceTask);
    }

    @Override
    public void deleteMaintenanceTask(Long id) {
        log.info("Maintenance task with id - {} has been deleted" , id);
        maintenanceTaskDAO.deleteMaintenanceTaskById(id);
    }

    @Override
    public MaintenanceTask openMaintenanceTask(Long id) {
        MaintenanceTask task = maintenanceTaskDAO.
                findMaintenanceTaskById(id).
                orElseThrow(()-> new NonExistentEntityException("Maintenance task with id " + id + " not found"));
        task.setTaskState(true);
        task.setClosingDate(null);
        log.info("Status of Maintenance task with id - {} has been set to OPEN" , id);
        return maintenanceTaskDAO.saveMaintenanceTask(task);
    }

    @Override
    public MaintenanceTask closeMaintenanceTask(Long id) {
        MaintenanceTask task = maintenanceTaskDAO.
                findMaintenanceTaskById(id).
                orElseThrow(()-> new NonExistentEntityException("Maintenance task with id " + id + " not found"));
        task.setTaskState(false);
        task.setClosingDate(new Date());
        log.info("Status of Maintenance task with id - {} has been set to CLOSED" , id);
        eventPublisher.publishEvent(new MaintenanceTaskCloseEvent(this,task.getMaintenance()));
        return maintenanceTaskDAO.saveMaintenanceTask(task);
    }

    @Override
    public Optional<MaintenanceTask> findMaintenanceTaskByID(Long id) {
        return maintenanceTaskDAO.findMaintenanceTaskById(id);
    }

    @Override
    public List<MaintenanceTask> findMaintenanceTasksByAssignee(Long id) {
        List<MaintenanceTask>tasks=maintenanceTaskDAO.findAllMaintenanceTasksByAssigneeId(id);
        return tasks.stream().sorted(Comparator.comparing(MaintenanceTask::getDeadline)).collect(Collectors.toList());
    }
}
