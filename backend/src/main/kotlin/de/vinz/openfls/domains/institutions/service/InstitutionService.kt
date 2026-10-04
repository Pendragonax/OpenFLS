package de.vinz.openfls.domains.institutions.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.permissions.entity.PermissionKey
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.institutions.dto.InstitutionCreateRequest
import de.vinz.openfls.domains.institutions.dto.InstitutionCreateResult
import de.vinz.openfls.domains.institutions.dto.InstitutionDeleteResult
import de.vinz.openfls.domains.institutions.dto.InstitutionPermissionRequest
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse
import de.vinz.openfls.domains.institutions.dto.InstitutionUpdateRequest
import de.vinz.openfls.domains.institutions.dto.InstitutionUpdateResult
import de.vinz.openfls.domains.institutions.dto.InstitutionWithPermissionsResponse
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.permissions.entity.Permission
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InstitutionService(
    private val institutionRepository: InstitutionRepository,
    private val institutionLookupService: InstitutionLookupService,
    private val employeeService: EmployeeService
) {

    @Transactional
    fun create(request: InstitutionCreateRequest): InstitutionCreateResult {
        val permissionRequests = request.permissions.distinctBy { it.employeeId }
        val employees = loadEmployees(permissionRequests.map { it.employeeId })
            ?: return InstitutionCreateResult.EmployeeNotFound

        val institution = institutionRepository.save(
            Institution(name = request.name, email = request.email, phonenumber = request.phonenumber)
        )
        permissionRequests.forEach {
            institution.permissions.add(buildPermission(institution, employees.getValue(it.employeeId), it))
        }

        return InstitutionCreateResult.Success(
            InstitutionWithPermissionsResponse.from(institutionRepository.save(institution))
        )
    }

    @Transactional
    fun update(request: InstitutionUpdateRequest): InstitutionUpdateResult {
        val institution = institutionRepository.findByIdOrNull(request.id)
            ?: return InstitutionUpdateResult.NotFound

        val requestedPermissions = request.permissions.associateBy { it.employeeId }
        val existingEmployeeIds = institution.permissions
            .filter { it.id.employeeId in requestedPermissions.keys }
            .mapNotNull { it.id.employeeId }
            .toSet()
        val newPermissionRequests = requestedPermissions.values.filter { it.employeeId !in existingEmployeeIds }
        val newEmployees = loadEmployees(newPermissionRequests.map { it.employeeId })
            ?: return InstitutionUpdateResult.EmployeeNotFound

        institution.name = request.name
        institution.email = request.email
        institution.phonenumber = request.phonenumber

        institution.permissions.removeIf { it.id.employeeId !in requestedPermissions.keys }
        institution.permissions.forEach { permission ->
            requestedPermissions[permission.id.employeeId]?.let { applyFlags(permission, it) }
        }
        newPermissionRequests.forEach {
            institution.permissions.add(buildPermission(institution, newEmployees.getValue(it.employeeId), it))
        }

        return InstitutionUpdateResult.Success(
            InstitutionWithPermissionsResponse.from(institutionRepository.save(institution))
        )
    }

    @Transactional
    fun delete(id: Long): InstitutionDeleteResult {
        val entity = institutionRepository.findByIdOrNull(id)
            ?: return InstitutionDeleteResult.NotFound
        val response = InstitutionWithPermissionsResponse.from(entity)
        institutionRepository.deleteById(id)
        return InstitutionDeleteResult.Success(response)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<InstitutionResponse> {
        return institutionRepository.findAll()
            .map { InstitutionResponse.from(it) }
            .sortedBy { it.name }
    }

    @Transactional(readOnly = true)
    fun getAllWithPermissions(): List<InstitutionWithPermissionsResponse> {
        return institutionRepository.findAll()
            .map { InstitutionWithPermissionsResponse.from(it) }
            .sortedBy { it.name }
    }

    @Transactional(readOnly = true)
    fun getWithPermissionsById(id: Long): InstitutionWithPermissionsResponse? {
        return institutionRepository.findByIdOrNull(id)?.let { InstitutionWithPermissionsResponse.from(it) }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Institution? {
        return institutionLookupService.getEntityById(id)
    }

    private fun loadEmployees(employeeIds: List<Long>): Map<Long, Employee>? {
        return employeeIds.distinct().associateWith { employeeService.getEntityById(it) ?: return null }
    }

    private fun buildPermission(institution: Institution, employee: Employee, request: InstitutionPermissionRequest): Permission {
        val permission = Permission(
            id = PermissionKey(employeeId = request.employeeId, institutionId = institution.id),
            employee = employee,
            institution = institution
        )
        applyFlags(permission, request)
        return permission
    }

    private fun applyFlags(permission: Permission, request: InstitutionPermissionRequest) {
        permission.readEntries = request.readEntries
        permission.writeEntries = request.writeEntries
        permission.changeInstitution = request.changeInstitution
        permission.affiliated = request.affiliated
    }
}
