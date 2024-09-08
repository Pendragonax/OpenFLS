package de.vinz.openfls.domains.employees.services

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanResponseDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.employees.entities.Unprofessional
import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.dtos.EmployeeDto
import de.vinz.openfls.domains.permissions.PermissionDto
import de.vinz.openfls.domains.employees.dtos.UnprofessionalDto
import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.GenericService
import de.vinz.openfls.domains.permissions.PermissionService
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

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
        // convert employee
        val entity = modelMapper.map(valueDto, Employee::class.java)
        // convert access
        entity.access = modelMapper.map(valueDto.access, EmployeeAccess::class.java)?.apply {
            password = if (username.isNotEmpty()) passwordEncoder.encode(username).toString() else ""
        }

        entity.permissions = permissionService.convertToPermissions(valueDto.permissions, -1)
        entity.unprofessionals = unprofessionalService.convertToUnprofessionals(valueDto.unprofessionals, -1)

        val savedEntity = create(entity)

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
        // convert employee
        val entity = modelMapper.map(valueDto, Employee::class.java)

        if (accessService.isAdmin()) {
            entity.permissions = permissionService.convertToPermissions(valueDto.permissions, id)
            entity.unprofessionals = unprofessionalService.convertToUnprofessionals(valueDto.unprofessionals, id)
        } else {
            entity.permissions = mutableSetOf()
            entity.unprofessionals = mutableSetOf()
        }

        // update employee
        val savedEntity = update(entity)

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
        employee.access?.role = role

        employeeRepository.save(employee)

        employee.access?.password = ""

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

    fun getAllEmployeeDtos(): List<EmployeeDto> {
        return getAll()
                .sortedBy { it.lastname.lowercase() }
                .map { employee -> modelMapper.map(employee, EmployeeDto::class.java).apply {
                    access?.password = ""
                } }
    }

    override fun getAll(): List<Employee> {
        return employeeRepository.findAll().map {
            it.apply {
                access?.password = ""
            }
        }.toList()
    }

    fun getAllProjections(): List<EmployeeSoloProjection> {
        return employeeRepository.findAllProjectionsBy();
    }

    override fun getById(id: Long): Employee? {
        return this.getById(id, false)?.apply {
            this.access?.password = ""
        }
    }

    override fun existsById(id: Long): Boolean {
        return employeeRepository.existsById(id)
    }

    fun getEmployeeDtoById(id: Long, adminMode: Boolean): EmployeeDto? {
        val entity = getById(id)

        return modelMapper.map(entity, EmployeeDto::class.java)
    }

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