package cz.cvut.fit.household.datamodel.enums;

public enum MembershipRole {
	OWNER("Owner"),
	REGULAR("Regular");

	private String role;

	MembershipRole(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}

	public static boolean canKick(MembershipRole r1, MembershipRole r2) {
		return r1.equals(MembershipRole.OWNER) && !r2.equals(MembershipRole.OWNER);
	}
}
