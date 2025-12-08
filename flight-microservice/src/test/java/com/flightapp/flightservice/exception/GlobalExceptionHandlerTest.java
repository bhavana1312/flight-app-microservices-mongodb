package com.flightapp.flightservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFound() {
        var ex = new ResourceNotFoundException("Not found");
        ResponseEntity<?> r = handler.handleNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
    }

    @Test
    void testHandleBadRequest() {
        var ex = new BadRequestException("Bad");
        ResponseEntity<?> r = handler.handleBadRequest(ex);
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
    }

    @Test
    void testHandleValidation() {
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, new BeanPropertyBindingResult("obj", "obj"));

        ResponseEntity<?> r = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
    }

    @Test
    void testHandleOther() {
        var ex = new RuntimeException("x");
        ResponseEntity<?> r = handler.handleOther(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
    }
}
