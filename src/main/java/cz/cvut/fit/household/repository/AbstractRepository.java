package cz.cvut.fit.household.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generalization of the repository main function. Created to solve problem related to
 * dependency on Hibernate.
 *
 * @param <K> key of the repository
 * @param <T> value which has to be linked with key
 */
public interface AbstractRepository<K, T> {

    /**
     * Save object to database.
     *
     * @param object which has to saved
     * @return freshly saved object
     */
    T save(T object);

    /**
     * Searching for the object in database, using key
     *
     * @param id of the needed object
     * @return object with given id, or null if it does not exist in database
     */
    Optional<T> findById(K id);

    /**
     * @return all objects in this repository
     */
    List<T> findAll();

    /**
     * Searching for the object in database, using key
     *
     * @param id of the needed object
     * @return true if object with given id exists, otherwise false
     */
    boolean existsById(K id);

    /**
     * Searching and deleting object form database, using key
     *
     * @param id of needed object
     */
    void deleteById(K id);
}
