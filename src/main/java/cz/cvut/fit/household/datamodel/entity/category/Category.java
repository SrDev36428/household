package cz.cvut.fit.household.datamodel.entity.category;

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
 * Categories of the items, which provided to improve managing, and sorting
 * items.
 *
 * @see Item
 * @see Household
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Household houseHolD;


    @OneToMany(mappedBy = "category", cascade = {CascadeType.REMOVE, CascadeType.REMOVE})
    private List<Item> items = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "main_id")
    private Category mainCategory;

    @OneToMany(mappedBy = "mainCategory", cascade = CascadeType.REMOVE)
    private List<Category> subCategory = new ArrayList<>();

    @NotBlank(message = "Title is empty")
    private String title;

    private String description;

    public String getCategoryPath(){
        if(mainCategory != null){
            return mainCategory.getCategoryPath() + "->" + title;
        }
        else{
            return title;
        }
    }
}
