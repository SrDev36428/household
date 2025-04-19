package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.events.OnForgotPasswordEvent;
import cz.cvut.fit.household.datamodel.entity.events.OnRegistrationCompleteEvent;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.entity.user.UserRegistrationDTO;
import cz.cvut.fit.household.datamodel.entity.user.VerificationToken;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.exception.VerificationTokenException;
import cz.cvut.fit.household.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {

    @Autowired
    private HttpServletRequest request;

    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private static final String ERROR = "error";
    private static final String NO_VERIFICATION_TOKEN = "No verification token";

    @GetMapping("/")
    public String defaultPage() {
        return "redirect:/welcome";
    }

    @GetMapping("/login")
    public String renderLoginPage() {
        return "login";
    }

    @GetMapping("/logout")
    public String renderLogoutPage(Authentication authentication) {
        log.info("User - {} logged out ", authentication.getName());
        new SecurityContextLogoutHandler().logout(request, null, null);
        return "redirect:/login?logout";
    }

    @GetMapping("/signup")
    public String renderSignUpPage(Model model) {
        model.addAttribute("user", new UserRegistrationDTO());
        return "signUp";
    }

    @GetMapping("/forgotPassword")
    public String renderForgotPasswordPage() {
        return "forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String forgotPassword(@RequestParam("email") String email, Model model) {
        log.debug("Forgot password request for email - {}" , email);
        try {
            eventPublisher.publishEvent(new OnForgotPasswordEvent(email));
            log.info("Sent email - {} to reset password" , email);
            return "resetPwdEmailSent";
        } catch (Exception exception) {
            model.addAttribute("wrongEmail", "true");
            log.error("Wrong email entered");
            return "forgotPassword";
        }
    }

    @GetMapping("/changePassword")
    public String renderChangePasswordPage() {
        return "changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, @RequestParam("newPasswordConfirmation") String newPasswordConfirmation, Model model, Authentication authentication) {
        log.debug("Changing password for user - {}" , authentication.getName());
        User user = userService.findUserByUsername(authentication.getName()).orElseThrow(
                () -> {
                    log.error("Could not find user with name {}", authentication.getName());
                    return new RuntimeException("Authenticated user no longer exists in the database");
                }
        );
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            model.addAttribute("wrongOldPassword", "true");
            log.info("Wrong old password entered by user - {}" , user.getUsername());
            return "changePassword";
        }
        if (!newPassword.equals(newPasswordConfirmation)) {
            model.addAttribute("nonMatchingPasswords", "true");
            log.info("New password confirmation does not match by user - {}" , user.getUsername());
            return "changePassword";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.createOrUpdateUser(user);
        log.info("Password changed successfully for user - {}" , user.getUsername());
        return "redirect:/login?changePassword";
    }

    @PostMapping("/signup")
    public String signUp(@Valid @ModelAttribute("user") UserRegistrationDTO userRegistrationDTO, BindingResult result, HttpServletRequest httpServletRequest, Model model) {
        User user = userRegistrationDTO.getUser();
        log.debug("Signing up a new user - {}" , user);

        if (userService.findUserByUsername(user.getUsername()).isPresent()) {
            user.setUsername("");
            log.error("Username - {} already exists" , user.getUsername());
            result.rejectValue("user.username", ERROR, "Username already exists");
        }

        if (userService.findByEmail(user.getEmail()).isPresent()) {
            user.setEmail("");
            log.error("Email - {} already exists"  , user.getEmail());
            result.rejectValue("user.email", ERROR, "Email already exists");
        }

        if (!user.getPassword().equals(userRegistrationDTO.getPasswordConfirmation())) {
            user.setPassword("");
            log.error("Password confirmation does not match");
            result.rejectValue("user.password", ERROR, "Passwords should match");
        }

        if (!result.getAllErrors().isEmpty()) {
            return "signUp";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.createOrUpdateUser(user);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(httpServletRequest.getLocale(), user));
        return "sign-up-successful";
    }

    @GetMapping("/registrationConfirm")
    public String confirmRegistration(WebRequest webRequest, Model model, @RequestParam("token") String token) {

        VerificationToken verificationToken = userService.getVerificationToken(token);
        log.debug("Confirming verification token");
        if (verificationToken == null) {
            log.error("Invalid verification token");
            throw new VerificationTokenException(NO_VERIFICATION_TOKEN);
        }

        User user = verificationToken.getUser();
        if (verificationToken.getExpirationDate().isBefore(LocalDate.now())) {
            log.error("Expired verification token");
            throw new VerificationTokenException(NO_VERIFICATION_TOKEN);
        }

        user.setEnabled(true);
        userService.createOrUpdateUser(user);
        log.info("User - {} registered successfully" , user.getUsername());
        return "registrationConfirm";
    }

    @GetMapping("/passwordRecovery")
    public String renderRecoverPassword(@ModelAttribute String password, Model model, @RequestParam("token") String token) {
        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            log.error("Invalid verification token");
            throw new VerificationTokenException(NO_VERIFICATION_TOKEN);
        }

        User user = verificationToken.getUser();
        log.debug("Recovering password - asking user - {} for new password" , user.getUsername());

        if (verificationToken.getExpirationDate().isBefore(LocalDate.now())) {
            log.error("Expired verification token");
            throw new VerificationTokenException("Verification token expired");
        }

        model.addAttribute("user", user);
        model.addAttribute("token", token);
        return "recoverPassword";
    }

    @PostMapping("/passwordRecovery/{token}")
    public String recoverPassword(@PathVariable String token, @RequestParam("password") String password,
                                  @RequestParam("passwordConfirmation") String passwordConfirmation, Model model) {

        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            log.error("Invalid verification token");
            throw new VerificationTokenException(NO_VERIFICATION_TOKEN);
        }
        if (!password.equals(passwordConfirmation)) {
            log.error("Password confirmation does not match");
            model.addAttribute("nonMatchingPasswords", true);
            return "recoverPassword";
        }

        log.debug("Setting a new password for recovery for user - {} ", verificationToken.getUser().getUsername());

        User user = verificationToken.getUser();
        if (verificationToken.getExpirationDate().isBefore(LocalDate.now())) {
            log.error("Expired verification token");
            throw new VerificationTokenException("Verification token expired");
        }


        user.setPassword(passwordEncoder.encode(password));
        userService.createOrUpdateUser(user);
        log.info("New Password has been set for user - {} . Redirected to login" , user.getUsername());
        return "login";
    }

    @GetMapping("/welcome")
    public String renderWelcomePage(Authentication authentication, Model model) {
        User user = userService.findUserByUsername(authentication.getName())
                .orElseThrow(() -> {
                    log.error("Could not find user with name {}", authentication.getName());
                    return new RuntimeException("Authenticated user no longer exists in the database");});
        log.info("User - {} logged in successfully" , authentication.getName());
        List<Membership> pendingMemberships =  user.getMemberships()
                .stream().filter(membership -> membership.getStatus().equals(MembershipStatus.PENDING))
                .collect(Collectors.toList());

        List<Membership> activeMemberships =  user.getMemberships()
                .stream().filter(membership -> membership.getStatus().equals(MembershipStatus.ACTIVE))
                .collect(Collectors.toList());

        model.addAttribute("pendingHouseholds", pendingMemberships);
        model.addAttribute("activeHouseholds", activeMemberships);
        return "welcome";
    }
}
