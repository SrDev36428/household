package cz.cvut.fit.household.repository.user.jpa;

import java.util.Optional;

import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.repository.user.AbstractUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository class for management of user entities in the database.
 */
@Repository
public interface UserRepository extends AbstractUserRepository, JpaRepository<User, String>  {

	Optional<User> findUserByEmail(String email);
}
