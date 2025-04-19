package cz.cvut.fit.household.datamodel.entity.household;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.entity.maintenance.Maintenance;
import lombok.*;

/**
 * Household is the main class of the application. Its purpose is to save general information about household and links
 * to such entities as Maintenance, Location, Memberships. All of mentioned entities can not exist without household.
 *
 * @see Membership
 * @see Maintenance
 * @see Location
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Household {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Title is empty")
    private String title;

    private String description;

    @OneToMany(mappedBy = "household", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<Membership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "houseHold", cascade = CascadeType.PERSIST)
    @ToString.Exclude
    private List<Location> locations;

    @OneToMany(mappedBy = "houseHolD", cascade = CascadeType.PERSIST)
    @ToString.Exclude
    private List<Category> category;

    @OneToMany(mappedBy = "houseHoLD", cascade = CascadeType.PERSIST)
    @ToString.Exclude
    private List<Maintenance> maintenances;

    public void addMembership(Membership membership) {
        memberships.add(membership);
        membership.setHousehold(this);
    }
}
