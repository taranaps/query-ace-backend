package com.queryapplication.exception;

import com.queryapplication.dto.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

    @ControllerAdvice
    public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
        // handle specific exceptions
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException exception,
                                                                            WebRequest webRequest){
            ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(),
                    webRequest.getDescription(false));
            return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
        }
        //handle Bad Request exceptions
        @ExceptionHandler(APIBadRequestException.class)
        public ResponseEntity<ErrorDetails> handleAPIBadRequestException(APIBadRequestException exception,
                                                                         WebRequest webRequest){
            ErrorDetails errorDetails = new ErrorDetails(new Date(), exception.getMessage(),
                    webRequest.getDescription(false));
            return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
        }
    }


