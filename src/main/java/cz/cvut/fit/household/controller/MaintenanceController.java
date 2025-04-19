package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.*;
import cz.cvut.fit.household.datamodel.enums.RecurringType;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.MaintenanceServiceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Controller
@RequestMapping("/household")
@RequiredArgsConstructor
@Slf4j
public class MaintenanceController {

    private final MaintenanceServiceFacade maintenanceServiceFacade;


    private static final String MAINTENANCE_ATTR = "maintenance";
    private static final String NEW_MAINTENANCE_ATTR = "newMaintenance";
    private static final String TASK_STATE_ATTR = "taskState";
    private static final String NEW_STATE_ATTR = "newState";
    private static final String HOUSEHOLD_ATTR = "household";
    private static final String HOUSEHOLD_ID_ATTR = "householdId";
    private static final String HOUSEHOLD_WITH_ID = "Household With id: ";
    private static final String MAINTENANCE_WITH_ID = "Maintenance With id: ";
    private static final String DOES_NOT_EXIST = " doesn't exist";
    private static final String OWNER_PERMISSION = "permission";
    private static final String REJECT_FREQUENCY = "rejectFrequency";
    private static final String REJECT_DEADLINE = "rejectDeadline";
    private static final String REJECT_END_DATE = "rejectEndDate";
    private static final String REJECT_START_DATE = "rejectStartDate";
    private static final String REJECT_INTERVAL = "rejectInterval";
    private static final String REJECT_DAY_OF_MONTH = "rejectDayOfMonth";
    private static final String REJECT_WEEK_PATTERN = "rejectWeekPattern";
    private static final String REJECT_TIME="rejectTime";
    private static final String REJECT_MESSAGE = "rejectMessage";
    private static final String ASSIGNEE = "assignee";
    private static final String MAINTENANCE_TASK_ATTR = "maintenanceTask";
    private static final String REDIRECTION_HOUSEHOLD = "redirect:/household/";
    private static final String RETURN_MAINTENANCE_DETAILS = "maintenance/maintenanceDetails";
    private static final String REDIRECT_TO_MAINTENANCE_VIEW = "redirect:/household/{householdId}/maintenance";
    private static final String REDIRECT_TO_MAIN_PAGE = "redirect:/welcome";
    private static final String RETURN_MAINTENANCE_VIEW = "maintenance/maintenanceView";
    private static final String RETURN_ADD_MAINTENANCE = "maintenance/addMaintenance";
    private static final String RETURN_EDIT_IN_MAINTENANCE = "maintenance/edit/maintenanceEditIn";
    private static final String RETURN_EDIT_MAINTENANCE = "maintenance/edit/maintenanceEdit";
    private static final String RETURN_STATE_EDIT_MAINTENANCE = "maintenance/edit/maintenanceStateEdit";
    private static final String REJECT_END_DATE_MSG = "End Date must not be a date before today and not be empty";
    private static final String REJECT_MESSAGE_MSG = "Task State cannot be set to close when it is not resolved yet";
    private static final String REJECT_INTERVAL_MSG = "The interval should be higher than zero";
    private static final String REJECT_WEEK_PATTERN_MSG = "At least one day should be chosen";
    private static final String REJECT_DAY_OF_MONTH_MSG = "The day should be higher than zero";
    private static final String REJECT_START_DATE_MSG = "Start Date must not be a date before today and not be empty";
    private static final String REJECT_TIME_MSG="Time should be chosen";
    private static final String ALL_GOOD="All validations passed";

    private String getHouseholdNotExistIdMsg(Long id) {
        return HOUSEHOLD_WITH_ID + id + DOES_NOT_EXIST;
    }

    private String getMaintenanceNotExitsIdMsg(Long id) {
        return MAINTENANCE_WITH_ID + id + DOES_NOT_EXIST;
    }

    private void setMaintenanceModel(MaintenanceCreationDTO maintenanceCreationDTO, Model model, Household household, List<Membership> membershipList, Long householdId) {
        model.addAttribute(OWNER_PERMISSION, maintenanceServiceFacade.isHouseholdOwner(household));
        model.addAttribute(HOUSEHOLD_ATTR, household);
        model.addAttribute(ASSIGNEE, membershipList);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(MAINTENANCE_ATTR, maintenanceCreationDTO);
    }

