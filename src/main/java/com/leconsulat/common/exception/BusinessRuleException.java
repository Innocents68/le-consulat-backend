package com.leconsulat.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised when a business rule from the cahier des charges is violated
 * (e.g. modifying a paid sale, insufficient stock, closing an already closed session).
 */
public class BusinessRuleException extends ApiException {
    public BusinessRuleException(String message) {
        super(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", message);
    }
}
