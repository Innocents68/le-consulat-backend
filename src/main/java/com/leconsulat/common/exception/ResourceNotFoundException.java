package com.leconsulat.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }

    public static ResourceNotFoundException of(String entite, Object id) {
        return new ResourceNotFoundException(entite + " introuvable (id=" + id + ")");
    }
}
