package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.AuthorizationService;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.MaintenanceService;
import cz.cvut.fit.household.service.interfaces.MaintenanceTaskService;
import cz.cvut.fit.household.service.interfaces.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/household")
@RequiredArgsConstructor
@Slf4j
public class MaintenanceTaskController {
    private final HouseHoldService houseHoldService;
    private final MembershipService membershipService;
    private final MaintenanceService maintenanceService;
    private final MaintenanceTaskService maintenanceTaskService;
    private final AuthorizationService authorizationService;

    private static final String MAINTENANCE_ATTR = "maintenance";
    private static final String NEW_MAINTENANCE_TASK_ATTR = "newMaintenanceTask";
    private static final String HOUSEHOLD_ATTR = "household";
    private static final String HOUSEHOLD_ID_ATTR = "householdId";
    private static final String HOUSEHOLD_WITH_ID = "Household With id: ";
    private static final String MAINTENANCE_WITH_ID = "Maintenance With id: ";
    private static final String MAINTENANCE_TASK_WITH_ID = "Maintenance Task With id: ";
    private static final String DOES_NOT_EXIST = " doesn't exist";
    private static final String OWNER_PERMISSION = "permission";
    private static final String REJECT_DEADLINE = "rejectDeadline";
    private static final String ASSIGNEE = "assignee";
    private static final String MAINTENANCE_TASK_ATTR = "task";
    private static final String ALL_MAINTENANCE_TASK_ATTR = "tasks";

    private static final String MAINTENANCE_TASK_TIME = "timeDeadline";
    private static final String REDIRECTION_HOUSEHOLD = "redirect:/household/";
    private static final String EDIT_MAINTENANCE_TASK_FROM_VIEW = "maintenance/maintenanceTask/maintenanceTaskEditFromView";
    private static final String EDIT_MAINTENANCE_TASK_FROM_DETAIL_FROM_VIEW = "maintenance/maintenanceTask/maintenanceTaskEditFromTaskDetailsFromTaskView";
    private static final String EDIT_FROM_MAINTENANCE_DETAILS = "maintenance/maintenanceTask/maintenanceTaskEditFromMaintenanceDetails";
    private static final String RETURN_MAINTENANCE_TASK_DETAILS = "maintenance/maintenanceTask/maintenanceTaskDetails";
    private static final String RETURN_MAINTENANCE_TASK_VIEW = "maintenance/maintenanceTask/maintenanceTaskView";
    private static final String REDIRECT_HOUSEHOLD_PATH = "redirect:/household/";
    private static final String MAINTENANCE_PATH = "/maintenance/";
    private static final String MAINTENANCE_TASK_PATH = "/maintenanceTask/";
    private static final String MAINTENANCE_TASK_PATH_END = "/maintenanceTask";
    private static final String DETAIL_PATH = "/detail";
    private static final String REJECT_DEADLINE_MSG = "Deadline must not be a date before today and not be empty";

    String getTimeFromDate(Date date) {
        LocalTime time = date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return time.format(formatter);
    }

    Date addTimeToDate(String time, Date date) throws ParseException {
        String dateTimeString = new SimpleDateFormat("yyyy-MM-dd").format(date) + " " + time;
        // Set the parsed deadline to the task
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateTimeFormat.parse(dateTimeString);
    }

    void setTimeInMaintenanceTask(MaintenanceTask maintenanceTask, String time){
        try{
            maintenanceTask.setDeadline(addTimeToDate(time,maintenanceTask.getDeadline()));
        }catch (ParseException e){
            log.error("Cannot add time to date during edit of maintenance task with id - {}", maintenanceTask.getId());
        }
    }

    void addRejectToModel(String attribute, String message, Model model){

    }

