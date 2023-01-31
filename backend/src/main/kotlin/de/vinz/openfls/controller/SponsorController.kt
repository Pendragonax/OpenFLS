package de.vinz.openfls.controller

import de.vinz.openfls.dtos.SponsorDto
import de.vinz.openfls.model.Sponsor
import de.vinz.openfls.services.HelperService
import de.vinz.openfls.services.SponsorService
import org.modelmapper.ModelMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import javax.validation.Valid

@RestController
@RequestMapping("/sponsors")
class SponsorController(
    val sponsorService: SponsorService,
    val helperService: HelperService,
    val modelMapper: ModelMapper
) {

    @PostMapping("")
    fun create(@Valid @RequestBody valueDto: SponsorDto): Any {
        return try {
            val entity = sponsorService.create(modelMapper.map(valueDto, Sponsor::class.java))

            helperService.printLog(this::class.simpleName, "create [id=${entity.id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, SponsorDto::class.java))
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
               @Valid @RequestBody valueDto: SponsorDto): Any {
        return try {
            if (id != valueDto.id)
                throw IllegalArgumentException("path id and dto id are not the same")
            if (!sponsorService.existsById(id))
                throw IllegalArgumentException("sponsor not found")

            val entity = sponsorService.update(modelMapper.map(valueDto, Sponsor::class.java))

            helperService.printLog(this::class.simpleName, "update [id=${id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, SponsorDto::class.java))
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
            if (!sponsorService.existsById(id))
                throw IllegalArgumentException("sponsor not found")

            val entity = sponsorService.getById(id)
            sponsorService.delete(id)

            helperService.printLog(this::class.simpleName, "delete [id=${id}]", false)

            ResponseEntity.ok(modelMapper.map(entity, SponsorDto::class.java))
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
            val entities = sponsorService.getAll()
                .map { modelMapper.map(it, SponsorDto::class.java) }
                .sortedBy { it.name.lowercase() }

            helperService.printLog(this::class.simpleName, "getAll", false)

            ResponseEntity.ok(entities)
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
            val entity = modelMapper.map(sponsorService.getById(id), SponsorDto::class.java)

            helperService.printLog(this::class.simpleName, "getById [id=$id]", false)

            ResponseEntity.ok(entity)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getById [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }
}