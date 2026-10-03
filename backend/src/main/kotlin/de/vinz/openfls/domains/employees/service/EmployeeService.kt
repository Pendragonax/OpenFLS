package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.authentication.UserRole
import de.vinz.openfls.domains.employees.dto.EmployeeCreateRequest
import de.vinz.openfls.domains.employees.dto.EmployeeCreateResult
import de.vinz.openfls.domains.employees.dto.EmployeeNameDto
import de.vinz.openfls.domains.employees.dto.EmployeePasswordResetResult
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateRoleResult
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateRequest
import de.vinz.openfls.domains.employees.dto.EmployeeUpdateResult
import de.vinz.openfls.domains.employees.dto.EmployeeDetailResponse
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.entity.EmployeeAccess
import de.vinz.openfls.domains.employees.entity.Unprofessional
import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import de.vinz.openfls.domains.permissions.entity.Permission
import de.vinz.openfls.domains.permissions.service.PermissionService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeService(
        private val employeeRepository: EmployeeRepository,
        private val employeeAccessService: EmployeeAccessService,
        private val permissionService: PermissionService,
        private val unprofessionalService: UnprofessionalService,
        private val passwordEncoder: PasswordEncoder
) {
    companion object {
        private val USERNAME_PATTERN = Regex("^[A-Za-z0-9ÄÖÜäöüß]+$")
    }

    @Transactional
    fun create(request: EmployeeCreateRequest): EmployeeCreateResult {
        val username = request.access.username
        getUsernameViolation(username)?.let { return EmployeeCreateResult.InvalidUsername(it) }
        if (!isValidRole(request.access.role))
            return EmployeeCreateResult.InvalidRole
        if (employeeAccessService.existsByUsername(username))
            return EmployeeCreateResult.UsernameTaken

        val employee = Employee(
            firstname = request.firstName,
            lastname = request.lastName,
            phonenumber = request.phonenumber,
            email = request.email,
            description = request.description,
            archived = false
        ).apply {
            access = EmployeeAccess(
                id = null,
                username = username,
                password = passwordEncoder.encode(username).orEmpty(),
                role = request.access.role,
                employee = this
            )
        }
        val unprofessionals = unprofessionalService.buildEntitiesFromRequests(request.unprofessionals, employee)
            ?: return EmployeeCreateResult.SponsorNotFound

        var savedEmployee = employeeRepository.save(employee)

        savedEmployee.permissions = permissionService.convertToPermissions(request.permissions, savedEmployee)
        unprofessionals.forEach { it.id?.employeeId = savedEmployee.id }
        savedEmployee.unprofessionals = unprofessionals
        savedEmployee = employeeRepository.save(savedEmployee)

        return EmployeeCreateResult.Success(EmployeeDetailResponse.from(savedEmployee))
    }

    @Transactional
    fun update(id: Long, request: EmployeeUpdateRequest): EmployeeUpdateResult {
        val employee = employeeRepository.findByIdOrNull(id) ?: return EmployeeUpdateResult.NotFound
        val newUnprofessionals = unprofessionalService.buildEntitiesFromRequests(request.unprofessionals, employee)
            ?: return EmployeeUpdateResult.SponsorNotFound

        employee.apply {
            firstname = request.firstName
            lastname = request.lastName
            email = request.email
            phonenumber = request.phonenumber
            description = request.description
        }

        val newPermissions = permissionService.convertToPermissions(request.permissions, employee)

        // permissions and unprofessionals are persisted on their own, so the employee is saved without them
        employee.permissions = null
        employee.contingents = null
        employee.unprofessionals = null

        val savedEmployee = employeeRepository.save(employee).apply {
            permissions = savePermissions(this, newPermissions)
            permissions = permissionService.getAllEntitiesByEmployeeId(this.id ?: 0).toMutableSet()
            unprofessionals = saveUnprofessionals(this, newUnprofessionals)
        }

        return EmployeeUpdateResult.Success(EmployeeDetailResponse.from(savedEmployee))
    }

    @Transactional
    fun updateRole(id: Long, role: Int): EmployeeUpdateRoleResult {
        val employee = employeeRepository.findByIdOrNull(id) ?: return EmployeeUpdateRoleResult.NotFound
        if (!isValidRole(role))
            return EmployeeUpdateRoleResult.InvalidRole

        employee.access?.role = role

        return EmployeeUpdateRoleResult.Success(EmployeeDetailResponse.from(employee))
    }

    @Transactional
    fun resetPassword(id: Long): EmployeePasswordResetResult {
        val employee = employeeRepository.findByIdOrNull(id) ?: return EmployeePasswordResetResult.NotFound

        employee.access?.let { it.password = passwordEncoder.encode(it.username).orEmpty() }

        return EmployeePasswordResetResult.Success(EmployeeDetailResponse.from(employee))
    }

    @Transactional
    fun deleteById(id: Long) {
        employeeRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAllEmployeeDetails(includeArchived: Boolean): List<EmployeeDetailResponse> {
        return employeeRepository.findAll()
            .filter { includeArchived || !it.archived }
            .sortedBy { it.lastname.lowercase() }
            .map { EmployeeDetailResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getEmployeeDetailById(id: Long, includeArchived: Boolean): EmployeeDetailResponse? {
        val employee = employeeRepository.findByIdOrNull(id) ?: return null

        return if (includeArchived || !employee.archived) EmployeeDetailResponse.from(employee) else null
    }

    @Transactional(readOnly = true)
    fun getEmployeeNameById(id: Long, includeArchived: Boolean): EmployeeNameDto? {
        val employee = employeeRepository.findByIdOrNull(id) ?: return null

        return if (includeArchived || !employee.archived) EmployeeNameDto.from(employee) else null
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Employee? {
        return employeeRepository.findByIdOrNull(id)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntities(): List<Employee> {
        return employeeRepository.findAll().toList()
    }

    @InternalEntityApi
    @Transactional
    fun saveEntity(employee: Employee): Employee {
        return employeeRepository.save(employee)
    }

    private fun savePermissions(employee: Employee,
                                permissions: MutableSet<Permission>): MutableSet<Permission> {
        return permissions
            .map { it.apply {
                this.employee = employee
                this.id.employeeId = employee.id } }
            .map { permissionService.savePermissionEntity(it) }
            .toMutableSet()
    }

    private fun saveUnprofessionals(employee: Employee,
                                    unprofessionals: MutableSet<Unprofessional>): MutableSet<Unprofessional> {
        val employeeId = employee.id ?: 0

        unprofessionalService
            .getAllEntitiesByEmployeeId(employeeId)
            .filter { existing -> unprofessionals.none { it.id?.sponsorId == existing.id?.sponsorId } }
            .forEach { unprofessionalService.deleteByEmployeeIdAndSponsorId(employeeId, it.id?.sponsorId ?: 0) }

        return unprofessionals
            .map { unprofessionalService.createEntity(it) }
            .toMutableSet()
    }

    private fun isValidRole(role: Int): Boolean {
        return UserRole.entries.any { it.id == role }
    }

    private fun getUsernameViolation(username: String): String? {
        if (username.isEmpty())
            return "username is empty"
        if (username.length < 6)
            return "username is too short"
        if (!USERNAME_PATTERN.matches(username))
            return "username contains invalid characters"

        return null
    }
}
