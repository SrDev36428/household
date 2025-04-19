package cz.cvut.fit.household.repository;

import cz.cvut.fit.household.datamodel.entity.user.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);
}
