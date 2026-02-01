package de.vinz.openfls.domains.employees.services

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanResponseDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.dtos.EmployeeDto
import de.vinz.openfls.domains.employees.dtos.UnprofessionalDto
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import de.vinz.openfls.domains.employees.entities.Unprofessional
import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.permissions.PermissionDto
import de.vinz.openfls.domains.permissions.PermissionService
import de.vinz.openfls.services.GenericService
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
        private val modelMapper: ModelMapper
) : GenericService<Employee> {

    @Transactional
    fun create(valueDto: EmployeeDto): EmployeeDto {
        val employee = modelMapper.map(valueDto, Employee::class.java).apply {
            id = null
            permissions = null
            unprofessionals = null
            contingents = null
            access = EmployeeAccess(
                id = null,
                username = valueDto.access?.username.orEmpty(),
                password = passwordEncoder.encode(valueDto.access?.username.orEmpty()),
                role = valueDto.access?.role ?: 3,
                employee = this
            )
        }
        var savedEmployee = employeeRepository.save(employee)

        savedEmployee.permissions = permissionService.convertToPermissions(valueDto.permissions, savedEmployee)
        savedEmployee.unprofessionals = unprofessionalService.convertToUnprofessionals(valueDto.unprofessionals, savedEmployee)
        savedEmployee = employeeRepository.save(savedEmployee)

        // set id to dto
        valueDto.id = savedEmployee.id!!
        valueDto.access?.id = savedEmployee.id!!
        valueDto.permissions = valueDto.permissions
                ?.filter { savedEmployee.permissions
                        ?.any { permission -> permission.id.institutionId == it.institutionId } ?: false }
                ?.map { it.apply { employeeId = savedEmployee.id!! } }
                ?.toTypedArray()
        valueDto.unprofessionals = valueDto.unprofessionals
                ?.map { it.apply { employeeId = savedEmployee.id!! } }
                ?.toTypedArray()

        return valueDto
    }

    @Transactional
    override fun create(value: Employee): Employee {
        if (value.access == null)
            throw IllegalArgumentException("employee access is null")
        if (value.access!!.password.isEmpty())
            throw IllegalArgumentException("password is empty")
        if (employeeAccessRepository.getEmployeeByUsername(value.access!!.username) != null)
            throw IllegalArgumentException("username already exists")

        val tmpPermissions = value.permissions
        val tmpAccess = value.access
        val tmpUnprofessionals = value.unprofessionals

        value.permissions = null
        value.access = null
        value.contingents = null
        value.unprofessionals = null

        // employee
        val employeeEntity = employeeRepository.save(value).apply {
            access = employeeAccessRepository.save(
                EmployeeAccess(
                    this.id,
                    tmpAccess?.username ?: "",
                    tmpAccess?.password ?: "",
                    tmpAccess?.role ?: 3,
                    this))
            permissions = savePermissions(this, tmpPermissions)
            unprofessionals = saveUnprofessionals(this, tmpUnprofessionals)
        }

        return employeeEntity
    }

    @Transactional
    fun update(id: Long, valueDto: EmployeeDto): EmployeeDto {
        val employee = getById(id) ?: throw EntityNotFoundException()

        employee.apply {
            if (valueDto.access != null) {
                access?.username = valueDto.access!!.username
                access?.role = valueDto.access!!.role
            }
            firstname = valueDto.firstName
            lastname = valueDto.lastName
            email = valueDto.email
            phonenumber = valueDto.phonenumber
        }

        if (accessService.isAdmin()) {
            employee.permissions = permissionService.convertToPermissions(valueDto.permissions, employee)
            employee.unprofessionals = unprofessionalService.convertToUnprofessionals(valueDto.unprofessionals, employee)
        }

        // update employee
        val savedEntity = update(employee)

        // permissions
        valueDto.permissions = savedEntity.permissions
                ?.map { modelMapper.map(it, PermissionDto::class.java) }
                ?.toTypedArray()
        // unprofessionals
        valueDto.unprofessionals = savedEntity.unprofessionals
                ?.map { modelMapper.map(it, UnprofessionalDto::class.java) }
                ?.toTypedArray()

        return valueDto
    }

    @Transactional
    override fun update(value: Employee): Employee {
        val tmpPermissions = value.permissions
        val tmpUnprofessionals = value.unprofessionals

        value.permissions = null
        value.access = null
        value.contingents = null
        value.unprofessionals = null

        // save employee
        val employeeEntity = employeeRepository.save(value).apply {
            access = null
            permissions = savePermissions(this, tmpPermissions)
            permissions = permissionService.getPermissionByEmployee(this.id ?: 0).toMutableSet()
            unprofessionals = saveUnprofessionals(this, tmpUnprofessionals)
        }

        return employeeEntity
    }

    @Transactional
    fun updateRole(id: Long, role: Int): EmployeeDto {
        // load employee
        val employee = employeeRepository.findById(id).get()
        employee.access?.password = "password"
        employeeAccessRepository.changeRole(id, role)

        return modelMapper.map(
                employee,
                EmployeeDto::class.java)
    }

    @Transactional
    fun resetPassword(id: Long): EmployeeDto {
        // load employee
        val tmpEmployee = employeeRepository.findById(id).get()

        // update role
        tmpEmployee.access?.password =
            passwordEncoder.encode(tmpEmployee.access?.username ?: "password").toString()

        val entity = employeeRepository.save(tmpEmployee)

        return modelMapper.map(entity, EmployeeDto::class.java)
    }

    @Transactional
    override fun delete(id: Long) {
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

    @Transactional(readOnly = true)
    fun getAllEmployeeDtos(): List<EmployeeDto> {
        return getAll()
                .sortedBy { it.lastname.lowercase() }
                .map { employee -> modelMapper.map(employee, EmployeeDto::class.java).apply {
                    access?.password = ""
                } }
    }

    @Transactional(readOnly = true)
    override fun getAll(): List<Employee> {
        return employeeRepository.findAll().map {
            it.apply {
                access?.password = ""
            }
        }.toList()
    }

    @Transactional(readOnly = true)
    fun getAllProjections(): List<EmployeeSoloProjection> {
        return employeeRepository.findAllProjectionsBy().sortedBy { it.lastname }
    }

    @Transactional(readOnly = true)
    override fun getById(id: Long): Employee? {
        return this.getById(id, false)?.apply {
            this.access?.password = ""
        }
    }

    @Transactional(readOnly = true)
    override fun existsById(id: Long): Boolean {
        return employeeRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    fun getEmployeeDtoById(id: Long, adminMode: Boolean): EmployeeDto? {
        val entity = getById(id)

        return modelMapper.map(entity, EmployeeDto::class.java)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long, adminMode: Boolean): Employee? {
        val value = employeeRepository.findById(id).orElse(null)?.apply {
            if (!adminMode) {
                this.access?.password = ""
            }
        }

        return value
    }

    private fun savePermissions(employee: Employee,
                                permissions: MutableSet<Permission>?): MutableSet<Permission> {
        return permissions
            ?.map { it.apply {
                this.employee = employee
                this.id.employeeId = employee.id } }
            ?.map { permissionService.savePermission(it) }
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
            .map { unprofessionalService.create(it) }
            .toMutableSet()
    }
}
