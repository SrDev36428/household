package cz.cvut.fit.household.datamodel.entity.events;

import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import org.springframework.context.ApplicationEvent;

public class MaintenanceAddedEvent extends ApplicationEvent {
    private final Maintenance maintenance;

    public MaintenanceAddedEvent(Object source, Maintenance maintenance) {
        super(source);
        this.maintenance = maintenance;
    }

    public Maintenance getMaintenance() {
        return maintenance;
    }
}
