package com.dj.glacierCRUD.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AWSCredentialsException extends RuntimeException {
    /**
	 *
	 */
	private static final long serialVersionUID = 2412931777914925372L;

	public AWSCredentialsException(String message) {
        super(message);
    }

    public AWSCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}