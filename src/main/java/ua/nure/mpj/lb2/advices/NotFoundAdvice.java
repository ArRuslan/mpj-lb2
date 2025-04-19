package ua.nure.mpj.lb2.advices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ua.nure.mpj.lb2.exceptions.EntityNotFoundException;
import ua.nure.mpj.lb2.responses.ErrorMessageResponse;

@RestControllerAdvice
public class NotFoundAdvice {
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessageResponse entityNotFoundHandler(EntityNotFoundException exc) {
        return new ErrorMessageResponse(exc.getMessage());
    }
}
