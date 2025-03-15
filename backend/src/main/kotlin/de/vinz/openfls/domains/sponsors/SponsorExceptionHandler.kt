package de.vinz.openfls.domains.sponsors

import de.vinz.openfls.domains.sponsors.exceptions.InvalidSponsorDtoException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class SponsorExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(InvalidSponsorDtoException::class)
    fun handleTypeMismatch(ex: InvalidSponsorDtoException, request: WebRequest?): ResponseEntity<Any>? {
        val message = ex.message
        return handleExceptionInternal(ex, message, HttpHeaders(), HttpStatus.BAD_REQUEST, request!!);
    }
}