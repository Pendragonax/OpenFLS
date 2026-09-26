package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.contingents.ContingentRepository
import de.vinz.openfls.domains.contingents.dtos.ContingentCreateRequest
import de.vinz.openfls.domains.contingents.dtos.ContingentResponse
import de.vinz.openfls.domains.contingents.dtos.ContingentUpdateRequest
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.permissions.AccessService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ContingentService(
    private val contingentRepository: ContingentRepository,
    private val institutionService: InstitutionService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService
) {

    @Transactional
    fun create(request: ContingentCreateRequest): ContingentResponse {
        validateRange(request.start, request.end)
        val employee = getActiveEmployee(request.employeeId)
        val institution = getInstitution(request.institutionId)

        val entity = Contingent(
            start = request.start,
            end = request.end,
            weeklyServiceHours = request.weeklyServiceHours,
            employee = employee,
            institution = institution
        )

        return ContingentResponse.from(contingentRepository.save(entity))
    }

    @Transactional
    fun update(request: ContingentUpdateRequest): ContingentResponse {
        val entity = contingentRepository.findById(request.id)
            .orElseThrow { IllegalArgumentException("contingent not found") }
        validateRange(request.start, request.end)
        val employee = getActiveEmployee(request.employeeId)
        val institution = getInstitution(request.institutionId)

        entity.start = request.start
        entity.end = request.end
        entity.weeklyServiceHours = request.weeklyServiceHours
        entity.employee = employee
        entity.institution = institution

        return ContingentResponse.from(contingentRepository.save(entity))
    }

    @Transactional
    fun delete(id: Long) {
        contingentRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<ContingentResponse> {
        return contingentRepository.findAll()
            .map { ContingentResponse.from(it) }
            .sortedBy { it.start }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): ContingentResponse? {
        return contingentRepository.findById(id).orElse(null)?.let { ContingentResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return contingentRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    fun getByEmployeeId(id: Long, includeArchivedEmployees: Boolean = false): List<ContingentResponse> {
        return getAllEntitiesByEmployeeId(id, includeArchivedEmployees)
            .map { ContingentResponse.from(it) }
            .sortedBy { it.employeeId }
    }

    @Transactional(readOnly = true)
    fun getByInstitutionId(id: Long, includeArchivedEmployees: Boolean = false): List<ContingentResponse> {
        return contingentRepository.findAllByInstitutionId(id)
            .filter { includeArchivedEmployees || !(it.employee?.archived ?: true) }
            .map { ContingentResponse.from(it) }
            .sortedBy { it.institutionId }
    }

    @Transactional(readOnly = true)
    fun canModifyContingent(contingentId: Long): Boolean {
        return try {
            // ADMIN
            if (accessService.isAdmin())
                return true

            val institutionId = contingentRepository.findById(contingentId).orElse(null)?.institution?.id ?: 0

            accessService.isLeader(accessService.getId(), institutionId)
        } catch (_: Exception) {
            false
        }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByEmployeeId(id: Long, includeArchivedEmployees: Boolean = false): List<Contingent> {
        val employee = employeeService.getById(id) ?: return emptyList()
        if (employee.archived && !includeArchivedEmployees) {
            return emptyList()
        }

        return contingentRepository.findAllByEmployeeId(id)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByInstitutionAndYear(institutionId: Long, year: Int): List<Contingent> {
        return contingentRepository.findByInstitutionIdAndStartAndEnd(
            institutionId,
            LocalDate.of(year, 1, 1),
            LocalDate.of(year, 12, 31)
        )
    }

    private fun validateRange(start: LocalDate, end: LocalDate?) {
        if (end != null && start >= end) {
            throw IllegalArgumentException("end before start")
        }
    }

    private fun getActiveEmployee(employeeId: Long): Employee {
        val employee = employeeService.getById(employeeId)
            ?: throw IllegalArgumentException("employee not found")
        if (employee.archived) {
            throw IllegalArgumentException("employee is archived")
        }

        return employee
    }

    private fun getInstitution(institutionId: Long): Institution {
        return institutionService.getEntityById(institutionId)
            ?: throw IllegalArgumentException("institution not found")
    }
}
