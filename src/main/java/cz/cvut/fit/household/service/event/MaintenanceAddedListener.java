package cz.cvut.fit.household.service.event;

import cz.cvut.fit.household.daos.interfaces.LogDAO;
import cz.cvut.fit.household.datamodel.entity.Log;
import cz.cvut.fit.household.datamodel.entity.events.MaintenanceAddedEvent;
import cz.cvut.fit.household.service.interfaces.MaintenanceServiceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class MaintenanceAddedListener implements ApplicationListener<MaintenanceAddedEvent> {

    private final LogDAO logDAO;
    private final MaintenanceServiceFacade maintenanceServiceFacade;

    @Override
    public void onApplicationEvent(MaintenanceAddedEvent event) {
        this.logMaintenanceAddition(event);
        this.maintenanceServiceFacade.generateMaintenanceTasks(event.getMaintenance(), false);
    }

    private void logMaintenanceAddition(MaintenanceAddedEvent event) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Log log = new Log();
        log.setHouseholdId(event.getMaintenance().getHouseHoLD().getId());
        log.setMessage("Created by: " + username + ", Date: " +
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant() + ".");
        logDAO.saveLog(log);
    }
}
