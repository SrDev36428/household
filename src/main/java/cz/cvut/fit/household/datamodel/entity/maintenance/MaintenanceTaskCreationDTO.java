package cz.cvut.fit.household.datamodel.entity.maintenance;

import cz.cvut.fit.household.datamodel.entity.Membership;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceTaskCreationDTO {

    private Membership assignee;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date deadline;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date closingDate;

    private boolean taskState;

    private Long taskId;
}
