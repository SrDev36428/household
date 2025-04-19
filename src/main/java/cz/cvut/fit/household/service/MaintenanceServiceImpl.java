package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.interfaces.MaintenanceDAO;
import cz.cvut.fit.household.datamodel.entity.events.MaintenanceAddedEvent;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.*;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.enums.RecurringType;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.MaintenanceService;
import cz.cvut.fit.household.service.interfaces.MaintenanceTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceTaskService maintenanceTaskService;
    private final MaintenanceDAO maintenanceDAO;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Maintenance addMaintenance(MaintenanceCreationDTO updatedMaintenance, Household household, Membership creator, Membership assignee) {

        Maintenance maintenance = createBaseMaintenance(updatedMaintenance, household, creator, assignee);
        RecurringPattern baseRecurringPattern =createBaseRecurringPattern(updatedMaintenance , maintenance);

        switch (baseRecurringPattern.getRecurringType()) {
            case DAILY:
                // If the new list is empty, fall back to the old single time
                if (updatedMaintenance.getDailyTimes() != null && !updatedMaintenance.getDailyTimes().isEmpty() && !(updatedMaintenance.getDailyTimes().size() == 0)) {
                    for (LocalTime t : updatedMaintenance.getDailyTimes()) {
                        RecurringPattern rp = new RecurringPattern();
                        rp.setMaintenance(maintenance);
                        rp.setInterval(updatedMaintenance.getInterval());
                        rp.setRecurringType(RecurringType.DAILY);
                        rp.setTimeOfDay(t);

                        // Add any other fields you need...
                        maintenance.addRecurringPattern(rp);
                    }
                } else {
                    // Fallback: if dailyTimes is empty, we either do nothing
                    // or we handle the old single 'time' from createBaseRecurringPattern
                    maintenance.addRecurringPattern(baseRecurringPattern);
                }
                break;
            case MONTHLY:
                if (updatedMaintenance.getDaysOfMonth() != null && !updatedMaintenance.getDaysOfMonth().isEmpty()) {
                    for (Integer day : updatedMaintenance.getDaysOfMonth()) {
                        RecurringPattern rp = new RecurringPattern();
                        rp.setMaintenance(maintenance);
                        rp.setInterval(updatedMaintenance.getInterval());
                        rp.setRecurringType(RecurringType.MONTHLY);
                        rp.setDayOfMonth(day);
                        rp.setTimeOfDay(updatedMaintenance.getTime()); // or a list of times if you want
                        // ...
                        maintenance.addRecurringPattern(rp);
                    }
                } else {
                    // fallback to single pattern
                    maintenance.addRecurringPattern(baseRecurringPattern);
                }
                break;
            case YEARLY:
                if (updatedMaintenance.getYearlyDates() != null && !updatedMaintenance.getYearlyDates().isEmpty()) {
                    for (LocalDate date : updatedMaintenance.getYearlyDates()) {
                        RecurringPattern rp = new RecurringPattern();
                        rp.setMaintenance(maintenance);
                        rp.setInterval(updatedMaintenance.getInterval());
                        rp.setRecurringType(RecurringType.YEARLY);

                        // Example if you store (month, dayOfMonth) in RecurringPattern
                        rp.setMonthOfYear(date.getMonthValue());
                        rp.setDayOfMonth(date.getDayOfMonth());

                        rp.setTimeOfDay(updatedMaintenance.getTime());
                        maintenance.addRecurringPattern(rp);
                    }
                } else {
                    // fallback to single pattern
                    maintenance.addRecurringPattern(baseRecurringPattern);
                }
                break;
            case WEEKLY:
                char[] days = updatedMaintenance.getDaysOfWeekPattern().toCharArray();
                for (int i = 0; i < 7; i++) {
                    if (days[i] == '1') {
                        RecurringPattern rp = new RecurringPattern();
                        rp.setMaintenance(maintenance);
                        rp.setInterval(updatedMaintenance.getInterval());
                        rp.setRecurringType(updatedMaintenance.getRecurringType());
                        rp.setTimeOfDay(updatedMaintenance.getTime());
                        rp.setDayOfWeek(i);
                        maintenance.addRecurringPattern(rp);
                    }
                }
                break;
            default:
                break;

        }
        Maintenance savedMaintenance = maintenanceDAO.saveMaintenance(maintenance);

        eventPublisher.publishEvent(new MaintenanceAddedEvent(this, savedMaintenance));

        return savedMaintenance;
    }

    private Maintenance createBaseMaintenance(MaintenanceCreationDTO updatedMaintenance, Household household, Membership creator, Membership assignee) {
        Maintenance maintenance = new Maintenance();

        maintenance.setTitle(updatedMaintenance.getTitle());
        maintenance.setDescription(updatedMaintenance.getDescription());
        maintenance.setAssignee(assignee);
        maintenance.setCreator(creator);
        maintenance.setHouseHoLD(household);
        maintenance.setStartDate(updatedMaintenance.getStartDate());
        maintenance.setEndDate(updatedMaintenance.getEndDate());
        maintenance.setActive(true);
        maintenance.setResolution(false);
        return maintenance;
    }


    private RecurringPattern createBaseRecurringPattern(MaintenanceCreationDTO updatedMaintenance , Maintenance maintenance) {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setMaintenance(maintenance);
        recurringPattern.setInterval(updatedMaintenance.getInterval());
        recurringPattern.setRecurringType(updatedMaintenance.getRecurringType());
        recurringPattern.setTimeOfDay(updatedMaintenance.getTime());
        recurringPattern.setDayOfMonth(updatedMaintenance.getDayOfMonth());
        recurringPattern.setMonthOfYear(updatedMaintenance.getMonth());

        return recurringPattern;
    }

    @Override
    public Maintenance updateMaintenance(Long maintenanceId, MaintenanceCreationDTO updatedMaintenance, Household household, Membership creator, Membership assignee) {
        Maintenance maintenance1 = findMaintenanceById(maintenanceId)
                .orElseThrow(() -> new NonExistentEntityException("Maintenance with id: " + maintenanceId + " doesn't exist"));
        Maintenance maintenance = createBaseMaintenance(updatedMaintenance, household, creator, assignee);
        List<RecurringPattern> recurringPatterns=new ArrayList<>();
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setMaintenance(maintenance);
        recurringPattern.setInterval(updatedMaintenance.getInterval());
        recurringPattern.setRecurringType(updatedMaintenance.getRecurringType());
        maintenance.getRecurringPatterns().clear();
        switch (recurringPattern.getRecurringType()) {
            case DAILY:
                recurringPattern.setTimeOfDay(updatedMaintenance.getTime());
                recurringPatterns.add(recurringPattern);
                maintenance.setRecurringPatterns(recurringPatterns);
                break;
            case WEEKLY:
                char[] days = updatedMaintenance.getDaysOfWeekPattern().toCharArray();
                for (int i = 0; i < 7; i++) {
                    if (days[i] == '1') {
                        RecurringPattern rp = new RecurringPattern();
                        rp.setMaintenance(maintenance);
                        rp.setInterval(updatedMaintenance.getInterval());
                        rp.setRecurringType(updatedMaintenance.getRecurringType());
                        rp.setTimeOfDay(updatedMaintenance.getTime());
                        rp.setDayOfWeek(i);
                        recurringPatterns.add(rp);
                    }
                }
                maintenance.setRecurringPatterns(recurringPatterns);
                break;
            case MONTHLY:
                recurringPattern.setTimeOfDay(updatedMaintenance.getTime());
                recurringPattern.setDayOfMonth(updatedMaintenance.getDayOfMonth());
                recurringPatterns.add(recurringPattern);
                maintenance.setRecurringPatterns(recurringPatterns);
                break;
            case YEARLY:
                recurringPattern.setTimeOfDay(updatedMaintenance.getTime());
                recurringPattern.setDayOfMonth(updatedMaintenance.getDayOfMonth());
                recurringPattern.setMonthOfYear(updatedMaintenance.getMonth());
                recurringPatterns.add(recurringPattern);
                maintenance.setRecurringPatterns(recurringPatterns);
                break;
            default:
                break;

        }
        maintenance.setId(maintenanceId);
        return maintenanceDAO.saveMaintenance(maintenance);
    }

    @Override
    public void deleteMaintenance(Long maintenanceId){
        Maintenance maintenance = findMaintenanceById(maintenanceId)
                .orElseThrow(() -> new NonExistentEntityException("Maintenance with id: " + maintenanceId + " doesn't exist"));

        for(MaintenanceTask task: maintenance.getMaintenanceTasks()){
            maintenanceTaskService.deleteMaintenanceTask(task.getId());
        }
        maintenanceDAO.deleteMaintenanceById(maintenanceId);
    }

    @Override
    public Maintenance stopGeneratingMaintenance(Maintenance maintenance){
        return maintenance;
    }

    @Override
    public Maintenance addRecurringPattern(Long maintenanceId, RecurringPattern recurringPattern) {
        Maintenance maintenance = findMaintenanceById(maintenanceId)
                .orElseThrow(() -> new NonExistentEntityException("Maintenance with id: " + maintenanceId + " doesn't exist"));

        maintenance.addRecurringPattern(recurringPattern);
        return maintenanceDAO.saveMaintenance(maintenance);
    }

    @Override
    public Maintenance changeState(MaintenanceStateDTO updatedMaintenanceStateDTO, Long maintenanceId) {
        Maintenance maintenance = findMaintenanceById(maintenanceId)
                .orElseThrow(() -> new NonExistentEntityException("Maintenance with id: " + maintenanceId + " doesn't exist"));
        maintenance.setActive(updatedMaintenanceStateDTO.getTaskState());
        return maintenanceDAO.saveMaintenance(maintenance);
    }

    @Override
    public Optional<Maintenance> findMaintenanceById(Long id) {
        return maintenanceDAO.findMaintenanceById(id);
    }

    @Override
    public List<Maintenance> getAll(){
        return maintenanceDAO.getAllMaintenances();
    }

}
