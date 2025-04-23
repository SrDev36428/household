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
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserService userService;  

    private static final String DOES_NOT_EXIST = " doesn't exist";
    private static final String HOUSEHOLD_WITH_ID = "Household With id: ";
    private static final String HOUSEHOLD_ATTR = "household";

    @GetMapping("/{householdId}/logs")
    public String renderLogsPage(@PathVariable Long householdId,
            Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Finding user - {}", username);
        User user = userService.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.error("User with username : {} not found", username);
                    return new NonExistentEntityException("User with username: " + username + " doesn't exist");
                });
        Household houseHold = houseHoldService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error(HOUSEHOLD_WITH_ID + "{}" + DOES_NOT_EXIST, householdId);
                    return new NonExistentEntityException(HOUSEHOLD_WITH_ID + householdId + DOES_NOT_EXIST);
                });

        model.addAttribute(HOUSEHOLD_ATTR, houseHold);
        model.addAttribute("householdId", householdId);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("logs", logDAO.findLogByHouseholdIdOrderByIdDesc(householdId));
        return "logs";
    }

    @GetMapping("/{householdId}/logs/{logId}/delete")
    @Transactional
    public RedirectView deleteLogById(@PathVariable Long householdId, 
                                    @PathVariable Long logId) {
        logDAO.deleteLogById(logId);
        log.debug("Deleted log with id - {} for household with id - {}", logId, householdId);
        return new RedirectView("/household/" + householdId + "/logs");
    }
}
