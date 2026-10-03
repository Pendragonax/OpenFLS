package de.vinz.openfls.domains.institutions.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.permissions.entity.PermissionKey
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.institutions.dto.InstitutionCreateRequest
import de.vinz.openfls.domains.institutions.dto.InstitutionDeleteResult
import de.vinz.openfls.domains.institutions.dto.InstitutionPermissionRequest
import de.vinz.openfls.domains.institutions.dto.InstitutionResponse
import de.vinz.openfls.domains.institutions.dto.InstitutionUpdateRequest
import de.vinz.openfls.domains.institutions.dto.InstitutionUpdateResult
import de.vinz.openfls.domains.institutions.dto.InstitutionWithPermissionsResponse
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.permissions.entity.Permission
import de.vinz.openfls.domains.permissions.service.PermissionService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InstitutionService(
    private val institutionRepository: InstitutionRepository,
    private val permissionService: PermissionService,
    private val employeeService: EmployeeService
) {

    @Transactional
    fun create(request: InstitutionCreateRequest): InstitutionWithPermissionsResponse {
        val institution = institutionRepository.save(
            Institution(name = request.name, email = request.email, phonenumber = request.phonenumber)
        )

        request.permissions
            .distinctBy { it.employeeId }
            .forEach { institution.permissions.add(savePermission(institution, it)) }

        return InstitutionWithPermissionsResponse.from(institution)
    }

    @Transactional
    fun update(request: InstitutionUpdateRequest): InstitutionUpdateResult {
        val institution = institutionRepository.findByIdOrNull(request.id)
            ?: return InstitutionUpdateResult.NotFound

        institution.name = request.name
        institution.email = request.email
        institution.phonenumber = request.phonenumber

        val requestedPermissions = request.permissions.associateBy { it.employeeId }
        institution.permissions.removeIf { it.id.employeeId !in requestedPermissions.keys }
        institution.permissions.forEach { permission ->
            requestedPermissions[permission.id.employeeId]?.let { applyFlags(permission, it) }
        }

        val existingEmployeeIds = institution.permissions.map { it.id.employeeId }.toSet()
        requestedPermissions.values
            .filter { it.employeeId !in existingEmployeeIds }
            .forEach { institution.permissions.add(savePermission(institution, it)) }

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
        return institutionRepository.findByIdOrNull(id)
    }

    private fun savePermission(institution: Institution, request: InstitutionPermissionRequest): Permission {
        val employee = employeeService.getEntityById(request.employeeId)
            ?: throw IllegalArgumentException("employee not found")
        val permission = Permission(
            id = PermissionKey(employeeId = request.employeeId, institutionId = institution.id),
            employee = employee,
            institution = institution
        )
        applyFlags(permission, request)
        return permissionService.savePermissionEntity(permission)
    }

    private fun applyFlags(permission: Permission, request: InstitutionPermissionRequest) {
        permission.readEntries = request.readEntries
        permission.writeEntries = request.writeEntries
        permission.changeInstitution = request.changeInstitution
        permission.affiliated = request.affiliated
    }
}
