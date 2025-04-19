package cz.cvut.fit.household.datamodel.entity.user;

import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * User class is responsible for storing general information about users of the application. User entity is created
 * when new user is registered in the application. For more info read about authorities and membership
 * class {@link Membership}
 *
 * @see Household
 * @see Item
 * @see Membership
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
public class User {

    @Id
    @NotBlank(message = "Username is empty")
    private String username;

    @NotBlank(message = "Password is empty")
    @ToString.Exclude
    private String password;

    @NotBlank(message = "First name is empty")
    private String firstName;

    @NotBlank(message = "Last name is empty")
    private String lastName;

    @Email(message = "Invalid format of email")
    @NotBlank(message = "Email is empty")
    private String email;

    @ToString.Exclude
    private boolean enabled;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<Membership> memberships = new ArrayList<>();

    public User(String username, String password, String firstName, String lastName, String email, List<Membership> memberships) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enabled = false;
        this.memberships = memberships;
    }

    public User() {
        this.enabled = false;
    }

    public void addMembership(Membership membership) {
        memberships.add(membership);
        membership.setUser(this);
    }
}
