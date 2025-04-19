package cz.cvut.fit.household.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MembershipAlreadyExistsException extends RuntimeException {
    public MembershipAlreadyExistsException() {
    }

    public MembershipAlreadyExistsException(String message) {
        super(message);
    }
}
