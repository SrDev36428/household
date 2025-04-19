package cz.cvut.fit.household.config;

import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.service.interfaces.MaintenanceService;
import cz.cvut.fit.household.service.interfaces.MaintenanceTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Configuration
@EnableScheduling
@EnableTransactionManagement
@RequiredArgsConstructor
public class MaintenanceConfig {

    private final MaintenanceService maintenanceService;
    private final MaintenanceTaskService maintenanceTaskService;
    private final JavaMailSender mailSender;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void maintenanceExpirationJobs() {
        List<Maintenance> maintenanceList = maintenanceService.getAll();

        for (Maintenance maintenance : maintenanceList) {
            for (MaintenanceTask task : maintenance.getMaintenanceTasks()) {
                if (!task.isTaskState()
                        && task.getDeadline().before(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))) {
                    maintenanceEmailProcessing(maintenance.getAssignee(), maintenance.getTitle(), "Maintenance Task Has Ended", "has ended");
                } else if (!task.isTaskState()
                        && task.getDeadline().before(Date.from(LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()))) {
                    maintenanceEmailProcessing(maintenance.getAssignee(), maintenance.getTitle(), "Maintenance Will Soon End", "will end soon.");
                }
            }
        }
    }

    //todo make the generator work in service or remove it from here
    private void maintenanceTaskGenerator(Maintenance maintenance) {

    }


    public void maintenanceEmailProcessing(Membership assignee, String mainTitle, String title, String ending) {
        if (assignee != null
                && title != null
                && ending != null) {
            sendMail(assignee.getUser().getEmail(), title, "Maintenance with title : " + "'" + mainTitle + "' " + ending);

        }
    }

    private void sendMail(String recipientAddress, String subject, String message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
    }
}
