package de.vinz.openfls.controller

import de.vinz.openfls.dtos.ClientDto
import de.vinz.openfls.dtos.ClientSimpleDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.entities.Client
import de.vinz.openfls.services.*
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Exception
import javax.validation.Valid
import kotlin.IllegalArgumentException

@RestController
@RequestMapping("/clients")
class ClientController(
    private val clientService: ClientService,
    private val accessService: AccessService,
    private val modelMapper: ModelMapper) {

    private val logger: Logger = LoggerFactory.getLogger(ClientController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody value: ClientDto): Any {
        return try {
            // performance
            var startMs = System.currentTimeMillis()

            if (!accessService.isLeader(token, value.institution.id))
                throw IllegalArgumentException("no permission to add clients")

            if (logPerformance) {
                logger.info(String.format("%s access check took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
                startMs = System.currentTimeMillis()
            }

            val entity = clientService.create(modelMapper.map(value, Client::class.java))
            val dto = modelMapper.map(entity, ClientDto::class.java)

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
    fun update(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long,
               @Valid @RequestBody valueDto: ClientDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (id != valueDto.id)
                throw java.lang.IllegalArgumentException("path id and dto id are not the same")
            if (!clientService.existById(id))
                throw IllegalArgumentException("client not found")
            if (!accessService.canModifyClient(token, valueDto.id))
                throw IllegalArgumentException("no permission to update this client")

            val entity = clientService.update(modelMapper.map(valueDto, Client::class.java))
            val dto = modelMapper.map(entity, ClientDto::class.java)

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
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to delete this client")
            if (!clientService.existById(id))
                throw IllegalArgumentException("client not found")

            val entity = clientService.getById(id) ?: throw IllegalArgumentException("client not found")

            clientService.delete(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(modelMapper.map(entity, ClientDto::class.java))
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

            val dtos = clientService.getAllSimple()
                .sortedBy { it.lastName.lowercase() }

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

            val dto = modelMapper.map(clientService.getById(id), ClientDto::class.java)
            dto.categoryTemplate.categories = dto.categoryTemplate.categories.sortedBy { it.shortcut }.toTypedArray()

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