    private void setUpEditModel(Long householdId, Long maintenanceTaskId, Model model) {
        Household houseHold = houseHoldService.findHouseHoldById(householdId).orElseThrow(() -> {
            log.error("Household with id - {} does not exist", householdId);
            return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
        });

        MaintenanceTask toEdit = maintenanceTaskService.findMaintenanceTaskByID(maintenanceTaskId).orElseThrow(() -> {
            log.error("Maintenance task with id - {} does not exist", maintenanceTaskId);
            return new NonExistentEntityException(MAINTENANCE_TASK_WITH_ID + maintenanceTaskId + DOES_NOT_EXIST);
        });

        List<Membership> members = membershipService.findAllMemberships().stream().filter(m1 -> m1.getStatus().equals(MembershipStatus.ACTIVE) && m1.getHousehold().equals(houseHold)).collect(Collectors.toList());

        Long maintenanceId = toEdit.getMaintenance().getId();

        Maintenance maintenance = maintenanceService.findMaintenanceById(maintenanceId).orElseThrow(() -> {
            log.error("Maintenance with id - {} does not exist ", maintenanceId);
            return new NonExistentEntityException(MAINTENANCE_WITH_ID + maintenanceId + DOES_NOT_EXIST);
        });
        toEdit.setMaintenance(maintenance);

        model.addAttribute(OWNER_PERMISSION, authorizationService.isOwner(houseHold));
        model.addAttribute(HOUSEHOLD_ATTR, houseHold);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(NEW_MAINTENANCE_TASK_ATTR, toEdit);
        model.addAttribute(MAINTENANCE_ATTR, maintenance);
        model.addAttribute(ASSIGNEE, members);
        model.addAttribute(MAINTENANCE_TASK_TIME, getTimeFromDate(toEdit.getDeadline()));
    }

    private String receiveEditModel(Long householdId, Long maintenanceTaskId, MaintenanceTask updatedMaintenanceTask, Model model, String successPath, String errorPath) {
        log.debug("Editing maintenance task with id - {}", maintenanceTaskId);
        Household houseHold = houseHoldService.findHouseHoldById(householdId).orElseThrow(() -> {
            log.error("Household with id - {} does not exist", maintenanceTaskId);
            return new NonExistentEntityException(MAINTENANCE_TASK_WITH_ID + maintenanceTaskId + DOES_NOT_EXIST);
        });

        if ((updatedMaintenanceTask.getDeadline() == null || updatedMaintenanceTask.getDeadline().before(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())))) {
            log.debug("Incorrect deadline when trying to update maintenance task with id - {}.", maintenanceTaskId);
            model.addAttribute(REJECT_DEADLINE, REJECT_DEADLINE_MSG);
            setUpEditModel(householdId, maintenanceTaskId, model);
            return errorPath;
        }

        MaintenanceTask toEdit = maintenanceTaskService.findMaintenanceTaskByID(maintenanceTaskId).orElseThrow(() -> {
            log.error("Maintenance task with id - {} does not exist", householdId);
            return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
        });

        toEdit.setDeadline(updatedMaintenanceTask.getDeadline());
        toEdit.getMaintenance().setAssignee(updatedMaintenanceTask.getMaintenance().getAssignee());
        toEdit.setTaskState(updatedMaintenanceTask.isTaskState());
        if (!updatedMaintenanceTask.isTaskState()) {
            toEdit.setClosingDate(updatedMaintenanceTask.getClosingDate());
        }
        maintenanceTaskService.updateMaintenanceTask(maintenanceTaskId, toEdit);

