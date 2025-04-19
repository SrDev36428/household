package cz.cvut.fit.household.datamodel.entity.household;

import lombok.*;

import javax.validation.constraints.NotBlank;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HouseholdCreationDTO {

    @NotBlank(message = "Title is empty")
    private String title;

    private String description;
}
