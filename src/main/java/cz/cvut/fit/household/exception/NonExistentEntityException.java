package cz.cvut.fit.household.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NonExistentEntityException extends RuntimeException {

    public NonExistentEntityException(String message) {
        super(message);
    }
}