    private String validateMaintenance(MaintenanceCreationDTO updatedMaintenance) {

        if (updatedMaintenance.getInterval() <= 0) {
            return REJECT_INTERVAL;
        }

        if (updatedMaintenance.getEndDate() == null
                || updatedMaintenance.getEndDate().before(Date.from(LocalDate.now()
                .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))) {
            return REJECT_END_DATE;
        }

        if (updatedMaintenance.getStartDate() == null
                || updatedMaintenance.getStartDate().before(Date.from(LocalDate.now()
                .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))) {
            return REJECT_START_DATE;
        }

        // For daily, we might use either a single "time" or multiple "dailyTimes".
        // So if both are null, we reject.
        if (updatedMaintenance.getTime() == null && (updatedMaintenance.getDailyTimes() == null
                || updatedMaintenance.getDailyTimes().isEmpty())) {
            return REJECT_TIME;
        }

        // For weekly
        if (updatedMaintenance.getRecurringType() == RecurringType.WEEKLY
                && (updatedMaintenance.getDaysOfWeekPattern() == null
                || updatedMaintenance.getDaysOfWeekPattern().isEmpty())) {
            return REJECT_WEEK_PATTERN;
        }

        // For monthly and yearly, we require EITHER a single dayOfMonth > 0
        // OR a non-empty list of days/dates.

        if (updatedMaintenance.getRecurringType() == RecurringType.MONTHLY) {
            // If dayOfMonth <= 0 AND daysOfMonth is empty => reject
            boolean invalidSingleDay = updatedMaintenance.getDayOfMonth() <= 0;
            boolean noMultiDays = (updatedMaintenance.getDaysOfMonth() == null
                    || updatedMaintenance.getDaysOfMonth().isEmpty());
            if (invalidSingleDay && noMultiDays) {
                return REJECT_DAY_OF_MONTH;
            }
        }

        if (updatedMaintenance.getRecurringType() == RecurringType.YEARLY) {
            // If dayOfMonth <= 0 AND yearlyDates is empty => reject
            boolean invalidSingleDay = updatedMaintenance.getDayOfMonth() <= 0;
            boolean noYearlyDates = (updatedMaintenance.getYearlyDates() == null
                    || updatedMaintenance.getYearlyDates().isEmpty());
            if (invalidSingleDay && noYearlyDates) {
                return REJECT_DAY_OF_MONTH;
            }
        }

        return ALL_GOOD;
    }


    private void setValidMaintenanceModel(MaintenanceCreationDTO maintenanceCreationDTO, Model model, Household household, List<Membership> membershipList, Long householdId, String error) {
        setMaintenanceModel(maintenanceCreationDTO, model, household, membershipList, householdId);

        model.addAttribute(REJECT_FREQUENCY, null);
        model.addAttribute(REJECT_DEADLINE, null);
        model.addAttribute(REJECT_END_DATE, null);
        model.addAttribute(REJECT_START_DATE, null);
        model.addAttribute(REJECT_DAY_OF_MONTH, null);
        model.addAttribute(REJECT_WEEK_PATTERN, null);
        model.addAttribute(REJECT_TIME, null);

        switch (error) {
            case REJECT_INTERVAL:
                model.addAttribute(REJECT_INTERVAL, REJECT_INTERVAL_MSG);
                break;
            case REJECT_END_DATE:
                model.addAttribute(REJECT_END_DATE, REJECT_END_DATE_MSG);
                break;
            case REJECT_START_DATE:
                model.addAttribute(REJECT_START_DATE, REJECT_START_DATE_MSG);
                break;
            case REJECT_TIME:
                model.addAttribute(REJECT_TIME,REJECT_TIME_MSG);
                break;
            case REJECT_DAY_OF_MONTH:
                model.addAttribute(REJECT_DAY_OF_MONTH, REJECT_DAY_OF_MONTH_MSG);
                break;
            case REJECT_WEEK_PATTERN:
                model.addAttribute(REJECT_WEEK_PATTERN, REJECT_WEEK_PATTERN_MSG);
                break;
        }
    }

