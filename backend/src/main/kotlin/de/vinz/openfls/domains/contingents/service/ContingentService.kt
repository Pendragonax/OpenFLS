package de.vinz.openfls.domains.contingents.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.contingents.entity.Contingent
import de.vinz.openfls.domains.contingents.repository.ContingentRepository
import de.vinz.openfls.domains.contingents.dto.ContingentCreateRequest
import de.vinz.openfls.domains.contingents.dto.ContingentCreateResult
import de.vinz.openfls.domains.contingents.dto.ContingentDeleteResult
import de.vinz.openfls.domains.contingents.dto.ContingentResponse
import de.vinz.openfls.domains.contingents.dto.ContingentUpdateRequest
import de.vinz.openfls.domains.contingents.dto.ContingentUpdateResult
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.domains.permissions.service.AccessService
import org.springframework.data.repository.findByIdOrNull
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
    fun create(request: ContingentCreateRequest): ContingentCreateResult {
        if (request.end != null && request.start >= request.end) {
            return ContingentCreateResult.InvalidRange("end before start")
        }
        val employee = employeeService.getEntityById(request.employeeId)
            ?: return ContingentCreateResult.EmployeeNotFound("employee not found")
        if (employee.archived) {
            return ContingentCreateResult.EmployeeArchived("employee is archived")
        }
        val institution = institutionService.getEntityById(request.institutionId)
            ?: return ContingentCreateResult.InstitutionNotFound("institution not found")

        val entity = Contingent(
            start = request.start,
            end = request.end,
            weeklyServiceHours = request.weeklyServiceHours,
            employee = employee,
            institution = institution
        )

        return ContingentCreateResult.Success(ContingentResponse.from(contingentRepository.save(entity)))
    }

    @Transactional
    fun update(request: ContingentUpdateRequest): ContingentUpdateResult {
        val entity = contingentRepository.findByIdOrNull(request.id)
            ?: return ContingentUpdateResult.NotFound
        if (request.end != null && request.start >= request.end) {
            return ContingentUpdateResult.InvalidRange("end before start")
        }
        val employee = employeeService.getEntityById(request.employeeId)
            ?: return ContingentUpdateResult.EmployeeNotFound("employee not found")
        if (employee.archived) {
            return ContingentUpdateResult.EmployeeArchived("employee is archived")
        }
        val institution = institutionService.getEntityById(request.institutionId)
            ?: return ContingentUpdateResult.InstitutionNotFound("institution not found")

        entity.start = request.start
        entity.end = request.end
        entity.weeklyServiceHours = request.weeklyServiceHours
        entity.employee = employee
        entity.institution = institution

        return ContingentUpdateResult.Success(ContingentResponse.from(contingentRepository.save(entity)))
    }

    @Transactional
    fun delete(id: Long): ContingentDeleteResult {
        val entity = contingentRepository.findByIdOrNull(id)
            ?: return ContingentDeleteResult.NotFound
        contingentRepository.deleteById(id)
        return ContingentDeleteResult.Success(ContingentResponse.from(entity))
    }

    @Transactional(readOnly = true)
    fun getAll(): List<ContingentResponse> {
        return contingentRepository.findAll()
            .map { ContingentResponse.from(it) }
            .sortedBy { it.start }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): ContingentResponse? {
        return contingentRepository.findByIdOrNull(id)?.let { ContingentResponse.from(it) }
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

            val institutionId = contingentRepository.findByIdOrNull(contingentId)?.institution?.id ?: 0

            accessService.isLeader(accessService.getId(), institutionId)
        } catch (_: Exception) {
            false
        }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByEmployeeId(id: Long, includeArchivedEmployees: Boolean = false): List<Contingent> {
        val employee = employeeService.getEntityById(id) ?: return emptyList()
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
}
