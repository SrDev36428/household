/*
 * (c) copyright 2012-2022 mgm technology partners GmbH.
 * This software, the underlying source code and other artifacts are protected by copyright.
 * All rights, in particular the right to use, reproduce, publish and edit are reserved.
 * A simple right of use (license) can be acquired for use, duplication, publication, editing etc..
 * Requests for this can be made at A12-license@mgm-tp.com or other official channels of the copyright holder.
 */
package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.daos.interfaces.LogDAO;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.transaction.Transactional;

@Controller
@RequestMapping("/household")
@RequiredArgsConstructor
@Slf4j
public class LogsController {

    private final LogDAO logDAO;
    private final HouseHoldService houseHoldService;

    private static final String DOES_NOT_EXIST = " doesn't exist";
    private static final String HOUSEHOLD_WITH_ID = "Household With id: ";
    private static final String HOUSEHOLD_ATTR = "household";

    @GetMapping("/{householdId}/logs")
    public String renderLogsPage(@PathVariable Long householdId,
                                 Model model) {
        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error(HOUSEHOLD_WITH_ID + "{}" + DOES_NOT_EXIST, householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        model.addAttribute(HOUSEHOLD_ATTR, houseHold);
        model.addAttribute("householdId", householdId);
        model.addAttribute("logs", logDAO.findLogByHouseholdIdOrderByIdDesc(householdId));
        return "logs";
    }

    @GetMapping("/{householdId}/logs/delete")
    @Transactional
    public RedirectView deleteAllLogs(@PathVariable Long householdId) {
        logDAO.deleteAllLogsByHouseholdId(householdId);
        log.debug("Deleted all logs for household with id - {}", householdId);
        return new RedirectView("/household/" + householdId + "/logs");
    }
}
