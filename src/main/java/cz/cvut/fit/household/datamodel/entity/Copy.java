package cz.cvut.fit.household.datamodel.entity;

import cz.cvut.fit.household.datamodel.entity.item.Item;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Copies of the specific item. Location of the copy records, so user can find out
 * everything easily.
 *
 * @see Item
 * @see Location
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Copy {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Location location;

    private String description;

    private LocalDate expirationDate;
}

