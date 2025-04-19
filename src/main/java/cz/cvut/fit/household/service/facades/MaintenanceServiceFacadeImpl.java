package cz.cvut.fit.household.service.facades;

import cz.cvut.fit.household.config.MaintenanceConfig;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.events.OnInventoryChangeEvent;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.*;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.datamodel.enums.RecurringType;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.AuthorizationService;
import cz.cvut.fit.household.service.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceServiceFacadeImpl implements MaintenanceServiceFacade {

    private final HouseHoldService houseHoldService;
    private final MembershipService membershipService;
    private final MaintenanceService maintenanceService;
    private final MaintenanceTaskService taskService;
    private final AuthorizationService authorizationService;
    private final MaintenanceConfig maintenanceConfig;
    private final ApplicationEventPublisher eventPublisher;
    private static final int DAILY_PATTERN_LIMIT = 10;
    private static final int WEEKLY_PATTERN_LIMIT = 12;
    private static final int MONTHLY_PATTERN_LIMIT = 6;
    private static final int YEARLY_PATTERN_LIMIT = 3;

    private static final Dictionary<RecurringType, Integer> recurringTypeToMaxInstanceNumber;

    static {
        recurringTypeToMaxInstanceNumber = new Hashtable<>();
        recurringTypeToMaxInstanceNumber.put(RecurringType.DAILY, DAILY_PATTERN_LIMIT);
        recurringTypeToMaxInstanceNumber.put(RecurringType.WEEKLY, WEEKLY_PATTERN_LIMIT);
        recurringTypeToMaxInstanceNumber.put(RecurringType.MONTHLY, MONTHLY_PATTERN_LIMIT);
        recurringTypeToMaxInstanceNumber.put(RecurringType.YEARLY, YEARLY_PATTERN_LIMIT);
    }

    public Household getHouseHoldById(Long id, String exceptionMsg) {
        return houseHoldService.findHouseHoldById(id)
                .orElseThrow(() -> new NonExistentEntityException(exceptionMsg));
    }

    public Maintenance getMaintenanceById(Long id, String exceptionMsg) {
        Maintenance maintenance = maintenanceService.findMaintenanceById(id)
                .orElseThrow(() -> new NonExistentEntityException(exceptionMsg));
        generateMaintenanceTasks(maintenance, true);
        return maintenance;
    }

    public boolean isHouseholdOwner(Household houseHold) {
        return authorizationService.isOwner(houseHold);
    }

    public List<Membership> getMembershipList() {
        return membershipService
                .findAllMemberships()
                .stream()
                .filter(m1 -> m1.getStatus()
                        .equals(MembershipStatus.ACTIVE))
                .collect(Collectors.toList());
    }

    public List<Membership> getMembershipList(Household household) {
        return membershipService.findAllMemberships().
                stream().
                filter(m1 -> m1.getStatus().equals(MembershipStatus.ACTIVE)
                        &&
                        m1.getHousehold().equals(household)).collect(Collectors.toList());
    }

    public Maintenance addMaintenance(MaintenanceCreationDTO updatedMaintenance, Household houseHold) {
        Maintenance maintenance = maintenanceService.addMaintenance(updatedMaintenance, houseHold, updatedMaintenance.getCreator(),updatedMaintenance.getAssignee());
        maintenanceConfig.maintenanceEmailProcessing(maintenance.getAssignee(), maintenance.getTitle(), "Maintenance Created", "created");
        eventPublisher.publishEvent(new OnInventoryChangeEvent(maintenance.getHouseHoLD().getId(), "Maintenance with title: "
                + maintenance.getTitle()
                + ", assignee:- "
                + maintenance.getAssignee().getUser().getUsername()
                + " " + maintenance.getRecurringPatterns()
                + ", start date:- " + maintenance.getStartDate()
                + ", end date:- " + maintenance.getEndDate()));
        return maintenance;
    }

    public Maintenance updateMaintenance(Long maintenanceId, MaintenanceCreationDTO updatedMaintenance, Maintenance maintenanceOld, Household houseHold) {
        Maintenance newMaintenance = maintenanceService.updateMaintenance(maintenanceId,
                updatedMaintenance, houseHold,
                updatedMaintenance.getCreator(),
                updatedMaintenance.getAssignee());

        maintenanceConfig.maintenanceEmailProcessing(maintenanceOld.getAssignee(), maintenanceOld.getTitle(), "Maintenance with title: ", "was updated.");
        eventPublisher.publishEvent(new OnInventoryChangeEvent(houseHold.getId(), "Maintenance with title: " +
                maintenanceOld.getTitle() + " updated. Before" + " title: " + maintenanceOld.getTitle() + ", assignee:- " + maintenanceOld.getAssignee().getUser().getUsername() + ", frequency:- " + maintenanceOld.getRecurringPatterns() + ", endDate: " + maintenanceOld.getEndDate() + ". After title: " + newMaintenance.getTitle() + ", assignee : " + newMaintenance.getAssignee().getUser().getUsername() + ", frequency : " + newMaintenance.getRecurringPatterns()  + ", endDate" + newMaintenance.getEndDate()));
        return newMaintenance;
    }

    public void deleteMaintenance(Long maintenanceId, Long householdId) {
        Household household = getHouseHoldById(householdId, "Household With id: "+ householdId + " doesn't exist");
        Maintenance maintenance = getMaintenanceById(maintenanceId,"Maintenance With id: "+maintenanceId+" doesn't exist");
        maintenanceService.deleteMaintenance(maintenance.getId());
        eventPublisher.publishEvent(new OnInventoryChangeEvent(household.getId(), "Maintenance with title: " + maintenance.getTitle() + " deleted"));
    }

    public Maintenance changeMaintenanceState(MaintenanceStateDTO updatedMaintenanceStateDTO, Long maintenanceId, Maintenance maintenance) {
        String prevResolution = String.valueOf(maintenance.isResolution());
        String prevState = String.valueOf(maintenance.isResolution());
        Maintenance maintenanceChanged=maintenanceService.changeState(updatedMaintenanceStateDTO, maintenanceId);
        maintenanceConfig.maintenanceEmailProcessing(maintenanceChanged.getAssignee(), maintenanceChanged.getTitle(), "Maintenance Was Updated", "was updated");
        eventPublisher.publishEvent(new OnInventoryChangeEvent(maintenance.getHouseHoLD().getId(), "State of maintenance with title: " + maintenance.getTitle() + " was updated. Before " + "resolve: " + prevResolution + ", state: " + prevState + ". After resolution: " + maintenanceChanged.isResolution() + ", state: " + maintenanceChanged.isActive()));
        return maintenanceChanged;
    }

    public Maintenance stopGeneratingMaintenance(Maintenance maintenance) {
        boolean prevState = maintenance.isActive();
        Maintenance newMaintenance = maintenanceService.stopGeneratingMaintenance(maintenance);
        eventPublisher.publishEvent(new OnInventoryChangeEvent(maintenance.getHouseHoLD().getId(), "State of maintenance with title: " + maintenance.getTitle() + " was updated. Before generation state: " + prevState + ". After generation state: " + newMaintenance.isActive()));
        return newMaintenance;
    }

    public void generateMaintenanceTasks(Maintenance maintenance, boolean update){
        if(maintenance.isActive()){
            List<RecurringPattern> recurringPatterns = maintenance.getRecurringPatterns();
            for (RecurringPattern pattern: recurringPatterns)
            {
                processRecurringPattern(maintenance, pattern, update);
            }
        }
    }

    private void processRecurringPattern(Maintenance maintenance, RecurringPattern pattern, boolean update) {
        Date endDate = maintenance.getEndDate(); // Default end date is the maintenance end date
        processRecurringPattern(maintenance, pattern, endDate, update);
    }

    private void processRecurringPattern(Maintenance maintenance, RecurringPattern pattern, Date endDate, boolean update) {
        Date maintenanceStartDate = maintenance.getStartDate();
        Date currentDate = new Date();

        Date generationStartDate;
        Date generationEndDate = endDate.before(maintenance.getEndDate()) ? endDate : maintenance.getEndDate();
        Integer maxInstanceNumber = recurringTypeToMaxInstanceNumber.get(pattern.getRecurringType());

        if (update) {
            List<MaintenanceTask> maintenanceTasks = maintenance.getMaintenanceTasks();
            if (maintenanceTasks.isEmpty()) {
                return; //old maintenance, doesn't contain maintenance task property
            }
            maintenanceTasks.sort(Comparator.comparing(MaintenanceTask::getDeadline));
            MaintenanceTask lastTask = maintenanceTasks.get(maintenanceTasks.size() - 1);
            Date lastTaskDate = lastTask.getDeadline();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastTaskDate);

            int interval = pattern.getInterval();

            switch (pattern.getRecurringType()) {
                case DAILY:
                    calendar.add(Calendar.DAY_OF_YEAR, interval);
                    break;

                case WEEKLY:
                    calendar.add(Calendar.WEEK_OF_YEAR, interval);
                    break;

                case MONTHLY:
                    calendar.add(Calendar.MONTH, interval);
                    break;

                case YEARLY:
                    calendar.add(Calendar.YEAR, interval);
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported recurring type: " + pattern.getRecurringType());
            }

            generationStartDate = calendar.getTime();
            generateMaintenanceTasks(maintenance, pattern, generationStartDate, generationEndDate, maxInstanceNumber, countActiveMaintenanceTasks(maintenanceTasks));
            return;
        }
        generationStartDate = maintenanceStartDate.before(currentDate) ? currentDate : maintenanceStartDate;
        generateMaintenanceTasks(maintenance, pattern, generationStartDate, generationEndDate, maxInstanceNumber, 0L);
    }

    private Long countActiveMaintenanceTasks(List<MaintenanceTask> maintenanceTasks) {
        Long counter = 0L;
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        for (MaintenanceTask task : maintenanceTasks) {
            if (task.isTaskState() && !task.getDeadline().before(today)) {
                counter++;
            }
        }
        return counter;
    }

    private void generateMaintenanceTasks(
            Maintenance maintenance,
            RecurringPattern pattern,
            Date generationStartDate,
            Date generationEndDate,
            Integer maxInstanceNumber,
            Long currentNumber
    ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(generationStartDate);

        int interval = pattern.getInterval();

        Long counter = currentNumber;
        switch (pattern.getRecurringType()) {
            case DAILY:
                log.debug("Generating maintenance tasks for daily");
                while (generationStartDate.before(generationEndDate) && counter < maxInstanceNumber) {
                    addMaintenanceTaskToDB(maintenance, calendar.getTime(), pattern);
                    calendar.add(Calendar.DAY_OF_YEAR, interval);
                    generationStartDate = calendar.getTime();
                    counter++;
                }
                break;

            case WEEKLY:
                log.debug("Generating maintenance tasks for weekly");
                setNextClosestDayOfWeek(calendar, pattern.getDayOfWeek());
                while (calendar.getTime().before(generationEndDate) && counter < maxInstanceNumber) {
                    addMaintenanceTaskToDB(maintenance, calendar.getTime(), pattern);
                    calendar.add(Calendar.WEEK_OF_YEAR, interval);
                    counter++;
                }
                break;

            case MONTHLY:
                log.debug("Generating maintenance tasks for monthly");
                while (generationStartDate.before(generationEndDate) && counter < maxInstanceNumber) {
                    if (pattern.getDayOfMonth() > 0) {
                        calendar.set(Calendar.DAY_OF_MONTH, pattern.getDayOfMonth());
                    }
                    addMaintenanceTaskToDB(maintenance, calendar.getTime(), pattern);
                    calendar.add(Calendar.MONTH, interval);
                    generationStartDate = calendar.getTime();
                    counter++;
                }
                break;

            case YEARLY:
                log.debug("Generating maintenance tasks for yearly");
                while (generationStartDate.before(generationEndDate) && counter < maxInstanceNumber) {
                    calendar.set(Calendar.MONTH, pattern.getMonthOfYear() - 1); // Adjust for zero-indexed months
                    if (pattern.getDayOfMonth() > 0) {
                        calendar.set(Calendar.DAY_OF_MONTH, pattern.getDayOfMonth());
                    }
                    addMaintenanceTaskToDB(maintenance, calendar.getTime(), pattern);
                    calendar.add(Calendar.YEAR, interval);
                    generationStartDate = calendar.getTime();
                    counter++;
                }
                break;

            default:
                log.error("Unsupported recurring type: {}", pattern.getRecurringType());
                throw new UnsupportedOperationException("Unsupported recurring type: " + pattern.getRecurringType());
        }
    }

    private void setNextClosestDayOfWeek(Calendar calendar, int targetDayOfWeek) {
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int adjustedTargetDayOfWeek = adjustDayOfWeek(targetDayOfWeek);
        int daysToAdd = adjustedTargetDayOfWeek - currentDayOfWeek;

        if (daysToAdd <= 0) {
            daysToAdd += 7; // If the target day is today or earlier, move to next week
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd); // Move to the next occurrence of the target day
    }

    /*
     * My system | library Calendar | day of week
     *   0               2             Monday
     *   1               3             Tuesday
     *   .               .
     *   5               7             Saturday
     *   6               1             Sunday
     *  */
    private int adjustDayOfWeek(int mySystemDayOfWeek) {
        return (mySystemDayOfWeek + 2) % 7;
    }

    //TODO: need to set some parameter to Maintenance entity to define the time to complete task
    private void addMaintenanceTaskToDB(Maintenance maintenance, Date openingDate, RecurringPattern pattern) {
        MaintenanceTask maintenanceTask = createMaintenanceTask(maintenance, openingDate, pattern);
        taskService.addMaintenanceTask(maintenanceTask); // Save to the database
        log.info("Created maintenance task {} for maintenance with ID {}", maintenanceTask, maintenanceTask.getMaintenance().getId());
    }

    private MaintenanceTask createMaintenanceTask(Maintenance maintenance, Date openingDate, RecurringPattern pattern) {
        MaintenanceTask maintenanceTask = new MaintenanceTask();
        maintenanceTask.setMaintenance(maintenance);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(openingDate);
        calendar.set(Calendar.HOUR_OF_DAY, pattern.getTimeOfDay().getHour()); //add deadline hours
        calendar.set(Calendar.MINUTE, pattern.getTimeOfDay().getMinute()); //add deadline minutes
        maintenanceTask.setDeadline(calendar.getTime());
        maintenanceTask.setTaskState(true); // Set the task state to true
        log.debug("Maintenance task generated for maintenance with id {}: {}", maintenance.getId(), maintenanceTask);
        return maintenanceTask;
    }
}
