package cz.cvut.fit.household.datamodel.entity;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

/**
 *
 * Membership is a connection bridge between household and users, it stores basic information about current status of user in the household.
 * New instance of membership is created whenever new user joins Household {@link Household}. Every user can have multiple
 * memberships (one for every household where he is a member).
 * Membership has three possible states: ACTIVE, PENDING and DISABLED.
 * ACTIVE - you are current member of the household.
 * PENDING - you are invited to the household, but you did not accept or decline invitation yet.
 * DISABLED - you either declined invitation or left household.
 *
 * @see Household
 * @see Maintenance
 * @see MaintenanceTask
 * @see MembershipStatus
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private final LocalDate creationDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private MembershipStatus status;

    @Enumerated(EnumType.STRING)
    private MembershipRole membershipRole;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private User user;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Household household;
}

