package cz.cvut.fit.household.datamodel.entity.item;

import cz.cvut.fit.household.datamodel.entity.category.Category;
import cz.cvut.fit.household.datamodel.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemCreationDTO {

    @NotBlank(message = "Title should not be empty")
    private String title;

    private String description;

    private Category category;

    private QuantityType quantityType;

    @NotBlank(message = "Maximum quantity should not be empty")
    private String maxQuantity;

    @NotBlank(message = "Current quantity should not be empty")
    private String currentQuantity;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date expiration;

}
