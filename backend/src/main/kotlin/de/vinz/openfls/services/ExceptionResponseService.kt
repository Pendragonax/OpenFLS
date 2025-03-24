package de.vinz.openfls.services

import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ExceptionResponseService {
    companion object {
        fun getExceptionResponseEntity(ex: Exception, logger: Logger): ResponseEntity<String> {
            logger.error(ex.message, ex)

            return ResponseEntity(
                    "Es trat ein unbekannter Fehler auf. Bitte wenden sie sich an ihren Administrator",
                    HttpStatus.BAD_REQUEST
            )
        }

        fun getIllegalArgumentExceptionResponseEntity(ex: Exception, logger: Logger): ResponseEntity<String> {
            logger.error(ex.message, ex)

            return ResponseEntity(
                "Die übergebenen Parameter sind nicht korrekt. Bitte überprüfen sie ihre Eingabe",
                HttpStatus.BAD_REQUEST
            )
        }

        fun getPermissionDeniedResponseEntity(ex: Exception, logger: Logger): ResponseEntity<String> {
            logger.error(ex.message, ex)

            return ResponseEntity(
                "Sie haben keine Berechtigung für diese Aktion",
                HttpStatus.FORBIDDEN
            )
        }
    }
}