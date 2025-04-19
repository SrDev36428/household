package cz.cvut.fit.household.datamodel.entity.item;

import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.Copy;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Item which has copies and category. Can be added/removed via household. User can
 * save items, or search them.
 *
 * @see Copy
 * @see Category
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Category category;

    @ManyToOne
    private Location location;

    @NotBlank
    private String title;

    private String description;

    private QuantityType quantityType;

    @NotNull
    private Double maxQuantity;

    @NotNull
    private Double currentQuantity;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expiration;

    public Item(ItemCreationDTO itemDto) {
        this.title = itemDto.getTitle();
        this.description = itemDto.getDescription();
        this.quantityType = itemDto.getQuantityType();
        this.maxQuantity = Double.valueOf(itemDto.getMaxQuantity());
        this.currentQuantity = Double.valueOf(itemDto.getCurrentQuantity());
        this.expiration = itemDto.getExpiration();
    }
}
