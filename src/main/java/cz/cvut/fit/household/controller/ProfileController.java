package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.entity.user.VerificationToken;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.exception.VerificationTokenException;
import cz.cvut.fit.household.repository.user.jpa.UserRepository;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.MaintenanceService;
import cz.cvut.fit.household.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;
    private final HouseHoldService householdService;
    private static final String HOUSEHOLD_ATTR = "household";

    @GetMapping("/profile")
    public String renderProfilePage(@RequestParam(name = "sidebarType", defaultValue = "default") String sidebarType,Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Finding user - {}" , username);
        User user = userService.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.error("User with username : {} not found", username);
                    return new NonExistentEntityException("User with username:" + username + " doesn't exist");
                });

        model.addAttribute("username", user.getUsername());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("sidebarType", sidebarType);
        return "profile/profile";
    }

    @GetMapping("/household/{householdId}/profile")
    public String renderHouseholdProfilePage(@RequestParam(name = "sidebarType", defaultValue = "household") String sidebarType, @PathVariable Long householdId, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.debug("Finding user - {}" , username);
        User user = userService.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.error("User with username : {} not found", username);
                    return new NonExistentEntityException("User with username: " + username + " doesn't exist");
                });

        Household household = householdService.findHouseHoldById(householdId)
                .orElseThrow(() -> {
                    log.error("Household with id {} not found", householdId);
                    return new NonExistentEntityException("Household with id: " + householdId + " doesn't exist");
                });

        model.addAttribute(HOUSEHOLD_ATTR, household);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("sidebarType", sidebarType);

        return "profile/profile";
    }


}