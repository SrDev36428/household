package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.TokenDAO;
import cz.cvut.fit.household.datamodel.entity.user.VerificationToken;
import cz.cvut.fit.household.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenDAOImpl implements TokenDAO {

    private final VerificationTokenRepository tokenRepository;

    @Override
    public VerificationToken saveToken (VerificationToken token) {
        return tokenRepository.save(token);
    }

    @Override
    public VerificationToken getToken(String tokenStr) {
        return tokenRepository.findByToken(tokenStr);
    }

}
