package dev.ishikawa.dd_rxjava.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
@AllArgsConstructor
public class ApplicationExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
		log.error("Unhandled error", ex);
		return new ResponseEntity<>(
        ErrorResponse.builder().message(ex.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Getter
	@Builder
	static class ErrorResponse {

		private final String message;
	}
}
