/*
 * (c) copyright 2012-2022 mgm technology partners GmbH.
 * This software, the underlying source code and other artifacts are protected by copyright.
 * All rights, in particular the right to use, reproduce, publish and edit are reserved.
 * A simple right of use (license) can be acquired for use, duplication, publication, editing etc..
 * Requests for this can be made at A12-license@mgm-tp.com or other official channels of the copyright holder.
 */
package cz.cvut.fit.household.datamodel.entity.user;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRegistrationDTO {

	@Valid
	private User user;

	private String passwordConfirmation;
}
