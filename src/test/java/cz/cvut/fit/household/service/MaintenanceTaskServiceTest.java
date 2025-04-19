package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.MaintenanceTaskDAOImpl;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MaintenanceTaskServiceTest {

    @Mock
    private MaintenanceTaskDAOImpl maintenanceTaskDAO;

    //this mock is needed for the even of closing a maintenance task
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MaintenanceTaskServiceImpl maintenanceTaskService;

    MaintenanceTask testMaintenanceTask = new MaintenanceTask();

    Maintenance maintenance = new Maintenance();
    MaintenanceTask updatingMaintenanceTask = new MaintenanceTask();

    @Before
    public void setup(){
        testMaintenanceTask.setId(1L);
        testMaintenanceTask.setTaskState(false);
        testMaintenanceTask.setMaintenance(maintenance);

        updatingMaintenanceTask.setId(1L);
        updatingMaintenanceTask.setDeadline(new Date(1L));
        updatingMaintenanceTask.setMaintenance(maintenance);
        updatingMaintenanceTask.setClosingDate(new Date(2L));
        updatingMaintenanceTask.setTaskState(true);

        when(maintenanceTaskDAO.saveMaintenanceTask(testMaintenanceTask)).thenReturn(testMaintenanceTask);
        when(maintenanceTaskDAO.findMaintenanceTaskById(1L)).thenReturn(Optional.ofNullable(testMaintenanceTask));
        doNothing().when(maintenanceTaskDAO).deleteMaintenanceTaskById(1L);
        when(maintenanceTaskDAO.saveMaintenanceTask(updatingMaintenanceTask)).thenReturn(updatingMaintenanceTask);
    }

    @Test
    public void addMaintenanceTask() {
        MaintenanceTask result = maintenanceTaskService.addMaintenanceTask(testMaintenanceTask);

        assertEquals(testMaintenanceTask, result);
        verify(maintenanceTaskDAO , times(1)).saveMaintenanceTask(testMaintenanceTask);
    }

    @Test
    public void updateMaintenanceTask() {
        MaintenanceTask updatedMaintenanceTask = maintenanceTaskService.updateMaintenanceTask(1L , updatingMaintenanceTask);

        assertEquals(updatingMaintenanceTask, updatedMaintenanceTask);
        verify(maintenanceTaskDAO,times(1)).findMaintenanceTaskById(1L);
        verify(maintenanceTaskDAO,times(1)).saveMaintenanceTask(Mockito.any());
    }

    @Test
    public void deleteMaintenanceTask() {
        maintenanceTaskService.deleteMaintenanceTask(1L);

        verify(maintenanceTaskDAO,times(1)).deleteMaintenanceTaskById(1L);
    }

    @Test
    public void closeMaintenanceTask() {

        MaintenanceTask closedMaintenanceTask = maintenanceTaskService.closeMaintenanceTask(testMaintenanceTask.getId());
        assertFalse(closedMaintenanceTask.isTaskState());
        verify(maintenanceTaskDAO,times(1)).findMaintenanceTaskById(1L);
        verify(maintenanceTaskDAO,times(1)).saveMaintenanceTask(Mockito.any());
    }

    @Test
    public void findMaintenanceTaskByID() {
    }
}