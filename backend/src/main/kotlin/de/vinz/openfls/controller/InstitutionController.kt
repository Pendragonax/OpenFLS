package de.vinz.openfls.controller

import de.vinz.openfls.dtos.AssistancePlanDto
import de.vinz.openfls.dtos.InstitutionDto
import de.vinz.openfls.model.Institution
import de.vinz.openfls.services.HelperService
import de.vinz.openfls.services.InstitutionService
import org.modelmapper.ModelMapper
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
    private val helperService: HelperService,
    private val modelMapper: ModelMapper
) {
    @PostMapping
    fun create(@Valid @RequestBody valueDto: InstitutionDto): Any {
        return try {
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

            helperService.printLog(this::class.simpleName, "create [id=${valueDto.id}]", false)

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "create [id=${valueDto.id}] - ${ex.message}", true)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody valueDto: InstitutionDto): Any {
        return try {
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

            helperService.printLog(this::class.simpleName, "update [id=${savedEntity.id}]", false)

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "update [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            if (!institutionService.existsById(id))
                throw IllegalArgumentException("institution not found")

            val entity = institutionService.getById(id)
            institutionService.delete(id)

            helperService.printLog(this::class.simpleName, "delete [id=${id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, InstitutionDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "delete [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        return try {
            val dtos = institutionService
                .getAll()
                .sortedBy { it.name }
                .map { value -> modelMapper.map(value, InstitutionDto::class.java) }

            helperService.printLog(this::class.simpleName, "getAll", false)

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getAll - ${ex.message}", true)

            ResponseEntity(
                emptyList(),
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        return try {
            val entity = institutionService.getById(id)

            helperService.printLog(this::class.simpleName, "getById [id=${id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, InstitutionDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getById [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }
}