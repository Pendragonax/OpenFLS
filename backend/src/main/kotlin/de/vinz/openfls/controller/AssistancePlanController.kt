package de.vinz.openfls.controller

import de.vinz.openfls.dtos.AssistancePlanDto
import de.vinz.openfls.dtos.AssistancePlanHourDto
import de.vinz.openfls.dtos.AssistancePlanResponseDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.entities.AssistancePlan
import de.vinz.openfls.entities.AssistancePlanHour
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
import javax.validation.Valid

@RestController
@RequestMapping("/assistance_plans")
class AssistancePlanController(
    private val assistancePlanService: AssistancePlanService,
    private val modelMapper: ModelMapper,
    private val institutionService: InstitutionService,
    private val sponsorService: SponsorService,
    private val clientService: ClientService,
    private val hourTypeService: HourTypeService,
    private val accessService: AccessService
) {
    private val logger: Logger = LoggerFactory.getLogger(AssistancePlanController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping("")
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: AssistancePlanDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.isAffiliated(token, valueDto.institutionId))
                throw IllegalArgumentException("user is not allowed to create assistance plans for this client")

            val entity = modelMapper.map(valueDto, AssistancePlan::class.java)

            entity.client = clientService.getById(valueDto.clientId)
                ?: throw IllegalArgumentException("client [id = ${valueDto.clientId}] not found")
            entity.institution = institutionService.getById(valueDto.institutionId)
                ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
            entity.sponsor = sponsorService.getById(valueDto.sponsorId)
                ?: throw IllegalArgumentException("sponsor [id = ${valueDto.sponsorId}] not found")
            entity.hours = valueDto.hours
                .map { modelMapper.map(it, AssistancePlanHour::class.java) }
                .map { it.apply {
                    hourType = hourTypeService.getById(it.hourType.id)
                        ?: throw IllegalArgumentException("hour type with id ${hourType.id} not found")
                } }
                .toMutableSet()

            val savedEntity = assistancePlanService.create(entity)

            valueDto.apply {
                id = savedEntity.id
                hours = savedEntity.hours
                    .map { modelMapper.map(it, AssistancePlanHourDto::class.java) }
                    .toMutableSet()
            }

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(valueDto)
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
               @Valid @RequestBody valueDto: AssistancePlanDto): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.canModifyAssistancePlan(token, id))
                throw IllegalArgumentException("user is not allowed to update this assistance plan")
            if (id != valueDto.id)
                throw IllegalArgumentException("path id and dto id are not the same")
            if (!assistancePlanService.existsById(id))
                throw IllegalArgumentException("assistance plan not found")

            val entity = modelMapper.map(valueDto, AssistancePlan::class.java)

            entity.client = clientService.getById(valueDto.clientId)
                ?: throw IllegalArgumentException("client [id = ${valueDto.clientId}] not found")
            entity.institution = institutionService.getById(valueDto.institutionId)
                ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
            entity.sponsor = sponsorService.getById(valueDto.sponsorId)
                ?: throw IllegalArgumentException("sponsor [id = ${valueDto.sponsorId}] not found")
            entity.hours = valueDto.hours
                .map { modelMapper.map(it, AssistancePlanHour::class.java) }
                .map { it.apply {
                    hourType = hourTypeService.getById(it.hourType.id)
                        ?: throw IllegalArgumentException("hour type with id ${hourType.id} not found")
                } }
                .toMutableSet()

            val savedEntity = assistancePlanService.update(entity)

            valueDto.apply {
                this.id = savedEntity.id
                hours = savedEntity.hours
                    .map { modelMapper.map(it, AssistancePlanHourDto::class.java) }
                    .toMutableSet()
            }

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
                throw IllegalArgumentException("user is not allowed to delete assistance plans for this client")
            if (!assistancePlanService.existsById(id))
                throw IllegalArgumentException("assistance plan not found")

            val entity = assistancePlanService.getById(id)
            val dto = modelMapper.map(entity, AssistancePlanDto::class.java)

            assistancePlanService.delete(id)

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

            val dtos = assistancePlanService.getAll()
                .map { modelMapper.map(it, AssistancePlanDto::class.java) }

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
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

            val entity = assistancePlanService.getById(id)
            val dto = modelMapper.map(entity, AssistancePlanResponseDto::class.java)

            if (logPerformance) {
                logger.info(String.format("%s getById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("client/{id}")
    fun getByClientId(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = assistancePlanService.getByClientId(id)
                .map { modelMapper.map(it, AssistancePlanDto::class.java) }

            if (logPerformance) {
                logger.info(String.format("%s getByClientId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("sponsor/{id}")
    fun getBySponsorId(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = assistancePlanService.getBySponsorId(id)
                .map { modelMapper.map(it, AssistancePlanDto::class.java) }

            if (logPerformance) {
                logger.info(String.format("%s getBySponsorId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("institution/{id}")
    fun getByInstitutionId(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = assistancePlanService.getByInstitutionId(id)
                .map { modelMapper.map(it, AssistancePlanDto::class.java) }

            if (logPerformance) {
                logger.info(String.format("%s getByInstitutionId took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("eval/{id}")
    fun getEvalById(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dto = assistancePlanService.getEvaluationById(id)

            if (logPerformance) {
                logger.info(String.format("%s getEvalById took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch(ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }
}