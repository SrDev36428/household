package cz.cvut.fit.household.datamodel.entity.maintenance;

import cz.cvut.fit.household.datamodel.enums.RecurringType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringPatternDTO {

    private  int dayOfMonth;
    private int dayOfWeek;
    private int interval;
    private int monthOfYear;
    private LocalTime timeOfDay;
    private RecurringType recurringType;
}
