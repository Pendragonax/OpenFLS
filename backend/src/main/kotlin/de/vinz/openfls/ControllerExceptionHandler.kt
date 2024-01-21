package de.vinz.openfls

import de.vinz.openfls.domains.goalTimeEvaluations.exceptions.NoGoalFoundWithHourTypeException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class ControllerExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException, request: WebRequest?): ResponseEntity<Any> {
        val name = ex.name
        val type = ex.requiredType!!.simpleName
        val value = ex.value
        val message = String.format("'%s' should be a valid '%s' and '%s' isn't", name, type, value)
        return handleExceptionInternal(ex, message, HttpHeaders(), HttpStatus.BAD_REQUEST, request!!);
    }

    @ExceptionHandler(NoGoalFoundWithHourTypeException::class)
    fun handleTypeMismatch(ex: NoGoalFoundWithHourTypeException, request: WebRequest?): ResponseEntity<Any> {
        val message = ex.message
        return handleExceptionInternal(ex, message, HttpHeaders(), HttpStatus.BAD_REQUEST, request!!);
    }
}