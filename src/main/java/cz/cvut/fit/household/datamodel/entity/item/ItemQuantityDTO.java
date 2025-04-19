package cz.cvut.fit.household.datamodel.entity.item;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class ItemQuantityDTO {

    @NotBlank(message = "New quantity is not specified")
    private String currentQuantity;

}
