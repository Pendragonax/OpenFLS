package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.contingents.services.ContingentService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import jakarta.validation.Valid
import kotlin.IllegalArgumentException

@RestController
@RequestMapping("/contingents")
class ContingentController(
        private val contingentService: ContingentService,
        private val modelMapper: ModelMapper,
        private val accessService: AccessService) {

    private val logger: Logger = LoggerFactory.getLogger(ContingentController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: ContingentDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.isLeader(token, valueDto.institutionId))
                throw IllegalArgumentException("no permission to add this contingent")
            if (valueDto.end != null && valueDto.start >= valueDto.end)
                throw IllegalArgumentException("end before start")

            var entity = modelMapper.map(valueDto, Contingent::class.java)
            entity = contingentService.create(entity)
            val dto = modelMapper.map(entity, ContingentDto::class.java)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long,
               @Valid @RequestBody valueDto: ContingentDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.canModifyContingent(token, id))
                throw IllegalArgumentException("no permission to update this contingent")
            if (id != valueDto.id)
                throw java.lang.IllegalArgumentException("path id and dto id are not the same")
            if (!contingentService.existsById(id))
                throw IllegalArgumentException("contingent not found")
            if (valueDto.end != null && valueDto.start >= valueDto.end)
                throw IllegalArgumentException("end before start")

            // convert dto to entity
            var entity = modelMapper.map(valueDto, Contingent::class.java)
            entity = contingentService.update(entity)
            val dto = modelMapper.map(entity, ContingentDto::class.java)

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
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
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to delete this contingent")
            if (!contingentService.existsById(id))
                throw IllegalArgumentException("contingent not found")

            val entity = contingentService.getById(id)
            val dto = modelMapper.map(entity, ContingentDto::class.java)

            contingentService.delete(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = contingentService
                .getAll()
                .map { modelMapper.map(it, ContingentDto::class.java) }
                .sortedBy { it.start }

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any  {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = modelMapper.map(contingentService.getById(id), ContingentDto::class.java)

            if (logPerformance) {
                logger.info(String.format("%s getById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("employee/{id}")
    fun getByEmployeeId(@PathVariable id: Long): Any  {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = contingentService.getByEmployeeId(id)
                .map { modelMapper.map(it, ContingentDto::class.java) }

            if (logPerformance) {
                logger.info(String.format("%s getByEmployeeId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("institution/{id}")
    fun getByInstitutionId(@PathVariable id: Long): Any  {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = contingentService.getByInstitutionId(id)
                .map { modelMapper.map(it, ContingentDto::class.java) }

            if (logPerformance) {
                logger.info(String.format("%s getByInstitutionId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }
}