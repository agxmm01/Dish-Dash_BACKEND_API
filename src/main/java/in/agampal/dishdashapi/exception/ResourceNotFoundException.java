package in.agampal.dishdashapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResourceNotFoundException extends ResponseStatusException {
    public ResourceNotFoundException(String resource, String field, String value) {
        super(HttpStatus.NOT_FOUND, String.format("%s not found with %s: %s", resource, field, value));
    }

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}


