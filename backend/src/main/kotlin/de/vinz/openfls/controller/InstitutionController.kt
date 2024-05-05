package de.vinz.openfls.controller

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.dtos.InstitutionDto
import de.vinz.openfls.entities.Institution
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.InstitutionService
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.validation.Valid

@RestController
@RequestMapping("/institutions")
class InstitutionController(
    private val institutionService: InstitutionService,
    private val modelMapper: ModelMapper
) {

    private val logger: Logger = LoggerFactory.getLogger(InstitutionController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody valueDto: InstitutionDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            // convert
            val entity = modelMapper.map(valueDto.apply { assistancePlans = null }, Institution::class.java)

            // save without assistance plans
            val savedEntity = institutionService.create(entity)

            // set id to dto
            valueDto.id = savedEntity.id!!

            // only created permissions
            valueDto.permissions = valueDto
                .permissions
                ?.filter {
                    savedEntity
                        .permissions
                        ?.any { permission -> permission.id.employeeId == it.employeeId } ?: false }
                ?.map { it.apply { institutionId = savedEntity.id!! } }
                ?.toTypedArray()

            // assistance plans
            valueDto.assistancePlans = savedEntity.assistancePlans
                ?.map { modelMapper.map(it, AssistancePlanDto::class.java) }?.toTypedArray()

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody valueDto: InstitutionDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (id != valueDto.id)
                throw IllegalArgumentException("path id and dto id are not the same")
            if (!institutionService.existsById(id))
                throw IllegalArgumentException("institution not found")

            val entity = modelMapper.map(valueDto
                .apply { assistancePlans = null }, Institution::class.java)
            val savedEntity = institutionService.update(entity)

            // only created or updated permissions
            valueDto.permissions = valueDto
                .permissions
                ?.filter { savedEntity
                    .permissions
                    ?.any { permission -> permission.id.employeeId == it.employeeId } ?: false }
                ?.map { it.apply { institutionId = savedEntity.id!! } }
                ?.toTypedArray()

            // assistance plans
            valueDto.assistancePlans = savedEntity.assistancePlans
                ?.map { modelMapper.map(it, AssistancePlanDto::class.java) }?.toTypedArray()

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!institutionService.existsById(id))
                throw IllegalArgumentException("institution not found")

            val entity = institutionService.getById(id)
            institutionService.delete(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(modelMapper.map(entity, InstitutionDto::class.java))
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = institutionService
                .getAll()
                .sortedBy { it.name.lowercase() }
                .map { value -> modelMapper.map(value, InstitutionDto::class.java) }

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                emptyList(),
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entity = institutionService.getById(id)
            val dto = modelMapper.map(entity, InstitutionDto::class.java)

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
}