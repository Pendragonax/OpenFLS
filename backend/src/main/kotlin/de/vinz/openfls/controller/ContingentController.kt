package de.vinz.openfls.controller

import de.vinz.openfls.dtos.ContingentDto
import de.vinz.openfls.model.Contingent
import de.vinz.openfls.services.*
import org.modelmapper.ModelMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import javax.validation.Valid
import kotlin.IllegalArgumentException

@RestController
@RequestMapping("/contingents")
class ContingentController(
    private val contingentService: ContingentService,
    private val modelMapper: ModelMapper,
    private val accessService: AccessService,
    private val helperService: HelperService
) {
    @PostMapping
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: ContingentDto): Any {
        return try {
            if (!accessService.isLeader(token, valueDto.institutionId))
                throw IllegalArgumentException("no permission to add this contingent")

            var entity = modelMapper.map(valueDto, Contingent::class.java)
            entity = contingentService.create(entity)

            helperService.printLog(this::class.simpleName, "create [id=${valueDto.id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, ContingentDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "create - ${ex.message}", true)

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
            if (!accessService.canModifyContingent(token, id))
                throw IllegalArgumentException("no permission to update this contingent")
            if (id != valueDto.id)
                throw java.lang.IllegalArgumentException("path id and dto id are not the same")
            if (!contingentService.existsById(id))
                throw IllegalArgumentException("contingent not found")

            // convert dto to entity
            var entity = modelMapper.map(valueDto, Contingent::class.java)
            entity = contingentService.update(entity)

            helperService.printLog(this::class.simpleName, "update [id=${valueDto.id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, ContingentDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "update [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)

        }
    }

    @DeleteMapping("{id}")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long): Any {
        return try {
            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to delete this contingent")
            if (!contingentService.existsById(id))
                throw IllegalArgumentException("contingent not found")

            val entity = contingentService.getById(id)

            contingentService.delete(id)

            helperService.printLog(this::class.simpleName, "delete [id=${id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, ContingentDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "delete [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any {
        return try {
            val dtos = contingentService
                .getAll()
                .map { modelMapper.map(it, ContingentDto::class.java) }
                .sortedBy { it.start }

            helperService.printLog(this::class.simpleName, "getAll", false)

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getAll - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any  {
        return try {
            val dto = modelMapper.map(contingentService.getById(id), ContingentDto::class.java)

            helperService.printLog(this::class.simpleName, "getById [id=${id}]", false)

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getById [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("employee/{id}")
    fun getByEmployeeId(@PathVariable id: Long): Any  {
        return try {
            val dtos = contingentService.getByEmployeeId(id)
                .map { modelMapper.map(it, ContingentDto::class.java) }

            helperService.printLog(this::class.simpleName, "getByEmployeeId [id=${id}]", false)

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getByEmployeeId [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("institution/{id}")
    fun getByInstitutionId(@PathVariable id: Long): Any  {
        return try {
            val dtos = contingentService.getByInstitutionId(id)
                .map { modelMapper.map(it, ContingentDto::class.java) }

            helperService.printLog(this::class.simpleName, "getByInstitutionId [id=${id}]", false)

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getByInstitutionId [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }
}