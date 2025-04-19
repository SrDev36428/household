/*
 * (c) copyright 2012-2022 mgm technology partners GmbH.
 * This software, the underlying source code and other artifacts are protected by copyright.
 * All rights, in particular the right to use, reproduce, publish and edit are reserved.
 * A simple right of use (license) can be acquired for use, duplication, publication, editing etc..
 * Requests for this can be made at A12-license@mgm-tp.com or other official channels of the copyright holder.
 */
package cz.cvut.fit.household.service.event;

import cz.cvut.fit.household.daos.interfaces.LogDAO;
import cz.cvut.fit.household.datamodel.entity.Log;
import cz.cvut.fit.household.datamodel.entity.events.OnInventoryChangeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class InventoryChangeListener implements
        ApplicationListener<OnInventoryChangeEvent> {

    private final LogDAO logDAO;

    @Override
    public void onApplicationEvent(OnInventoryChangeEvent event) {
        this.logInventoryChange(event);
    }

    private void logInventoryChange(OnInventoryChangeEvent event) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Log log = new Log();
        log.setHouseholdId(event.getHouseholdId());
        log.setMessage(event.getDescription() + ". Created by: " + username + ", Date: " + LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()+".");
        logDAO.saveLog(log);
    }
}
