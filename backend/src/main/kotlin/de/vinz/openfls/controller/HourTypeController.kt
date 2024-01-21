package de.vinz.openfls.controller

import de.vinz.openfls.dtos.HourTypeDto
import de.vinz.openfls.entities.HourType
import de.vinz.openfls.services.HelperService
import de.vinz.openfls.services.HourTypeService
import org.modelmapper.ModelMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/hour_types")
class HourTypeController(
    private val hourTypeService: HourTypeService,
    private val modelMapper: ModelMapper,
    private val helperService: HelperService
) {
    @PostMapping
    fun create(@Valid @RequestBody value: HourTypeDto): Any {
        return try {
            val entity = hourTypeService.create(modelMapper.map(value, HourType::class.java))

            helperService.printLog(this::class.simpleName, "create [id=${entity.id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, HourTypeDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "create - ${ex.message}", true)

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
            if (id != valueDto.id)
                throw java.lang.IllegalArgumentException("path id and dto id are not the same")
            if (!hourTypeService.existsById(id))
                throw IllegalArgumentException("hour type not found")

            val entity = hourTypeService.update(modelMapper.map(valueDto, HourType::class.java))

            helperService.printLog(this::class.simpleName, "update [id=${entity.id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, HourTypeDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "update [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            if (!hourTypeService.existsById(id))
                throw IllegalArgumentException("hour type not found")

            val entity = hourTypeService.getById(id)
            hourTypeService.delete(id)

            helperService.printLog(this::class.simpleName, "delete [id=${id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, HourTypeDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "delete [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping
    fun getAll(): Any {
        return try {
            val entities = hourTypeService
                .getAll()
                .sortedBy { it.title.lowercase() }

            helperService.printLog(this::class.simpleName, "getAll", false)

            ResponseEntity.ok(entities.map { modelMapper.map(it, HourTypeDto::class.java) })
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getAll - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        return try {
            val entity = hourTypeService.getById(id)

            helperService.printLog(this::class.simpleName, "getById [id=${id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, HourTypeDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getById [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }
}