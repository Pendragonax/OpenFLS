package de.vinz.openfls.controller

import de.vinz.openfls.dtos.HourTypeDto
import de.vinz.openfls.entities.HourType
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.HelperService
import de.vinz.openfls.services.HourTypeService
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/hour_types")
class HourTypeController(
    private val hourTypeService: HourTypeService,
    private val modelMapper: ModelMapper
) {

    private val logger: Logger = LoggerFactory.getLogger(HourTypeController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody value: HourTypeDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entity = hourTypeService.create(modelMapper.map(value, HourType::class.java))
            val dto = modelMapper.map(entity, HourTypeDto::class.java)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody valueDto: HourTypeDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (id != valueDto.id)
                throw java.lang.IllegalArgumentException("path id and dto id are not the same")
            if (!hourTypeService.existsById(id))
                throw IllegalArgumentException("hour type not found")

            val entity = hourTypeService.update(modelMapper.map(valueDto, HourType::class.java))
            val dto = modelMapper.map(entity, HourTypeDto::class.java)

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!hourTypeService.existsById(id))
                throw IllegalArgumentException("hour type not found")

            val entity = hourTypeService.getById(id)
            hourTypeService.delete(id)
            val dto = modelMapper.map(entity, HourTypeDto::class.java)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entities = hourTypeService
                .getAll()
                .sortedBy { it.title.lowercase() }
            val dtos = entities.map { modelMapper.map(it, HourTypeDto::class.java) }

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
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entity = hourTypeService.getById(id)
            val dto = modelMapper.map(entity, HourTypeDto::class.java)

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
                HttpStatus.BAD_REQUEST
            )
        }
    }
}