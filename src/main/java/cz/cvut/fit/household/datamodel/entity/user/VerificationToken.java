package cz.cvut.fit.household.datamodel.entity.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private LocalDate expirationDate;

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expirationDate = LocalDate.now().plusDays(1);
    }
}
