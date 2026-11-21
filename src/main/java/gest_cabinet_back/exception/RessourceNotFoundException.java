package gest_cabinet_back.exception;

import org.springframework.http.HttpStatus;

public class RessourceNotFoundException extends ApiException {
    public RessourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
