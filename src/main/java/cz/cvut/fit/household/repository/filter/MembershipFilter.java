package cz.cvut.fit.household.repository.filter;

import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import lombok.Builder;
import lombok.Getter;


/**
 * Contains custom searching parameters, for filtering functions
 */
@Builder
@Getter
public class MembershipFilter {
    private Long id;
    private MembershipStatus status;
    private String username;
    private Long householdId;
}
