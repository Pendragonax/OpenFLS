package de.vinz.openfls.domains.overviews.exceptions

import de.vinz.openfls.exceptions.CsvCreationFailedException
import de.vinz.openfls.exceptions.IllegalTimeException
import de.vinz.openfls.exceptions.UserNotAllowedException
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

    @ExceptionHandler
    fun overviewUserNotAllowedException(ex: UserNotAllowedException): ResponseEntity<ErrorMessageModel> {
        val message = ErrorMessageModel(
                Date(),
                HttpStatus.FORBIDDEN.value(),
                ex.message)

        return ResponseEntity(message, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler
    fun overviewCsvCreationFailedException(ex: CsvCreationFailedException): ResponseEntity<ErrorMessageModel> {
        val message = ErrorMessageModel(
                Date(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.message)

        return ResponseEntity(message, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}