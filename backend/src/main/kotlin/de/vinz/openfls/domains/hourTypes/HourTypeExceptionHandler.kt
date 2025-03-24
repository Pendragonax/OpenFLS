package de.vinz.openfls.domains.hourTypes

import de.vinz.openfls.domains.hourTypes.exceptions.InvalidHourTypeDtoException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class HourTypeExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(InvalidHourTypeDtoException::class)
    fun handleTypeMismatch(ex: InvalidHourTypeDtoException, request: WebRequest?): ResponseEntity<Any>? {
        val message = ex.message
        return handleExceptionInternal(ex, message, HttpHeaders(), HttpStatus.BAD_REQUEST, request!!);
    }
}