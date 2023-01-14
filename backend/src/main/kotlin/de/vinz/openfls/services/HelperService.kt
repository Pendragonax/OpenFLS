package de.vinz.openfls.services

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Level
import java.util.logging.Logger

@Service
class HelperService {
    /**
     * This method will return the current Timestamp with a given format.
     */
    fun getDateTimeNow(): String = LocalDateTime.now().format(formatter)

    /**
     * Prints the given information to the console. Errors will have a red background.
     * */
    fun printLog(title: String?, description: String, error: Boolean) {
        LOG.log(
            if (error) Level.WARNING else Level.INFO,
            (if (error) ANSI_RED else "") +
                    "[$title] ${if (error) "[WARNING]" else ""} $description" +
                    (if (error) ANSI_RESET else ""))
    }

    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val LOG = Logger.getLogger("OpenFls")
        const val ANSI_RED: String = "\u001B[41m"
        const val ANSI_RESET: String = "\u001B[0m"
    }
}