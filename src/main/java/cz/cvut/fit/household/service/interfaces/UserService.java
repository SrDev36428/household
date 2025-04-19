/**
 * The UserService interface provides methods to manage User entities in the application.
 * It includes operations for creating or updating users, retrieving users, finding users by search term,
 * finding users by username or email, checking if a user exists, deleting users, and managing verification tokens.
 *
 * @see cz.cvut.fit.household.datamodel.entity.user.User
 * @see cz.cvut.fit.household.datamodel.entity.user.VerificationToken
 */
package cz.cvut.fit.household.service.interfaces;

import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.entity.user.VerificationToken;

import java.util.List;
import java.util.Optional;

public interface UserService {

    /**
     * Creates or updates a user.
     *
     * @param user The user to be added or updated in the database.
     * @return The freshly saved user.
     */
    User createOrUpdateUser(User user);

    /**
     * Retrieves all existing users.
     *
     * @return A list of all existing users in the database.
     */
    List<User> findAllUsers();

    /**
     * Retrieves a list of users whose username matches the search term.
     *
     * @param searchTerm The username that is probably similar to some users' usernames.
     * @return A list of users whose usernames match the given search term.
     */
    List<User> findUsersBySearchTerm(String searchTerm);

    /**
     * Retrieves a user with the given username.
     *
     * @param username The username of the required user.
     * @return An Optional containing the user with the given username, or an empty Optional if not found.
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Retrieves a user with the given email.
     *
     * @param email The email of the required user.
     * @return An Optional containing the user with the given email, or an empty Optional if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given username exists.
     *
     * @param username The username to check for existence.
     * @return True if the user is found, otherwise false.
     */
    Boolean exists(String username);

    /**
     * Deletes a user with the given username.
     *
     * @param username The username of the user to be deleted.
     */
    void deleteUserByUsername(String username);

    /**
     * Creates a verification token for the specified user with the given token.
     *
     * @param user  The user for whom to create the verification token.
     * @param token The token to be associated with the verification process.
     */
    void createVerificationToken(User user, String token);

    /**
     * Retrieves a verification token based on the provided token string.
     *
     * @param verificationToken The token string used for verification.
     * @return The verification token associated with the provided token string.
     */
    VerificationToken getVerificationToken(String verificationToken);
}
