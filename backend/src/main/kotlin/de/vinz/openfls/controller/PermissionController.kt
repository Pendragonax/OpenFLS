package de.vinz.openfls.controller

import de.vinz.openfls.dtos.PermissionDto
import de.vinz.openfls.model.Permission
import de.vinz.openfls.repositories.PermissionRepository
import de.vinz.openfls.repositories.EmployeeRepository
import de.vinz.openfls.repositories.InstitutionRepository
import de.vinz.openfls.services.HelperService
import de.vinz.openfls.services.PermissionService
import org.modelmapper.ModelMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.Exception
import javax.validation.Valid

@RestController
@RequestMapping("/permissions")
class PermissionController(
    private val repository: PermissionRepository,
    private val employeeRepository: EmployeeRepository,
    private val institutionRepository: InstitutionRepository,
    private val helperService: HelperService,
    private val modelMapper: ModelMapper)
{
    private fun convertToEntity(permissionDto: PermissionDto): Permission {
        val permission: Permission = modelMapper.map(permissionDto, Permission::class.java)

        permission.employee = employeeRepository.findById(permissionDto.employeeId).get()
        permission.institution = institutionRepository.findById(permissionDto.institutionId).get()

        return permission
    }

    @PostMapping
    fun create(@Valid @RequestBody valueDto: PermissionDto): Any {
        return try {
            val permission = repository.save(convertToEntity(valueDto))

            helperService.printLog(this::class.simpleName, "create " +
                    "[institutionId=${valueDto.institutionId}|employeeId=${valueDto.employeeId}]", false)

            ResponseEntity.ok(permission)
        } catch (ex: Exception) {
            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody valueDto: PermissionDto): Any {
        return try {
            val permission = repository.save(convertToEntity(valueDto))

            helperService.printLog(this::class.simpleName, "update " +
                    "[institutionId=${valueDto.institutionId}|employeeId=${valueDto.employeeId}]", false)

            ResponseEntity.ok(permission)
        } catch (ex: Exception) {
            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            repository.deleteById(id)

            helperService.printLog(this::class.simpleName, "delete [id=${id}]", false)

            ResponseEntity.ok()
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "delete [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("")
    fun getAll(): Any =
        ResponseEntity.ok(repository.findAll())

    @GetMapping("/combination/{employeeId}/{institutionId}")
    fun getById(@PathVariable employeeId: Long,
                @PathVariable institutionId: Long): Any =
        ResponseEntity.ok(repository.findByIds(employeeId, institutionId))

    @GetMapping("/employee/{employeeId}")
    fun getByEmployeeId(@PathVariable employeeId: Long): Any =
        ResponseEntity.ok(repository.findByEmployeeId(employeeId))

    @GetMapping("/institution/{institutionId}")
    fun getByInstitutionId(@PathVariable institutionId: Long): Any =
        ResponseEntity.ok(repository.findByInstitutionId(institutionId))
}