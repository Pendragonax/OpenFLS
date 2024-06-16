package de.vinz.openfls.domains.goalTimeEvaluations;

import de.vinz.openfls.domains.goalTimeEvaluations.exceptions.NoGoalFoundWithHourTypeException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GoalTimeEvaluationExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(NoGoalFoundWithHourTypeException::class)
    fun handleTypeMismatch(ex: NoGoalFoundWithHourTypeException, request:WebRequest?): ResponseEntity<Any>? {
        val message = ex.message
        return handleExceptionInternal(ex, message, HttpHeaders(), HttpStatus.BAD_REQUEST, request!!)
    }
}
