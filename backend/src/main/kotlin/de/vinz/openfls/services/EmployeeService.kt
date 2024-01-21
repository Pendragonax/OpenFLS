package de.vinz.openfls.services

import de.vinz.openfls.dtos.AssistancePlanResponseDto
import de.vinz.openfls.dtos.PasswordDto
import de.vinz.openfls.entities.*
import de.vinz.openfls.repositories.*
import org.modelmapper.ModelMapper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.persistence.EntityNotFoundException
import javax.transaction.Transactional
import kotlin.IllegalArgumentException

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
    private val employeeAccessRepository: EmployeeAccessRepository,
    private val clientRepository: ClientRepository,
    private val sponsorRepository: SponsorRepository,
    private val permissionServiceImpl: PermissionService,
    private val unprofessionalService: UnprofessionalService,
    private val assistancePlanRepository: AssistancePlanRepository,
    private val institutionRepository: InstitutionRepository,
    private val passwordEncoder: PasswordEncoder,
    private val modelMapper: ModelMapper
) : GenericService<Employee> {

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
            permissions = permissionServiceImpl.getPermissionByEmployee(this.id ?: 0).toMutableSet()
            unprofessionals = saveUnprofessionals(this, tmpUnprofessionals)
        }

        return employeeEntity
    }

    @Transactional
    fun updateRole(id: Long, role: Int): Employee {
        // load employee
        val tmpEmployee = employeeRepository.findById(id).get()

        // update role
        tmpEmployee.access?.role = role

        return employeeRepository.save(tmpEmployee)
    }

    @Transactional
    fun resetPassword(id: Long): Employee {
        // load employee
        val tmpEmployee = employeeRepository.findById(id).get()

        // update role
        tmpEmployee.access?.password =
            passwordEncoder.encode(tmpEmployee.access?.username ?: "password").toString()

        return employeeRepository.save(tmpEmployee)
    }

    @Transactional
    override fun delete(id: Long) {
        employeeAccessRepository.deleteById(id)
    }

    @Transactional
    fun changePassword(id: Long, passwordDto: PasswordDto) {
        if (passwordDto.oldPassword.isEmpty())
            throw IllegalArgumentException("old password is empty")
        if (passwordDto.newPassword.isEmpty())
            throw IllegalArgumentException("new password is empty")

        employeeAccessRepository.findById(id).orElse(null)?.also {
            // old password is correct?
            if (!passwordEncoder.matches(passwordDto.oldPassword, it.password))
                throw IllegalArgumentException("old password is wrong")

            employeeAccessRepository.changePassword(id, passwordDto.newPassword)
        } ?: throw IllegalArgumentException("employee doesnt exists")
    }

    @Transactional
    fun changeRole(id: Long, role: Int) {
        employeeAccessRepository.findById(id).orElse(null)?.also {
            employeeAccessRepository.changeRole(id, role)
        } ?: throw IllegalArgumentException("employee doesnt exists")
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

    override fun getAll(): List<Employee> {
        return employeeRepository.findAll().map {
            it.apply {
                access?.password = ""
            }
        }.toList()
    }

    override fun getById(id: Long): Employee? {
        return this.getById(id, false)?.apply {
            this.access?.password = ""
        }
    }

    override fun existsById(id: Long): Boolean {
        return employeeRepository.existsById(id)
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
            ?.map { permissionServiceImpl.savePermission(it) }
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
                    value.id.employeeId == it.id.employeeId && value.id.sponsorId == it.id.sponsorId} }
            .forEach { unprofessionalService
                .deleteByEmployeeIdSponsorId(it.id.employeeId ?: 0, it.id.sponsorId ?: 0) }

        return unprofessionals
            .map { it.apply {
                this.employee = employee
                this.id.employeeId = employee.id
                this.id.sponsorId = this.sponsor?.id
            } }
            .map { unprofessionalService.create(it) }
            .toMutableSet()
    }
}