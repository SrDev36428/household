package cz.cvut.fit.household.datamodel.entity.item;

import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.entity.location.Location;
import cz.cvut.fit.household.datamodel.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class ItemFilterDTO {

    Category category;
    Location location;
    String title;
    QuantityType quantityType;
    Double maxQuantityMin;
    Double maxQuantityMax;
    Double currentQuantityMin;
    Double currentQuantityMax;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date expirationMin;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date expirationMax;
}
