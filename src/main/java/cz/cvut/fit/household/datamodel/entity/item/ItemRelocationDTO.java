package cz.cvut.fit.household.datamodel.entity.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRelocationDTO {
    private Long itemId;

    @NotNull
    private Long locationId;

    public ItemRelocationDTO(Long itemId) {
        this.itemId = itemId;
    }
}
