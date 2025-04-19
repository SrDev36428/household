package cz.cvut.fit.household.datamodel.entity.maintenance;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Entity
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Maintenance maintenance;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date deadline;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date closingDate;

    //false = not closed ( not resolved) whatever
    @NotNull
    private boolean taskState;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaintenanceTask that = (MaintenanceTask) o;
        return taskState == that.taskState && Objects.equals(id, that.id) && Objects.equals(maintenance, that.maintenance) && Objects.equals(deadline, that.deadline) && Objects.equals(closingDate, that.closingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, maintenance, deadline, closingDate, taskState);
    }
}
