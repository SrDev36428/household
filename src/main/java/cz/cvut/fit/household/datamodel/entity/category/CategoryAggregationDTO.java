package cz.cvut.fit.household.datamodel.entity.category;

import com.google.common.util.concurrent.AtomicDouble;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.item.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryAggregationDTO {

    private Long id;

    private Household household;

    private List<Item> items;

    private Category mainCategory;

    private List<Category> subCategories;

    private String title;

    private String description;

    private AtomicDouble sumKilo;

    private AtomicDouble sumLiter;

    private AtomicDouble sumPiece;

}