    private void setMaintenanceView(Model model, Household household, Long householdId) {
        model.addAttribute(OWNER_PERMISSION,
                maintenanceServiceFacade.isHouseholdOwner(household));
        model.addAttribute(HOUSEHOLD_ATTR, household);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(MAINTENANCE_ATTR, household.getMaintenances());
    }

    private void setChangeStateModel(Model model, Household household, Long householdId, Maintenance old, MaintenanceStateDTO updatedDTO, String rejectMsg) {
        model.addAttribute(OWNER_PERMISSION, maintenanceServiceFacade.isHouseholdOwner(household));
        model.addAttribute(HOUSEHOLD_ATTR, household);
        model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
        model.addAttribute(MAINTENANCE_ATTR, old);
        model.addAttribute(NEW_STATE_ATTR, updatedDTO);
        model.addAttribute(REJECT_MESSAGE, rejectMsg);
    }

    private MaintenanceTaskCreationDTO fillMaintenanceTaskCreationDTO(MaintenanceTask maintenanceTask){
        MaintenanceTaskCreationDTO dto = new MaintenanceTaskCreationDTO();
        dto.setDeadline(maintenanceTask.getDeadline());
        dto.setClosingDate(maintenanceTask.getClosingDate());
        dto.setTaskState(maintenanceTask.isTaskState());
        dto.setTaskId(maintenanceTask.getId());
        return dto;
    }

    private MaintenanceCreationDTO fillDto(Maintenance maintenance){
        MaintenanceCreationDTO dto = new MaintenanceCreationDTO();

        // Basic fields from Maintenance itself
        dto.setTitle(maintenance.getTitle());
        dto.setDescription(maintenance.getDescription());
        dto.setAssignee(maintenance.getAssignee());
        dto.setCreator(maintenance.getCreator());
        dto.setEndDate(maintenance.getEndDate());
        dto.setStartDate(maintenance.getStartDate());
        // If your old code had "month" for yearly usage, you can keep it or convert it.
        // dto.setMonth(...);

        // 1) Figure out the recurring type (assuming all patterns are the same type).
        RecurringType type = maintenance.getRecurringPatterns().get(0).getRecurringType();
        dto.setRecurringType(type);

        // 2) Common fields that might be identical across patterns (interval, etc.)
        //    We'll assume the first pattern has the "main" interval/time fields.
        RecurringPattern firstPattern = maintenance.getRecurringPatterns().get(0);
        dto.setInterval(firstPattern.getInterval());
        // if you keep a single time for e.g. weekly, monthly, yearly:
        dto.setTime(firstPattern.getTimeOfDay());
        // if you keep a single dayOfMonth, etc. for backwards compatibility:
        dto.setDayOfMonth(firstPattern.getDayOfMonth());
        dto.setMonth(firstPattern.getMonthOfYear());

        // 3) Now process all patterns to fill your new multi-value lists
        //    or combine data as needed:
        switch (type) {
            case DAILY:
                // Suppose we store each daily time in a separate RecurringPattern
                // So gather all those times:
                List<LocalTime> dailyTimes = new ArrayList<>();
                for (RecurringPattern rp : maintenance.getRecurringPatterns()) {
                    if (rp.getRecurringType() == RecurringType.DAILY) {
                        dailyTimes.add(rp.getTimeOfDay());
                    }
                }
                dto.setDailyTimes(dailyTimes);
                break;

            case WEEKLY:
                // For weekly, you have multiple patterns each with dayOfWeek.
                // Create a 7-char pattern, default "0000000".
                char[] weekPatternArray = "0000000".toCharArray();
                for (RecurringPattern rp : maintenance.getRecurringPatterns()) {
                    if (rp.getRecurringType() == RecurringType.WEEKLY) {
                        int dayOfWeek = rp.getDayOfWeek();
                        if (dayOfWeek >= 0 && dayOfWeek < 7) {
                            weekPatternArray[dayOfWeek] = '1';
                        }
                    }
                }
                String weekPattern = new String(weekPatternArray);
                dto.setDaysOfWeekPattern(weekPattern);
                break;

            case MONTHLY:
                // Possibly multiple RecurringPatterns, each with a different dayOfMonth
                List<Integer> daysOfMonth = new ArrayList<>();
                for (RecurringPattern rp : maintenance.getRecurringPatterns()) {
                    if (rp.getRecurringType() == RecurringType.MONTHLY) {
                        daysOfMonth.add(rp.getDayOfMonth());
                    }
                }
                dto.setDaysOfMonth(daysOfMonth);
                // If you want to store a single time in the old field for monthly:
                // dto.setTime(firstPattern.getTimeOfDay());
                break;

            case YEARLY:
                // Possibly multiple patterns each with (monthOfYear, dayOfMonth).
                // If your new DTO has a "List<LocalDate> yearlyDates", let's fill it:
                List<LocalDate> yearlyDates = new ArrayList<>();
                for (RecurringPattern rp : maintenance.getRecurringPatterns()) {
                    if (rp.getRecurringType() == RecurringType.YEARLY) {
                        // Suppose we store month in rp.getMonthOfYear() and day in rp.getDayOfMonth()
                        int m = rp.getMonthOfYear();
                        int d = rp.getDayOfMonth();
                        // We can craft a LocalDate with a dummy year (like year 2000)
                        LocalDate date = LocalDate.of(2000, m, d);
                        yearlyDates.add(date);
                    }
                }
                dto.setYearlyDates(yearlyDates);
                break;

            default:
                // If you have a NONE pattern or unknown, no extra fields
                break;
        }

        // 4) Fill tasks if any
        List<MaintenanceTaskCreationDTO> tmpList = new ArrayList<>();
        if (maintenance.getMaintenanceTasks() != null) {
            for (MaintenanceTask task : maintenance.getMaintenanceTasks()) {
                tmpList.add(fillMaintenanceTaskCreationDTO(task));
            }
        }
        dto.setTasks(tmpList);

        return dto;
    }

