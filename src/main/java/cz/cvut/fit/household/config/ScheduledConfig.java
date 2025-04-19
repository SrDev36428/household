/*
 * (c) copyright 2012-2022 mgm technology partners GmbH.
 * This software, the underlying source code and other artifacts are protected by copyright.
 * All rights, in particular the right to use, reproduce, publish and edit are reserved.
 * A simple right of use (license) can be acquired for use, duplication, publication, editing etc..
 * Requests for this can be made at A12-license@mgm-tp.com or other official channels of the copyright holder.
 */
package cz.cvut.fit.household.config;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.service.interfaces.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Configuration
@EnableScheduling
@EnableTransactionManagement
@RequiredArgsConstructor
public class ScheduledConfig {

    private final ItemService itemService;
    private final JavaMailSender mailSender;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void scheduleExpirationJob() {
        List<Item> itemList = itemService.findItems();
        for (Item item : itemList) {

            if (item.getExpiration() != null && item.getExpiration().before(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))) {
                List<Membership> memberships = item.getLocation().getHouseHold().getMemberships();

                for (Membership membership : memberships) {
                    sendMail(membership.getUser().getEmail(), "Item has expired", "Item with tile : " + item.getTitle() + " has expired");
                }
            }

            else if (item.getExpiration() != null && item.getExpiration().after(Date.from(LocalDateTime.now().minusDays(7).atZone(ZoneId.systemDefault()).toInstant()))) {
                List<Membership> memberships = item.getLocation().getHouseHold().getMemberships();

                for (Membership membership : memberships) {
                    sendMail(membership.getUser().getEmail(), "Item will soon expire", "Item with title : " + item.getTitle() + " will soon expire");
                }
            }
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
