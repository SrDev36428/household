package cz.cvut.fit.household.service.event;

import cz.cvut.fit.household.datamodel.entity.events.OnRegistrationCompleteEvent;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RegistrationListener implements
        ApplicationListener<OnRegistrationCompleteEvent> {

    private final UserService userService;
    private final JavaMailSender mailSender;

    @Value("${app.url}")
    private String appUrl;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();

        userService.createVerificationToken(user, token);
        String recipientAddress = user.getEmail();
        String subject = "Registration confirmation";
        String confirmationUrl = "registrationConfirm?token=" + token;
        String message = "Confirm your registration by using this link";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + "\r\n" + appUrl + confirmationUrl);
        mailSender.send(email);
    }
}
