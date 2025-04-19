package cz.cvut.fit.household.service.event;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import cz.cvut.fit.household.datamodel.entity.events.OnForgotPasswordEvent;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PasswordRecoveryListener implements
	ApplicationListener<OnForgotPasswordEvent> {

	private final UserService userService;
	private final JavaMailSender mailSender;

	@Value("${app.url}")
	private String appUrl;

	@Override
	public void onApplicationEvent(OnForgotPasswordEvent event) {
		this.sendPasswordRecovery(event);
	}

	private void sendPasswordRecovery(OnForgotPasswordEvent event) {
		User user = userService.findByEmail(event.getEmail())
			.orElseThrow(RuntimeException::new);
		String token = UUID.randomUUID().toString();

		userService.createVerificationToken(user, token);
		String recipientAddress = user.getEmail();
		String subject = "Password recovery";
		String confirmationUrl = "passwordRecovery?token=" + token;
		String message = "Use this link for password recovery";

		SimpleMailMessage email1 = new SimpleMailMessage();
		email1.setTo(recipientAddress);
		email1.setSubject(subject);
		email1.setText(message + "\r\n" + appUrl + confirmationUrl);
		mailSender.send(email1);
	}
}
