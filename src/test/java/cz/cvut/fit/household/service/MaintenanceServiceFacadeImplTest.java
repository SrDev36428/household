package cz.cvut.fit.household.service;

import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;
import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;
import cz.cvut.fit.household.datamodel.enums.RecurringType;
import cz.cvut.fit.household.service.facades.MaintenanceServiceFacadeImpl;
import cz.cvut.fit.household.service.interfaces.MaintenanceTaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalTime;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class MaintenanceServiceFacadeImplTest {

    @Mock
    private MaintenanceTaskService taskService;

    @InjectMocks
    private MaintenanceServiceFacadeImpl facade;

    @Test
    public void testGenerateDailyTasksForShortPeriod() {
        // Arrange
        Maintenance maintenance = createMaintenance(RecurringType.DAILY, 1);
        maintenance.setActive(true);
        Date endDate = addDaysToDate(new Date(), 5); // Short period of 5 days
        maintenance.setEndDate(endDate);

        // Act
        facade.generateMaintenanceTasks(maintenance, false);

        // Assert
        Mockito.verify(taskService, Mockito.times(5)).addMaintenanceTask(Mockito.any(MaintenanceTask.class));
    }

    @Test
    public void testGenerateWeeklyTasksForLongPeriod() {
        // Arrange
        Maintenance maintenance = createMaintenance(RecurringType.WEEKLY, 1);
        maintenance.setActive(true);
        Date endDate = addDaysToDate(new Date(), 57); // Long period of 8+ weeks
        maintenance.setEndDate(endDate);

        // Act
        facade.generateMaintenanceTasks(maintenance, false);

        // Assert
        Mockito.verify(taskService, Mockito.times(8)).addMaintenanceTask(Mockito.any(MaintenanceTask.class));
    }

    @Test
    public void testAddTasksWhenSomeAreResolved() {
        // Arrange
        Maintenance maintenance = createMaintenance(RecurringType.DAILY, 1);
        maintenance.setActive(true);
        maintenance.setMaintenanceTasks(createExistingTasks(3)); // Already 3 tasks exist
        Date endDate = addDaysToDate(new Date(), 5); // Generate for 5 days
        maintenance.setEndDate(endDate);

        // Act
        facade.generateMaintenanceTasks(maintenance, true);

        // Assert
        Mockito.verify(taskService, Mockito.times(2)).addMaintenanceTask(Mockito.any(MaintenanceTask.class));
    }

    @Test
    public void testNoTasksGeneratedIfUpToDate() {
        // Arrange
        Maintenance maintenance = createMaintenance(RecurringType.DAILY, 1);
        maintenance.setMaintenanceTasks(createExistingTasks(5)); // Up-to-date with tasks
        Date endDate = addDaysToDate(new Date(), 5); // Generate for the same 5 days
        maintenance.setEndDate(endDate);

        // Act
        facade.generateMaintenanceTasks(maintenance, true);

        // Assert
        Mockito.verify(taskService, Mockito.never()).addMaintenanceTask(Mockito.any(MaintenanceTask.class));
    }

    private Maintenance createMaintenance(RecurringType type, int interval) {
        Maintenance maintenance = new Maintenance();
        maintenance.setStartDate(new Date());
        List<RecurringPattern> recurringPatterns = Arrays.asList(createRecurringPattern(type, interval));
        maintenance.setRecurringPatterns(recurringPatterns);
        return maintenance;
    }

    private RecurringPattern createRecurringPattern(RecurringType type, int interval) {
        RecurringPattern pattern = new RecurringPattern();
        pattern.setRecurringType(type);
        pattern.setInterval(interval);
        pattern.setTimeOfDay(LocalTime.now());
        return pattern;
    }

    private List<MaintenanceTask> createExistingTasks(int count) {
        List<MaintenanceTask> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            MaintenanceTask task = new MaintenanceTask();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, i);
            task.setDeadline(calendar.getTime());
            task.setTaskState(true); // Active task
            tasks.add(task);
        }
        return tasks;
    }

    private Date addDaysToDate(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }
}
