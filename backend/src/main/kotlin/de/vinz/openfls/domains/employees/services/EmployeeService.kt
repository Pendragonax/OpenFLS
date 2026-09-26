package de.vinz.openfls.domains.employees.services

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanResponseDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.dtos.EmployeeCreateDto
import de.vinz.openfls.domains.employees.dtos.EmployeeSoloDto
import de.vinz.openfls.domains.employees.dtos.EmployeeUpdateDto
import de.vinz.openfls.domains.employees.dtos.EmployeeWithAccess
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import de.vinz.openfls.domains.employees.entities.Unprofessional
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.permissions.PermissionService
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityNotFoundException
import org.modelmapper.ModelMapper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeService(
        private val employeeRepository: EmployeeRepository,
        private val employeeAccessRepository: EmployeeAccessRepository,
        private val permissionService: PermissionService,
        private val unprofessionalService: UnprofessionalService,
        private val assistancePlanRepository: AssistancePlanRepository,
        private val accessService: AccessService,
        private val passwordEncoder: PasswordEncoder,
        private val modelMapper: ModelMapper,
        private val entityManager: EntityManager
) {
    companion object {
        private val USERNAME_PATTERN = Regex("^[A-Za-z0-9ÄÖÜäöüß]+$")
    }

    @Transactional
    fun create(valueDto: EmployeeCreateDto): EmployeeWithAccess {
        val username = valueDto.access.username
        validateUsername(username)
        if (employeeAccessRepository.getEmployeeByUsername(username) != null)
            throw IllegalArgumentException("username already exists")

        val employee = Employee(
            firstname = valueDto.firstName,
            lastname = valueDto.lastName,
            phonenumber = valueDto.phonenumber,
            email = valueDto.email,
            description = valueDto.description,
            archived = false
        ).apply {
            access = EmployeeAccess(
                id = null,
                username = username,
                password = passwordEncoder.encode(username),
                role = valueDto.access.role,
                employee = this
            )
        }
        var savedEmployee = employeeRepository.save(employee)

        savedEmployee.permissions = permissionService.convertToPermissions(valueDto.permissions, savedEmployee)
        savedEmployee.unprofessionals = unprofessionalService.convertToUnprofessionals(valueDto.unprofessionals, savedEmployee)
        savedEmployee = employeeRepository.save(savedEmployee)

        return toEmployeeWithAccess(savedEmployee)
    }

    @Transactional
    fun update(id: Long, valueDto: EmployeeUpdateDto): EmployeeWithAccess {
        val employee = getById(id) ?: throw EntityNotFoundException()

        employee.apply {
            firstname = valueDto.firstName
            lastname = valueDto.lastName
            email = valueDto.email
            phonenumber = valueDto.phonenumber
        }

        if (accessService.isAdmin()) {
            employee.permissions = permissionService.convertToPermissions(valueDto.permissions, employee)
            employee.unprofessionals = unprofessionalService.convertToUnprofessionals(valueDto.unprofessionals, employee)
        }

        val tmpPermissions = employee.permissions
        val tmpUnprofessionals = employee.unprofessionals

        employee.permissions = null
        employee.contingents = null
        employee.unprofessionals = null

        // save employee
        val savedEntity = employeeRepository.save(employee).apply {
            permissions = savePermissions(this, tmpPermissions)
            permissions = permissionService.getEntitiesByEmployeeId(this.id ?: 0).toMutableSet()
            unprofessionals = saveUnprofessionals(this, tmpUnprofessionals)
        }

        return toEmployeeWithAccess(savedEntity)
    }

    @Transactional
    fun updateRole(id: Long, role: Int): EmployeeWithAccess {
        // load employee
        val employee = employeeRepository.findById(id).get()
        employee.access?.password = "password"
        employeeAccessRepository.changeRole(id, role)

        return toEmployeeWithAccess(employee)
    }

    @Transactional
    fun resetPassword(id: Long): EmployeeWithAccess {
        // load employee
        val tmpEmployee = employeeRepository.findById(id).get()

        // update role
        tmpEmployee.access?.password =
            passwordEncoder.encode(tmpEmployee.access?.username ?: "password").toString()

        val entity = employeeRepository.save(tmpEmployee)

        return toEmployeeWithAccess(entity)
    }

    @Transactional
    fun delete(id: Long) {
        employeeAccessRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAssistancePlanAsFavorites(employeeId: Long): List<AssistancePlanResponseDto> {
        val employee = employeeRepository.findById(employeeId)
                .orElseThrow { EntityNotFoundException() }

        return employee.assistancePlanFavorites
                .map { modelMapper.map(it, AssistancePlanResponseDto::class.java) }
                .sortedByDescending { it.end }
    }

    @Transactional
    fun addAssistancePlanAsFavorite(assistancePlanId: Long, employeeId: Long) {
        val employee = employeeRepository.findById(employeeId)
                .orElseThrow { EntityNotFoundException() }
        val assistancePlan = assistancePlanRepository.findById(assistancePlanId)
                .orElseThrow { EntityNotFoundException() }

        if (employee.assistancePlanFavorites.none { it.id == assistancePlanId }) {
            employee.assistancePlanFavorites.add(assistancePlan)
            employeeRepository.save(employee)
        }
    }

    @Transactional
    fun deleteAssistancePlanAsFavorite(assistancePlanId: Long, employeeId: Long) {
        val employee = employeeRepository.findById(employeeId)
                .orElseThrow { EntityNotFoundException() }

        if (employee.assistancePlanFavorites.any { it.id == assistancePlanId }) {
            employee.assistancePlanFavorites.removeIf { it.id == assistancePlanId }
            employeeRepository.save(employee)
        }
    }

    @Transactional
    fun addClientAsFavorite(clientId: Long, employeeId: Long) {
        val employee = employeeRepository.findById(employeeId)
                .orElseThrow { EntityNotFoundException() }

        if (employee.clientFavorites.none { it.id == clientId }) {
            employee.clientFavorites.add(entityManager.getReference(Client::class.java, clientId))
            employeeRepository.save(employee)
        }
    }

    @Transactional
    fun deleteClientAsFavorite(clientId: Long, employeeId: Long) {
        val employee = employeeRepository.findById(employeeId)
                .orElseThrow { EntityNotFoundException() }

        if (employee.clientFavorites.any { it.id == clientId }) {
            employee.clientFavorites.removeIf { it.id == clientId }
            employeeRepository.save(employee)
        }
    }

    /**
     * Removes a client from the favourites of every employee. Used when the client is
     * archived or deleted, so nobody keeps a dead entry on their home view.
     */
    @Transactional
    fun deleteClientFavoritesByClientId(clientId: Long): Int {
        var removedFavorites = 0
        employeeRepository.findAll().forEach { employee ->
            if (employee.clientFavorites.removeIf { it.id == clientId }) {
                removedFavorites++
                employeeRepository.save(employee)
            }
        }

        return removedFavorites
    }

    @Transactional
    fun deleteAssistancePlanFavoritesByClientId(clientId: Long): Int {
        val assistancePlanIds = assistancePlanRepository.findByClientId(clientId)
            .map { it.id }
            .toSet()

        if (assistancePlanIds.isEmpty()) {
            return 0
        }

        var removedFavorites = 0
        employeeRepository.findAll().forEach { employee ->
            val before = employee.assistancePlanFavorites.size
            employee.assistancePlanFavorites.removeIf { it.id in assistancePlanIds }
            val removedForEmployee = before - employee.assistancePlanFavorites.size
            if (removedForEmployee > 0) {
                removedFavorites += removedForEmployee
                employeeRepository.save(employee)
            }
        }

        return removedFavorites
    }

    @Transactional(readOnly = true)
    fun getAllEmployeeDtos(includeArchived: Boolean = false): List<EmployeeWithAccess> {
        return getAll()
                .filter { includeArchived || !it.archived }
                .sortedBy { it.lastname.lowercase() }
                .map { employee -> toEmployeeWithAccess(employee) }
    }

    @Transactional(readOnly = true)
    fun getAll(): List<Employee> {
        return employeeRepository.findAll().map {
            it.apply {
                access?.password = ""
            }
        }.toList()
    }

    @Transactional(readOnly = true)
    fun getAllSoloDtos(): List<EmployeeSoloDto> {
        return employeeRepository.findAllProjectionsBy()
            .filter { !it.archived }
            .sortedBy { it.lastname }
            .map { EmployeeSoloDto.of(it) }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): Employee? {
        return this.getById(id, false)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return employeeRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    fun getEmployeeDtoById(id: Long, adminMode: Boolean): EmployeeWithAccess? {
        val entity = getById(id)

        if (entity != null && (!entity.archived || adminMode)) {
            return toEmployeeWithAccess(entity)
        }

        return null
    }

    @Transactional(readOnly = true)
    fun getById(id: Long, adminMode: Boolean): Employee? {
        return employeeRepository.findById(id).orElse(null)
    }

    private fun toEmployeeWithAccess(entity: Employee): EmployeeWithAccess {
        return modelMapper.map(entity, EmployeeWithAccess::class.java).apply {
            access?.password = ""
        }
    }

    private fun savePermissions(employee: Employee,
                                permissions: MutableSet<Permission>?): MutableSet<Permission> {
        return permissions
            ?.map { it.apply {
                this.employee = employee
                this.id.employeeId = employee.id } }
            ?.map { permissionService.savePermissionEntity(it) }
            ?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveUnprofessionals(employee: Employee,
                                    unprofessionals: MutableSet<Unprofessional>?): MutableSet<Unprofessional> {
        if (unprofessionals == null)
            return mutableSetOf()

        // delete unprofessionals
        unprofessionalService
            .getByEmployeeId(employee.id ?: 0)
            .filter { !unprofessionals.any { value ->
                    value.id?.employeeId == it.id?.employeeId && value.id?.sponsorId == it.id?.sponsorId} }
            .forEach { unprofessionalService
                .deleteByEmployeeIdSponsorId(it.id?.employeeId ?: 0, it.id?.sponsorId ?: 0) }

        return unprofessionals
            .map { it.apply {
                this.employee = employee
                this.id?.employeeId = employee.id
                this.id?.sponsorId = this.sponsor?.id
            } }
            .map { unprofessionalService.createEntity(it) }
            .toMutableSet()
    }

    private fun validateUsername(username: String) {
        if (username.isEmpty())
            throw IllegalArgumentException("username is empty")
        if (username.length < 6)
            throw IllegalArgumentException("username is too short")
        if (!USERNAME_PATTERN.matches(username))
            throw IllegalArgumentException("username contains invalid characters")
    }
}
