package cz.cvut.fit.household.datamodel.entity.location;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LocationCreationDTO {

    @NotBlank(message = "Title is empty")
    private String title;

    private String description;
}
