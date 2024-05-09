package be.kuleuven.safetyrestservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@ControllerAdvice
class ReservationAdvice {

    @ResponseBody
    @ExceptionHandler(ReservationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String reservationHandler(ReservationException ex) {
        return ex.getMessage();
    }
}
