package de.vinz.openfls.domains.evaluations.controller

import de.vinz.openfls.domains.evaluations.dtos.EvaluationRequestDto
import de.vinz.openfls.services.AccessService
import de.vinz.openfls.services.EmployeeService
import de.vinz.openfls.domains.evaluations.services.EvaluationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import javax.validation.Valid

@Controller
@RequestMapping("/evaluations")
class EvaluationController(
        private val evaluationService: EvaluationService,
        private val employeeService: EmployeeService,
        private val accessService: AccessService
) {

    private val logger: Logger = LoggerFactory.getLogger(EvaluationController::class.java)
    @PostMapping
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: EvaluationRequestDto): Any {
        return try {
            // find user
            val user = employeeService.getById(accessService.getId(token), true)
                    ?: throw IllegalArgumentException("User not found")

            ResponseEntity.ok(evaluationService.create(valueDto, user))
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping
    fun update(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: EvaluationRequestDto): Any {
        return try {
            // find user
            val user = employeeService.getById(accessService.getId(token), true)
                    ?: throw IllegalArgumentException("User not found")

            ResponseEntity.ok(evaluationService.update(valueDto, user))
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(evaluationService.delete(id))
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.localizedMessage,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping
    fun getAll(): Any {
        return try {
            ResponseEntity.ok(evaluationService.getAll())
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}")
    fun getById(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                              @PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(evaluationService.getById(id))
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("assistance_plan/{assistancePlanId}")
    fun getByAssistancePlanId(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                              @PathVariable assistancePlanId: Long): Any {
        return try {
            ResponseEntity.ok(evaluationService.getByAssistancePlanId(assistancePlanId))
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("assistance_plan/{assistancePlanId}/{year}")
    fun getByAssistancePlanIdAndYear(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                     @PathVariable assistancePlanId: Long,
                                     @PathVariable year: Int): Any {
        return try {
            ResponseEntity.ok(evaluationService.getByAssistancePlanIdAndYear(assistancePlanId, year))
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }
}