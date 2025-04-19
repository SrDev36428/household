package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.UserDAO;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.repository.user.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDAOImpl implements UserDAO {

    private final UserRepository userRepository;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findUsersBySearchTerm (String searchTerm) {
        return userRepository.searchByUsername(searchTerm);
    }

    @Override
    public Optional<User> findByUsername (String username) {
        return userRepository.findById(username);
    }

    @Override
    public Optional<User> findUserByEmail (String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public Boolean userExists (String username) {
        return userRepository.existsById(username);
    }

    @Override
    public void deleteUserByUsername (String username) {
        userRepository.deleteById(username);
    }

}