        return successPath;
    }

    private void getMaintenanceTaskDetail(Long householdId, Long maintenanceTaskId, Model model) {
        Household household = houseHoldService.findHouseHoldById(householdId).orElseThrow(() -> {
            log.error("Household with id - {} does not exist", householdId);
            return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
        });
        MaintenanceTask task = maintenanceTaskService.findMaintenanceTaskByID(maintenanceTaskId).orElseThrow(() -> {
            log.error("Maintenance task with id - {} does not exist", maintenanceTaskId);
            return new NonExistentEntityException(MAINTENANCE_TASK_WITH_ID + maintenanceTaskId + DOES_NOT_EXIST);
        });

        model.addAttribute(OWNER_PERMISSION, authorizationService.isOwner(household));
        model.addAttribute(HOUSEHOLD_ATTR, household);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(MAINTENANCE_TASK_ATTR, task);
    }

    @GetMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/edit")
    public String getEditMaintenanceTaskFromView(@PathVariable Long householdId, @PathVariable Long maintenanceTaskId, Model model) {
        setUpEditModel(householdId, maintenanceTaskId, model);
        return EDIT_MAINTENANCE_TASK_FROM_VIEW;
    }

    @PostMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/edit")
    public String postEditMaintenanceTaskFromView(@PathVariable Long householdId, @PathVariable Long maintenanceTaskId, @Valid @ModelAttribute(NEW_MAINTENANCE_TASK_ATTR) MaintenanceTask updatedMaintenanceTask, @RequestParam("time") String time, @RequestParam(value = "showAll", required = false) String showAll, Model model) {
        setTimeInMaintenanceTask(updatedMaintenanceTask, time);
        return receiveEditModel(householdId, maintenanceTaskId, updatedMaintenanceTask, model, REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_TASK_PATH_END + (showAll != null ? "?showAll=" + showAll : ""), EDIT_MAINTENANCE_TASK_FROM_VIEW);
    }

    @GetMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/detail/edit")
    public String getEditMaintenanceTaskFromDetailFromView(@PathVariable Long householdId, @PathVariable Long maintenanceTaskId, Model model) {
        setUpEditModel(householdId, maintenanceTaskId, model);
        return EDIT_MAINTENANCE_TASK_FROM_DETAIL_FROM_VIEW;
    }

    @PostMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/detail/edit")
    public String postEditMaintenanceTaskFromDetailFromView(@PathVariable Long householdId, @PathVariable Long maintenanceTaskId, @Valid @ModelAttribute(NEW_MAINTENANCE_TASK_ATTR) MaintenanceTask updatedMaintenanceTask,@RequestParam("time") String time, Model model) {
        setTimeInMaintenanceTask(updatedMaintenanceTask, time);
        return receiveEditModel(householdId, maintenanceTaskId, updatedMaintenanceTask, model, REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_TASK_PATH + maintenanceTaskId + DETAIL_PATH, EDIT_MAINTENANCE_TASK_FROM_DETAIL_FROM_VIEW);
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/maintenanceTask/{maintenanceTaskId}/edit")
    public String getEditMaintenanceTaskFromMaintenanceDetail(@PathVariable Long householdId, @PathVariable Long maintenanceTaskId, @PathVariable Long maintenanceId, Model model) {
        setUpEditModel(householdId, maintenanceTaskId, model);
        return EDIT_FROM_MAINTENANCE_DETAILS;
    }

    @PostMapping("/{householdId}/maintenance/{maintenanceId}/maintenanceTask/{maintenanceTaskId}/edit")
    public String postEditMaintenanceTaskFromMaintenanceDetail(@PathVariable Long householdId, @PathVariable Long maintenanceTaskId, @PathVariable Long maintenanceId, @Valid @ModelAttribute(NEW_MAINTENANCE_TASK_ATTR) MaintenanceTask updatedMaintenanceTask, @RequestParam("time") String time, Model model) {
        setTimeInMaintenanceTask(updatedMaintenanceTask, time);
        return receiveEditModel(householdId, maintenanceTaskId, updatedMaintenanceTask, model, REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_PATH + maintenanceId + DETAIL_PATH, EDIT_FROM_MAINTENANCE_DETAILS);
    }

    @GetMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/delete")
    public String deleteMaintenanceTaskFromView(@PathVariable Long householdId, @PathVariable Long maintenanceTaskId, @RequestParam(value = "showAll", required = false) String showAll) {
        maintenanceTaskService.deleteMaintenanceTask(maintenanceTaskId);
        return REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_TASK_PATH_END + (showAll != null ? "?showAll=" + showAll : null);
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/maintenanceTask/{maintenanceTaskId}/delete")
    public String deleteMaintenanceTaskFromMaintenanceDetail(@PathVariable Long householdId, @PathVariable Long maintenanceId, @PathVariable Long maintenanceTaskId) {
        maintenanceTaskService.deleteMaintenanceTask(maintenanceTaskId);
        return REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_PATH + maintenanceId + DETAIL_PATH;
    }

    @GetMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/detail/delete")
    public String deleteMaintenanceTaskFromDetailFromView(@PathVariable Long householdId, @PathVariable Long maintenanceTaskId, @RequestParam(value = "showAll", required = false) String showAll) {
        maintenanceTaskService.deleteMaintenanceTask(maintenanceTaskId);
        return REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_TASK_PATH_END + (showAll != null ? "?showAll=" + showAll : null);
    }

    @GetMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/detail")
    public String getMaintenanceTaskDetailInView(@PathVariable Long maintenanceTaskId, @PathVariable Long householdId, Model model) {
        getMaintenanceTaskDetail(householdId, maintenanceTaskId, model);
        return RETURN_MAINTENANCE_TASK_DETAILS;
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/maintenanceTask/{maintenanceTaskId}/open")
    public String openMaintenanceTaskFromMaintenanceDetail(@PathVariable Long maintenanceTaskId, @PathVariable Long householdId, @PathVariable Long maintenanceId) {
        maintenanceTaskService.openMaintenanceTask(maintenanceTaskId);
        return REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_PATH + maintenanceId + DETAIL_PATH;
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/maintenanceTask/{maintenanceTaskId}/close")
    public String closeMaintenanceTaskFromMaintenanceDetail(@PathVariable Long maintenanceTaskId, @PathVariable Long householdId, @PathVariable Long maintenanceId) {
        maintenanceTaskService.closeMaintenanceTask(maintenanceTaskId);
        return REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_PATH + maintenanceId + DETAIL_PATH;
    }

    @GetMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/detail/open")
    public String openMaintenanceTaskInDetailFromView(@PathVariable Long maintenanceTaskId, @PathVariable Long householdId) {
        maintenanceTaskService.openMaintenanceTask(maintenanceTaskId);
        return REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_TASK_PATH + maintenanceTaskId + DETAIL_PATH;
    }

    @GetMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/detail/close")
    public String closeMaintenanceTaskInDetailFromView(@PathVariable Long maintenanceTaskId, @PathVariable Long householdId) {
        maintenanceTaskService.closeMaintenanceTask(maintenanceTaskId);
        return REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_TASK_PATH + maintenanceTaskId + DETAIL_PATH;
    }

    @GetMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/open")
    public String openTaskFromView(@PathVariable Long maintenanceTaskId, @PathVariable String householdId, @RequestParam(value = "showAll", required = false) String showAll) {
        maintenanceTaskService.openMaintenanceTask(maintenanceTaskId);
        return REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_TASK_PATH_END + (showAll != null ? "?showAll=" + showAll : null);
    }

    @GetMapping("/{householdId}/maintenanceTask/{maintenanceTaskId}/close")
    public String closeTaskFromView(@PathVariable Long maintenanceTaskId, @PathVariable String householdId, @RequestParam(value = "showAll", required = false) String showAll) {
        maintenanceTaskService.closeMaintenanceTask(maintenanceTaskId);
        return REDIRECT_HOUSEHOLD_PATH + householdId + MAINTENANCE_TASK_PATH_END + (showAll != null ? "?showAll=" + showAll : null);
    }

    @GetMapping("/{householdId}/maintenanceTask")
    public String getUserMaintenanceTasksView(@PathVariable Long householdId, @RequestParam(value = "showAll", defaultValue = "false") boolean showAll, Model model) {

        Household household = houseHoldService.findHouseHoldById(householdId).orElseThrow(() -> {
            log.error("Household with id - {} does not exist", householdId);
            return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
        });

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Membership> membershipList = membershipService.findMembershipsByUsername(username);
        List<Membership> filteredMemberships = membershipList.stream().filter(membership -> Objects.equals(membership.getHousehold().getId(), householdId)).collect(Collectors.toList());
        if (filteredMemberships.size() != 1) {
            log.error("Membership with username : {} not found for household with id: {}", username, householdId);
            return REDIRECTION_HOUSEHOLD;
        }
        Membership member = filteredMemberships.get(0);
        List<MaintenanceTask> userTasks;
        if (!showAll) {
            userTasks = maintenanceTaskService.findMaintenanceTasksByAssignee(member.getId()).stream().filter(MaintenanceTask::isTaskState).collect(Collectors.toList());
        } else {
            userTasks = maintenanceTaskService.findMaintenanceTasksByAssignee(member.getId());
        }
        userTasks.sort(Comparator.comparing(MaintenanceTask::getDeadline));
        model.addAttribute(ALL_MAINTENANCE_TASK_ATTR, userTasks);
        model.addAttribute("showAll", showAll);
        model.addAttribute(HOUSEHOLD_ATTR, household);

        return RETURN_MAINTENANCE_TASK_VIEW;
    }
}
