/**
 * The UserDAO interface is a Data Access Object that is used to manage User entities in the data storage.
 * It provides methods for saving, retrieving, searching, and deleting user profiles.
 *
 * @see cz.cvut.fit.household.datamodel.entity.user.User
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {

    /**
     * Saves the given User entity in the data storage.
     *
     * @param user The User object to be saved.
     * @return The saved User object.
     */
    User saveUser(User user);

    /**
     * Retrieves a list of all User entities in the data storage.
     *
     * @return A list of User objects representing all user profiles.
     */
    List<User> findAllUsers();

    /**
     * Searches for User entities based on the provided search term that is a string.
     *
     * @param searchTerm The search term to match against user profiles.
     * @return A list of User objects that match the search term.
     */
    List<User> findUsersBySearchTerm(String searchTerm);

    /**
     * Finds a User entity by its unique username.
     *
     * @param username The unique username of the User to be retrieved.
     * @return An Optional containing the found User, or an empty Optional if not found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a User entity by its unique email address.
     *
     * @param email The unique email address of the User to be retrieved.
     * @return An Optional containing the found User, or an empty Optional if not found.
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Checks if a user with the specified username exists in the data storage.
     *
     * @param username The username to check for existence.
     * @return true if the user exists, false otherwise.
     */
    Boolean userExists(String username);

    /**
     * Deletes a User entity from the data storage by its username.
     *
     * @param username The username of the User to be deleted.
     */
    void deleteUserByUsername(String username);
}
