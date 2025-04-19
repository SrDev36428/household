package cz.cvut.fit.household.datamodel.entity.events;

import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MaintenanceTaskCloseEvent extends ApplicationEvent {

    private final Maintenance maintenance;

    public MaintenanceTaskCloseEvent(Object source, Maintenance parentMaintenance) {
        super(source);
        this.maintenance = parentMaintenance;
    }
}
