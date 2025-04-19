package cz.cvut.fit.household.datamodel.entity.maintenance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceStateDTO {
    @NotNull
    private boolean taskState;

    @NotNull
    private boolean taskResolution;

    public boolean getTaskState() {
        return taskState;
    }
    public  boolean getTaskResolution(){return taskResolution;}
}
