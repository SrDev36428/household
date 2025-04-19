package cz.cvut.fit.household.datamodel.enums;

public enum RecurringType {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly");

    private final String type;

    RecurringType(String type) {
        this.type = type;
    }

    public String getRecurringType() {
        return type;
    }

}
