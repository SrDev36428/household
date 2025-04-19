/**
 * The TokenDAO interface is responsible for managing VerificationToken entities in the data storage.
 * It provides methods for saving and retrieving verification tokens.
 *
 * @see cz.cvut.fit.household.datamodel.entity.user.VerificationToken
 */
package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.user.VerificationToken;

public interface TokenDAO {

    /**
     * Saves the given VerificationToken entity in the data storage.
     *
     * @param token The VerificationToken object to be saved.
     * @return The saved VerificationToken object.
     */
    VerificationToken saveToken(VerificationToken token);

    /**
     * Retrieves a VerificationToken entity by its unique token string.
     *
     * @param tokenStr The unique token string to be retrieved.
     * @return The VerificationToken associated with the token string, or null if not found.
     */
    VerificationToken getToken(String tokenStr);
}
