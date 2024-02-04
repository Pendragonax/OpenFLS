package de.vinz.openfls.controller

import de.vinz.openfls.dtos.SponsorDto
import de.vinz.openfls.entities.Sponsor
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.HelperService
import de.vinz.openfls.services.SponsorService
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import javax.validation.Valid

@RestController
@RequestMapping("/sponsors")
class SponsorController(
    val sponsorService: SponsorService,
    val modelMapper: ModelMapper
) {

    private val logger: Logger = LoggerFactory.getLogger(SponsorController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping("")
    fun create(@Valid @RequestBody valueDto: SponsorDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entity = sponsorService.create(modelMapper.map(valueDto, Sponsor::class.java))
            val dto = modelMapper.map(entity, SponsorDto::class.java)

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
               @Valid @RequestBody valueDto: SponsorDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (id != valueDto.id)
                throw IllegalArgumentException("path id and dto id are not the same")
            if (!sponsorService.existsById(id))
                throw IllegalArgumentException("sponsor not found")

            val entity = sponsorService.update(modelMapper.map(valueDto, Sponsor::class.java))
            val dto = modelMapper.map(entity, SponsorDto::class.java)

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

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!sponsorService.existsById(id))
                throw IllegalArgumentException("sponsor not found")

            val entity = sponsorService.getById(id)
            sponsorService.delete(id)
            val dto = modelMapper.map(entity, SponsorDto::class.java)

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

    @GetMapping
    fun getAll(): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val entities = sponsorService.getAll()
                .map { modelMapper.map(it, SponsorDto::class.java) }
                .sortedBy { it.name.lowercase() }

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(entities)
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

            val entity = modelMapper.map(sponsorService.getById(id), SponsorDto::class.java)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(entity)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }
}