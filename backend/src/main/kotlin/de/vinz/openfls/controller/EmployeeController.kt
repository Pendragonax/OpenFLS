package de.vinz.openfls.controller

import de.vinz.openfls.dtos.EmployeeDto
import de.vinz.openfls.dtos.PermissionDto
import de.vinz.openfls.dtos.UnprofessionalDto
import de.vinz.openfls.model.Employee
import de.vinz.openfls.model.EmployeeAccess
import de.vinz.openfls.model.Permission
import de.vinz.openfls.model.Unprofessional
import de.vinz.openfls.services.*
import org.modelmapper.ModelMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import kotlin.Exception

@RestController
@RequestMapping("/employees")
class EmployeeController(
    private val employeeService: EmployeeService,
    private val modelMapper: ModelMapper,
    private val passwordEncoder: PasswordEncoder,
    private val accessService: AccessService,
    private val helperService: HelperService
) {
    @PostMapping
    fun create(@Valid @RequestBody valueDto: EmployeeDto): Any {
        return try {
            // convert employee
            val entity = modelMapper.map(valueDto, Employee::class.java)
            // convert access
            entity.access = modelMapper.map(valueDto.access, EmployeeAccess::class.java)?.apply {
                password = if (username.isNotEmpty()) passwordEncoder.encode(username).toString() else ""
            }

            entity.permissions = convertToPermissions(valueDto.permissions, -1)
            entity.unprofessionals = convertToUnprofessionals(valueDto.unprofessionals, -1)

            val savedEntity = employeeService.create(entity)

            // set id to dto
            valueDto.id = savedEntity.id!!
            valueDto.access?.id = savedEntity.id!!

            valueDto.permissions = valueDto.permissions
                ?.filter { savedEntity.permissions
                    ?.any { permission -> permission.id.institutionId == it.institutionId } ?: false }
                ?.map { it.apply { employeeId = savedEntity.id!! } }
                ?.toTypedArray()
            valueDto.unprofessionals = valueDto.unprofessionals
                ?.map { it.apply { employeeId = savedEntity.id!! } }
                ?.toTypedArray()

            helperService.printLog(this::class.simpleName, "create [id=${savedEntity.id}]", false)

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "create - ${ex.message}", true)

            ResponseEntity(
                ex.localizedMessage,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}/{role}")
    fun updateRole(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                   @PathVariable id: Long,
                   @PathVariable role: Int): Any {
        return try {
            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to change the role")

            // update role
            val dto = modelMapper.map(
                employeeService.updateRole(id, role).apply {
                    access?.password = "" },
                EmployeeDto::class.java)

            helperService.printLog(this::class.simpleName, "updateRole [id=${id}]", false)

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "updateRole [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("reset_password/{id}")
    fun resetPassword(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                      @PathVariable id: Long): Any {
        return try {
            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to reset passwords")
            if (!employeeService.existsById(id))
                throw IllegalArgumentException("employee not found")

            val dto = modelMapper.map(employeeService.resetPassword(id), EmployeeDto::class.java)
            println("${dto.id}")

            helperService.printLog(this::class.simpleName, "resetPassword [id=${id}]", false)

            return ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "resetPassword [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("{id}")
    fun update(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long,
               @Valid @RequestBody valueDto: EmployeeDto): Any {
        return try {
            if (!accessService.canModifyEmployee(token, valueDto.id))
                throw IllegalArgumentException("no permission to update this employee")
            if (id != valueDto.id)
                throw IllegalArgumentException("path id and dto id are not the same")
            if (!employeeService.existsById(id))
                throw IllegalArgumentException("employee not found")

            // convert employee
            val entity = modelMapper.map(valueDto, Employee::class.java)

            if (accessService.isAdmin(token)) {
                entity.permissions = convertToPermissions(valueDto.permissions, id)
                entity.unprofessionals = convertToUnprofessionals(valueDto.unprofessionals, id)
            } else {
                entity.permissions = mutableSetOf()
                entity.unprofessionals = mutableSetOf()
            }

            // update employee
            val savedEntity = employeeService.update(entity)

            // permissions
            valueDto.permissions = savedEntity.permissions
                ?.map { modelMapper.map(it, PermissionDto::class.java) }
                ?.toTypedArray()
            // unprofessionals
            valueDto.unprofessionals = savedEntity.unprofessionals
                ?.map { modelMapper.map(it, UnprofessionalDto::class.java) }
                ?.toTypedArray()

            helperService.printLog(this::class.simpleName, "update [id=${id}]", false)

            ResponseEntity.ok(valueDto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "update [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("{id}")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long): Any {
        return try {
            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("no permission to delete this employee")
            if (!employeeService.existsById(id))
                throw IllegalArgumentException("employee not found")

            val dto = modelMapper.map(employeeService.getById(id, true), EmployeeDto::class.java)

            employeeService.delete(id).toString()

            helperService.printLog(this::class.simpleName, "delete [id=${id}]", false)

            ResponseEntity.ok(dto)
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
            val dtos = employeeService
                .getAll()
                .sortedBy { it.lastname }
                .map { employee -> modelMapper.map(employee, EmployeeDto::class.java).apply {
                    access?.password = ""
                } }

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
    fun getById(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String, @PathVariable id: Long): Any  {
        return try {
            val dto = modelMapper.map(
                employeeService.getById(
                    id,
                    accessService.isAdmin(token)),
                EmployeeDto::class.java)

            helperService.printLog(this::class.simpleName, "getById [id=${id}]", false)

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "getById [id=${id}] - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST)
        }
    }

    private fun convertToPermissions(permissionDtos: Array<PermissionDto>?, employeeId: Long): MutableSet<Permission> {
        return permissionDtos
            ?.map {
                modelMapper
                    .map(it, Permission::class.java)
                    .apply { it.employeeId = employeeId } }
            ?.toMutableSet() ?: mutableSetOf()
    }

    private fun convertToUnprofessionals(dtos: Array<UnprofessionalDto>?, employeeId: Long): MutableSet<Unprofessional> {
        return dtos
            ?.map {
                modelMapper
                    .map(it, Unprofessional::class.java)
                    .apply { it.employeeId = employeeId } }
            ?.toMutableSet() ?: mutableSetOf()
    }
}