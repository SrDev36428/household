package cz.cvut.fit.household.datamodel.entity.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreationDTO {

    @NotBlank(message = "Title is empty")
    private String title;

    private String description;
}
