package cz.cvut.fit.household.datamodel.entity.maintenance;

import cz.cvut.fit.household.datamodel.enums.RecurringType;
import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.Objects;


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RecurringPattern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private  int dayOfMonth;
    private int dayOfWeek;
    private int interval;
    private int monthOfYear;
    private LocalTime timeOfDay;

    @ManyToOne
    @JoinColumn(name = "maintenance_id")
    private Maintenance maintenance;

    private RecurringType recurringType;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecurringPattern that = (RecurringPattern) o;
        return id == that.id && dayOfMonth == that.dayOfMonth && dayOfWeek == that.dayOfWeek && interval == that.interval && monthOfYear == that.monthOfYear && Objects.equals(timeOfDay, that.timeOfDay) && Objects.equals(maintenance, that.maintenance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dayOfMonth, dayOfWeek, interval, monthOfYear, timeOfDay, maintenance);
    }
}
