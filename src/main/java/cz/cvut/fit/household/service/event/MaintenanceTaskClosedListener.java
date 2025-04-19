package cz.cvut.fit.household.service.event;

import cz.cvut.fit.household.datamodel.entity.events.MaintenanceTaskCloseEvent;
import cz.cvut.fit.household.service.interfaces.MaintenanceServiceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MaintenanceTaskClosedListener implements ApplicationListener<MaintenanceTaskCloseEvent> {

    private final MaintenanceServiceFacade maintenanceServiceFacade;

    @Override
    public void onApplicationEvent(MaintenanceTaskCloseEvent event) {
        log.debug("Maintenance task closed");
        maintenanceServiceFacade.generateMaintenanceTasks(event.getMaintenance(), true);
    }
}
