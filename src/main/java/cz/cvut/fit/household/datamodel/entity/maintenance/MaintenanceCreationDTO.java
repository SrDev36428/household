package cz.cvut.fit.household.datamodel.entity.maintenance;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.enums.RecurringType;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceCreationDTO {

    @NotBlank(message = "Title cannot be empty!")
    private String title;

    private String description;

    private Membership assignee;

    private Membership creator;

    private RecurringType recurringType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    private int interval;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime time;

    private int dayOfMonth;

    private String daysOfWeekPattern;

    private int month;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    private List<MaintenanceTaskCreationDTO> tasks;

    private List<LocalTime> dailyTimes = new ArrayList<>();   // Instead of single `time`
    private List<Integer> daysOfMonth = new ArrayList<>();    // Instead of single `dayOfMonth`

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> yearlyDates = new ArrayList<>();  // Or (month, day) pairs, etc.
}
