package de.vinz.openfls.exceptionResolver

import de.vinz.openfls.exceptions.IllegalTimeException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.*

@ControllerAdvice
class OverviewExceptionResolver: ResponseEntityExceptionHandler() {
    @ExceptionHandler
    fun overviewIllegalTimeException(ex: IllegalTimeException): ResponseEntity<ErrorMessageModel> {
        val message = ErrorMessageModel(
                Date(),
                HttpStatus.BAD_REQUEST.value(),
                ex.message)

        return ResponseEntity(message, HttpStatus.BAD_REQUEST)
    }
}