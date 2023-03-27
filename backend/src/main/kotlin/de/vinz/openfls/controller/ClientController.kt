package de.vinz.openfls.controller

import de.vinz.openfls.dtos.CategoryTemplateDto
import de.vinz.openfls.dtos.ClientDto
import de.vinz.openfls.dtos.ClientSimpleDto
import de.vinz.openfls.model.Client
import de.vinz.openfls.services.*
import org.modelmapper.ModelMapper
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
    private val helperService: HelperService,
    private val accessService: AccessService,
    private val modelMapper: ModelMapper

) {
    @PostMapping
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody value: ClientDto): Any {
        return try {
            if (!accessService.isLeader(token, value.institution.id))
                throw IllegalArgumentException("no permission to add clients")

            val entity = clientService.create(modelMapper.map(value, Client::class.java))

            helperService.printLog(this::class.simpleName, "create", false)

            ResponseEntity.ok(modelMapper.map(entity, ClientDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "create - ${ex.message}", true)

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
            if (id != valueDto.id)
                throw java.lang.IllegalArgumentException("path id and dto id are not the same")
            if (!clientService.existById(id))
                throw IllegalArgumentException("client not found")
            if (!accessService.canModifyClient(token, valueDto.id))
                throw IllegalArgumentException("no permission to update this client")

            val entity = clientService.update(modelMapper.map(valueDto, Client::class.java))

            helperService.printLog(this::class.simpleName, "update [id=$id]", false)

            ResponseEntity.ok(modelMapper.map(entity, ClientDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "update [id=$id] - ${ex.message}", true)

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
            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to delete this client")
            if (!clientService.existById(id))
                throw IllegalArgumentException("client not found")

            val entity = clientService.getById(id) ?: throw IllegalArgumentException("client not found")

            clientService.delete(id)

            helperService.printLog(this::class.simpleName, "delete [id=$id]", false)

            ResponseEntity.ok(modelMapper.map(entity, ClientDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "delete [id=$id] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping
    fun getAll(): Any {
        return try {
            val dtos = clientService.getAll()
                .map { modelMapper.map(it, ClientSimpleDto::class.java)}
                .sortedBy { it.lastName.lowercase() }

            helperService.printLog(this::class.simpleName, "getAll", false)

            ResponseEntity.ok(dtos)
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
            val dto = modelMapper.map(clientService.getById(id), ClientDto::class.java)
            dto.categoryTemplate.categories = dto.categoryTemplate.categories.sortedBy { it.shortcut }.toTypedArray()

            helperService.printLog(this::class.simpleName, "getById [id=$id]", false)

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getById [id=$id] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }
}