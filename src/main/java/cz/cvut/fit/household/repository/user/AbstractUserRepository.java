package cz.cvut.fit.household.repository.user;


import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.repository.AbstractRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Contains a filter method, which is searching for users, in invitation procedure
 */
public interface AbstractUserRepository extends AbstractRepository<String, User> {

    /**
     * Searching for users with given username
     *
     * @param searchTerm is username of needed users
     * @return a list of the users, whose username is matched with given
     */
    @Query(value = "select m from User m where m.username like %?1%")
    List<User> searchByUsername(String searchTerm);
}
