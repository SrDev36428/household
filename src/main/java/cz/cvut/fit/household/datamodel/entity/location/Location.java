package cz.cvut.fit.household.datamodel.entity.location;

import cz.cvut.fit.household.datamodel.entity.Copy;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Location class represent location of the household where items can be possibly located. Sublocations are possible.
 *
 * @see Household
 * @see Copy
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Household houseHold;

    @OneToMany(mappedBy = "location", cascade = {CascadeType.REMOVE, CascadeType.REMOVE})
    private List<Item> items = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "main_id")
    private Location mainLocation;

    @OneToMany(mappedBy = "mainLocation", cascade = CascadeType.REMOVE)
    private List<Location> subLocations = new ArrayList<>();

    @NotBlank(message = "Title is empty")
    private String title;

    private String description;

    public String getLocationPath(){
        if(mainLocation != null){
            return mainLocation.getLocationPath() + "/" + title;
        }
        else{
            return title;
        }
    }
}
