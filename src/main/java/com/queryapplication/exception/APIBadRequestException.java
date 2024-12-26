package com.queryapplication.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class APIBadRequestException extends RuntimeException {

    private HttpStatus status;
    private String message;

}