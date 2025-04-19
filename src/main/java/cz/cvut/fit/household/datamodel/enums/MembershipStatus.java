package cz.cvut.fit.household.datamodel.enums;

/**
 * Statuses of the members invitation procedure.
 */
public enum MembershipStatus {
    ACTIVE("ACTIVE"),
    PENDING("PENDING"),
    DISABLED("DISABLED");

    private String status;

    MembershipStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
