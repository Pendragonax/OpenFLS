package de.vinz.openfls.domains.hourCorridors

import de.vinz.openfls.domains.hourCorridors.exceptions.InvalidHourCorridorDtoException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class HourCorridorExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(InvalidHourCorridorDtoException::class)
    fun handleTypeMismatch(ex: InvalidHourCorridorDtoException, request: WebRequest?): ResponseEntity<Any>? {
        val message = ex.message
        return handleExceptionInternal(ex, message, HttpHeaders(), HttpStatus.BAD_REQUEST, request!!)
    }
}