    @GetMapping("/{householdId}/maintenance")
    public String getMaintenancesPage(@PathVariable Long householdId, Model model) {
        try {
            Household household = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            setMaintenanceView(model, household, householdId);
            return RETURN_MAINTENANCE_VIEW;
        } catch (Exception e) {
            log.error("ERROR" + e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @GetMapping("/{householdId}/maintenance/add")
    public String getAddMaintenancePage(@PathVariable Long householdId, Model model) {
        try {
            Household household = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            List<Membership> membershipList = maintenanceServiceFacade.getMembershipList(household);
            MaintenanceCreationDTO maintenanceCreationDTO = new MaintenanceCreationDTO();
            maintenanceCreationDTO.setCreator(membershipList.get(0));
            setMaintenanceModel(maintenanceCreationDTO, model, household, membershipList, householdId);
            return RETURN_ADD_MAINTENANCE;
        } catch (Exception e) {
            log.error("ERROR" + e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAIN_PAGE;
        }
    }

    @PostMapping("/{householdId}/maintenance")
    public String postAddMaintenancePage(@PathVariable Long householdId,
                                         @Valid @ModelAttribute("updatedMaintenance") MaintenanceCreationDTO updatedMaintenance,
                                         BindingResult result,
                                         Model model) {
        try {
            log.debug("Creating maintenance - {} in household with id - {}" , updatedMaintenance , householdId);
            List<Membership> membershipList = maintenanceServiceFacade.getMembershipList();
            Household household = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));

            String hasError = validateMaintenance(updatedMaintenance);
            if (result.hasErrors() || !Objects.equals(hasError, ALL_GOOD)) {
                log.error("Error occurred during creating maintenance - " + result.getAllErrors());
                setValidMaintenanceModel(updatedMaintenance, model, household, membershipList, householdId, hasError);
                return RETURN_ADD_MAINTENANCE;
            } else {
                maintenanceServiceFacade.addMaintenance(updatedMaintenance, household);
                log.info("Maintenance - {} has been created in household with id - {}" , updatedMaintenance , householdId);
                setMaintenanceView(model, household, householdId);
                return REDIRECT_TO_MAINTENANCE_VIEW;
            }
        } catch (RuntimeException e) {
            log.error("Error occurred during creating maintenance - " + e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAIN_PAGE;
        }
    }



    @GetMapping("/{householdId}/maintenance/{maintenanceId}/detail/edit")
    public String getEditMaintenanceInPage(@PathVariable Long householdId, @PathVariable Long maintenanceId, Model model) {
        try {
            Household household = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));
            List<Membership> membershipList = maintenanceServiceFacade.getMembershipList(household);
            MaintenanceCreationDTO dto=fillDto(maintenance);

            setMaintenanceModel(dto,model,household,membershipList,householdId);

            return RETURN_EDIT_IN_MAINTENANCE;
        } catch (NonExistentEntityException e) {
            log.error(e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAINTENANCE_VIEW;
        }
    }

    @PostMapping("/{householdId}/maintenance/{maintenanceId}/detail/edit")
    public String postEditMaintenanceInPage(@PathVariable Long householdId, @PathVariable Long maintenanceId, @Valid @ModelAttribute("updatedMaintenance") MaintenanceCreationDTO updatedMaintenance, BindingResult result, Model model) {
        try {
            log.debug("Editing maintenance with id - {} in household with id -{} as new maintenance - {}" , maintenanceId , householdId , updatedMaintenance);
            List<Membership> membershipList = maintenanceServiceFacade.getMembershipList();

            Household houseHold = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));

            try {
                String hasError = validateMaintenance(updatedMaintenance);
                if (result.hasErrors() || !Objects.equals(hasError, ALL_GOOD)) {
                    log.error(result.getAllErrors().toString());
                    setValidMaintenanceModel(updatedMaintenance, model, houseHold, membershipList, householdId, hasError);
                    model.addAttribute("maintenance", updatedMaintenance);
                    return RETURN_EDIT_IN_MAINTENANCE;
                } else {
                    maintenanceServiceFacade.updateMaintenance(maintenanceId,updatedMaintenance,maintenance,houseHold);
                    log.info("Maintenance with id -{} in household with id - {} has been updated as {}" , maintenanceId , householdId , updatedMaintenance);
                    model.addAttribute(OWNER_PERMISSION, maintenanceServiceFacade.isHouseholdOwner(houseHold));
                    model.addAttribute(HOUSEHOLD_ATTR, houseHold);
                    model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
                    model.addAttribute(MAINTENANCE_ATTR, maintenance);
                    model.addAttribute(MAINTENANCE_TASK_ATTR, maintenance.getMaintenanceTasks());
                    return "redirect:/household/{householdId}/maintenance/{maintenanceId}/detail";
                }
            } catch (RuntimeException e) {
                log.error("Error occurred during updating maintenance {}", e.getMessage());
                setValidMaintenanceModel(updatedMaintenance, model, houseHold, membershipList, householdId, validateMaintenance(updatedMaintenance));
                model.addAttribute(MAINTENANCE_ATTR, maintenance);
                model.addAttribute(NEW_MAINTENANCE_ATTR, updatedMaintenance);
                return RETURN_EDIT_IN_MAINTENANCE;
            }
        } catch (NonExistentEntityException e) {
            log.error(e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAINTENANCE_VIEW;
        }
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/edit")
    public String getEditMaintenancePage(@PathVariable Long householdId, @PathVariable Long maintenanceId, Model model) {
        try {
            Household household = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));
            List<Membership> membershipList = maintenanceServiceFacade.getMembershipList(household);
            MaintenanceCreationDTO dto=fillDto(maintenance);

            setMaintenanceModel(dto,model,household,membershipList,householdId);
            return RETURN_EDIT_MAINTENANCE;
        } catch (NonExistentEntityException e) {
            log.error(e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAINTENANCE_VIEW;
        }
    }

    @PostMapping("/{householdId}/maintenance/{maintenanceId}/edit")
    public String postEditMaintenancePage(@PathVariable Long householdId, @PathVariable Long maintenanceId, @Valid @ModelAttribute("updatedMaintenance") MaintenanceCreationDTO updatedMaintenance, BindingResult result, Model model) {
        try {
            List<Membership> membershipList = maintenanceServiceFacade.getMembershipList();
            Household houseHold = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));
            try {
                String hasError = validateMaintenance(updatedMaintenance);
                if (result.hasErrors() || !Objects.equals(hasError, ALL_GOOD)) {
                    log.error(result.getAllErrors().toString());
                    setValidMaintenanceModel(updatedMaintenance, model, houseHold, membershipList, householdId, hasError);
                    model.addAttribute("maintenance", updatedMaintenance);
                    return RETURN_EDIT_MAINTENANCE;
                } else {
                    maintenanceServiceFacade.updateMaintenance(maintenanceId,updatedMaintenance,maintenance,houseHold);
                    setMaintenanceView(model, houseHold, householdId);
                    return REDIRECT_TO_MAINTENANCE_VIEW;
                }
            } catch (RuntimeException e) {
                log.error(e.getMessage());
                setValidMaintenanceModel(updatedMaintenance, model, houseHold, membershipList, householdId, validateMaintenance(updatedMaintenance));
                model.addAttribute("maintenance", updatedMaintenance);
                return RETURN_EDIT_MAINTENANCE;
            }
        } catch (NonExistentEntityException e) {
            log.error(e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAINTENANCE_VIEW;
        }
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/delete")
    public String deleteMaintenance(@PathVariable Long householdId, @PathVariable Long maintenanceId, Model model) {
        try {
            log.debug("Deleting maintenance with id - {} in household with id - {}" , maintenanceId , householdId);
            Household houseHold = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            maintenanceServiceFacade.deleteMaintenance(maintenanceId,houseHold.getId());
            setMaintenanceView(model, houseHold, householdId);
            return REDIRECT_TO_MAINTENANCE_VIEW;
        } catch (NonExistentEntityException e) {
            log.error(e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAINTENANCE_VIEW;
        }
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/detail")
    public String renderMaintenanceDetailsPage(@PathVariable Long householdId, @PathVariable Long maintenanceId, Model model) {
        try {
            Household household = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));
            List<Membership> membershipList = maintenanceServiceFacade.getMembershipList(household);
            maintenance.getMaintenanceTasks().sort(Comparator.comparing(MaintenanceTask::getDeadline));
            MaintenanceCreationDTO dto=fillDto(maintenance);
            setMaintenanceModel(dto,model,household,membershipList,householdId);
            MaintenanceStateDTO maintenanceStateDTO = new MaintenanceStateDTO();
            maintenanceStateDTO.setTaskState(maintenance.isActive());
            maintenanceStateDTO.setTaskResolution(maintenance.isResolution());
            model.addAttribute(TASK_STATE_ATTR, maintenanceStateDTO);
            return RETURN_MAINTENANCE_DETAILS;
        } catch (NonExistentEntityException e) {
            log.error(e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAINTENANCE_VIEW;
        }
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/changeState")
    public String renderChangeStatePage(@PathVariable Long householdId, @PathVariable Long maintenanceId, Model model) {
        try {
            Household houseHold = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));
            MaintenanceStateDTO maintenanceStateDTO = new MaintenanceStateDTO();
            maintenanceStateDTO.setTaskState(maintenance.isActive());
            maintenanceStateDTO.setTaskResolution(maintenance.isResolution());
            setChangeStateModel(model, houseHold, householdId, maintenance, maintenanceStateDTO, null);
            return RETURN_STATE_EDIT_MAINTENANCE;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAINTENANCE_VIEW;
        }
    }

    @PostMapping("/{householdId}/maintenance/{maintenanceId}/changeState")
    public String getChangeMaintenanceState(@PathVariable Long householdId, @PathVariable Long maintenanceId, @Valid @ModelAttribute("updatedMaintenanceStateDTO") MaintenanceStateDTO updatedMaintenanceStateDTO, BindingResult result, Model model) {
        try {
            log.debug("Changing state of  maintenance with id - {} in household with id - {}" , maintenanceId ,householdId);
            Household houseHold = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));
            try {
                if ((!updatedMaintenanceStateDTO.getTaskResolution() && updatedMaintenanceStateDTO.getTaskState()) || result.hasErrors()) {
                    log.error("error occurred during changing maintenance state of maintenance with id - {} in household with id - {} " , maintenanceId  , householdId + REJECT_MESSAGE_MSG) ;
                    setChangeStateModel(model, houseHold, householdId, maintenance, updatedMaintenanceStateDTO, REJECT_MESSAGE_MSG);
                    result.rejectValue("taskState", "error", REJECT_MESSAGE_MSG);
                    return RETURN_STATE_EDIT_MAINTENANCE;
                }
                Maintenance maintenanceChanged = maintenanceServiceFacade.changeMaintenanceState(updatedMaintenanceStateDTO, maintenanceId,maintenance);
                model.addAttribute(OWNER_PERMISSION, maintenanceServiceFacade.isHouseholdOwner(houseHold));
                model.addAttribute(HOUSEHOLD_ATTR, houseHold);
                model.addAttribute(HOUSEHOLD_ID_ATTR, householdId);
                model.addAttribute(MAINTENANCE_ATTR, maintenanceChanged);
                model.addAttribute(MAINTENANCE_TASK_ATTR, maintenanceChanged.getMaintenanceTasks());
                return "redirect:/household/{householdId}/maintenance/{maintenanceId}";
            } catch (RuntimeException e) {
                log.error("error occurred during changing maintenance state {}", e.getMessage());
                setChangeStateModel(model, houseHold, householdId, maintenance, updatedMaintenanceStateDTO, REJECT_MESSAGE_MSG);
                result.rejectValue("taskState", "error", "Fields should be properly defined");
                return RETURN_STATE_EDIT_MAINTENANCE;
            }
        } catch (NonExistentEntityException e) {
            log.error(e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAINTENANCE_VIEW;
        }
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/stop")
    public String stopGeneratingMaintenanceTask(@PathVariable Long householdId, @PathVariable Long maintenanceId, Model model) {
        try {
            Household houseHold = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));

            maintenanceServiceFacade.stopGeneratingMaintenance(maintenance);
            setMaintenanceView(model, houseHold, householdId);
            return REDIRECT_TO_MAINTENANCE_VIEW;
        } catch (NonExistentEntityException e) {
            log.error(e.getMessage());
            e.getCause();
            return REDIRECT_TO_MAINTENANCE_VIEW;
        }
    }

    @PostMapping("/{householdId}/maintenance/{maintenanceId}/stop")
    public String closeMaintenance(@PathVariable Long householdId, @PathVariable Long maintenanceId, Model model) {
        try{
            log.debug("Closing maintenance with id - {} in household with id - {}" , maintenanceId , householdId);
            Household houseHold = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
            Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));

            if(!maintenance.isActive()){
                log.error(REDIRECT_TO_MAINTENANCE_VIEW);
                return REDIRECT_TO_MAINTENANCE_VIEW;
            }
            MaintenanceStateDTO maintenanceStateDTO = new MaintenanceStateDTO();
            maintenanceStateDTO.setTaskState(false);
            maintenanceStateDTO.setTaskResolution(maintenance.isResolution());
            maintenanceServiceFacade.changeMaintenanceState(maintenanceStateDTO, maintenanceId, maintenance);
        } catch (NonExistentEntityException e){
            log.error(e.getMessage());
            e.getCause();
        }
        return REDIRECT_TO_MAINTENANCE_VIEW;
    }

    @GetMapping("/{householdId}/maintenance/{maintenanceId}/return")
    public ModelAndView returnToMaintenance(@PathVariable Long householdId, @PathVariable Long maintenanceId, Model model) {
        Household houseHold = maintenanceServiceFacade.getHouseHoldById(householdId, getHouseholdNotExistIdMsg(householdId));
        Maintenance maintenance = maintenanceServiceFacade.getMaintenanceById(maintenanceId, getMaintenanceNotExitsIdMsg(maintenanceId));
        model.addAttribute(OWNER_PERMISSION, maintenanceServiceFacade.isHouseholdOwner(houseHold));
        model.addAttribute(MAINTENANCE_ATTR, maintenance);
        return new ModelAndView(REDIRECTION_HOUSEHOLD + householdId + "/" + MAINTENANCE_ATTR, (Map<String, ?>) model);
    }
}
