package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.interfaces.TokenDAO;
import cz.cvut.fit.household.daos.interfaces.UserDAO;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.entity.user.VerificationToken;
import cz.cvut.fit.household.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    private final TokenDAO tokenDAO;

    @Override
    public User createOrUpdateUser(User user) {
        return userDAO.saveUser(user);
    }

    @Override
    public List<User> findAllUsers() {
        return userDAO.findAllUsers();
    }

    @Override
    public List<User> findUsersBySearchTerm(String searchTerm) { return userDAO.findUsersBySearchTerm(searchTerm); }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userDAO.findUserByEmail(email);
    }

    @Override
    public Boolean exists(String username) {
        return userDAO.userExists(username);
    }

    @Override
    public void deleteUserByUsername(String username) {
        userDAO.deleteUserByUsername(username);
    }

    @Override
    public void createVerificationToken(User user, String token) {
        VerificationToken myToken = new VerificationToken(token, user);
        tokenDAO.saveToken(myToken);
    }

    @Override
    public VerificationToken getVerificationToken(String tokenString) {
        return tokenDAO.getToken(tokenString);
    }
}
