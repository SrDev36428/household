/*
 * (c) copyright 2012-2022 mgm technology partners GmbH.
 * This software, the underlying source code and other artifacts are protected by copyright.
 * All rights, in particular the right to use, reproduce, publish and edit are reserved.
 * A simple right of use (license) can be acquired for use, duplication, publication, editing etc..
 * Requests for this can be made at A12-license@mgm-tp.com or other official channels of the copyright holder.
 */
package cz.cvut.fit.household.service;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.HouseHoldService;
import cz.cvut.fit.household.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

	private final HouseHoldService houseHoldService;
	private final UserService userService;

	public boolean isOwner(Household household) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		for (Membership membership : household.getMemberships()) {
			if (membership.getUser().getUsername().equals(username) &&
				membership.getStatus().equals(MembershipStatus.ACTIVE) &&
				membership.getMembershipRole().equals(MembershipRole.OWNER)) {
				return true;
			}
		}

		return false;
	}

	public boolean canKick(Long householdId, String usernameOfUserToKick) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Household household = houseHoldService.findHouseHoldById(householdId)
			.orElseThrow(() -> new NonExistentEntityException("Household with id: " + householdId + " doesn't exist"));

		Membership membershipOfAuthenticatedUser = household.getMemberships().stream()
			.filter(m -> m.getUser().getUsername().equals(username) && m.getStatus().equals(MembershipStatus.ACTIVE))
			.findFirst()
			.orElse(null);

		Membership membershipOfUserToKick = household.getMemberships().stream()
			.filter(m -> m.getUser().getUsername().equals(usernameOfUserToKick) && m.getStatus().equals(MembershipStatus.ACTIVE))
			.findFirst()
			.orElse(null);

		if (membershipOfAuthenticatedUser == null || membershipOfUserToKick == null) {
			return false;
		}

		return MembershipRole.canKick(membershipOfAuthenticatedUser.getMembershipRole(), membershipOfUserToKick.getMembershipRole());
	}
}
