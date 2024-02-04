package de.vinz.openfls.controller

import de.vinz.openfls.dtos.GoalDto
import de.vinz.openfls.dtos.GoalHourDto
import de.vinz.openfls.entities.Goal
import de.vinz.openfls.entities.GoalHour
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.*
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/goals")
class GoalController(
    private val goalService: GoalService,
    private val hourTypeService: HourTypeService,
    private val assistancePlanService: AssistancePlanService,
    private val institutionService: InstitutionService,
    private val accessService: AccessService,
    private val modelMapper: ModelMapper
) {

    private val logger: Logger = LoggerFactory.getLogger(GoalController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping("")
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: GoalDto
    ): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.canModifyAssistancePlan(token, valueDto.assistancePlanId))
                throw IllegalArgumentException("no permission to create goals to this assistance plan")

            val entity = modelMapper.map(valueDto, Goal::class.java)

            entity.assistancePlan = assistancePlanService.getById(valueDto.assistancePlanId)
                ?: throw IllegalArgumentException("assistance plan [id = ${valueDto.assistancePlanId}] not found")

            if (valueDto.institutionId != null) {
                entity.institution = institutionService.getById(valueDto.institutionId!!)
                    ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
            }

            entity.hours = valueDto.hours
                .map { modelMapper.map(it, GoalHour::class.java).apply {
                    hourType = hourTypeService.getById(it.hourTypeId)
                        ?: throw IllegalArgumentException("hour type with id ${hourType.id} not found")
                } }
                .toMutableSet()

            val savedEntity = goalService.create(entity)

            valueDto.apply {
                id = savedEntity.id
                hours = savedEntity.hours
                    .map { modelMapper.map(it, GoalHourDto::class.java) }
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
               @Valid @RequestBody valueDto: GoalDto
    ): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            if (!accessService.canModifyAssistancePlan(token, valueDto.assistancePlanId))
                throw IllegalArgumentException("no permission to update this goal to this assistance plan")
            if (id != valueDto.id)
                throw java.lang.IllegalArgumentException("path id and dto id are not the same")
            if (!goalService.existsById(id))
                throw IllegalArgumentException("goal not found")

            val entity = modelMapper.map(valueDto, Goal::class.java)

            entity.assistancePlan = assistancePlanService.getById(valueDto.assistancePlanId)
                ?: throw IllegalArgumentException("assistance plan [id = ${valueDto.assistancePlanId}] not found")

            if (valueDto.institutionId != null) {
                entity.institution = institutionService.getById(valueDto.institutionId!!)
                    ?: throw IllegalArgumentException("institution [id = ${valueDto.institutionId}] not found")
            }

            entity.hours = valueDto.hours
                .map { modelMapper.map(it, GoalHour::class.java).apply {
                    hourType = hourTypeService.getById(it.hourTypeId)
                        ?: throw IllegalArgumentException("hour type with id ${hourType.id} not found")
                } }
                .toMutableSet()

            val savedEntity = goalService.update(entity)

            valueDto.apply {
                this.id = savedEntity.id
                hours = savedEntity.hours
                    .map { modelMapper.map(it, GoalHourDto::class.java) }
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
                throw IllegalArgumentException("no permission to delete this goal to this assistance plan")
            if (!goalService.existsById(id))
                throw IllegalArgumentException("goal not found")

            val dto = modelMapper.map(goalService.getById(id), GoalDto::class.java)

            goalService.delete(id)

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

    @GetMapping("assistance_plan/{id}")
    fun getByAssistancePlanId(@PathVariable id: Long): Any {
        return try {
            // performance
            val startMs = System.currentTimeMillis()

            val dtos = goalService.getByAssistancePlanId(id).map {
                modelMapper.map(it, GoalDto::class.java) ?: throw IllegalArgumentException()
            }

            if (logPerformance) {
                logger.info(String.format("%s getByAssistancePlanId took %s ms",
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
}