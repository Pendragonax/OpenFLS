package de.vinz.openfls.domains.institutions

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.employees.entities.EmployeeInstitutionRightsKey
import de.vinz.openfls.domains.institutions.dtos.InstitutionCreateRequest
import de.vinz.openfls.domains.institutions.dtos.InstitutionPermissionRequest
import de.vinz.openfls.domains.institutions.dtos.InstitutionResponse
import de.vinz.openfls.domains.institutions.dtos.InstitutionUpdateRequest
import de.vinz.openfls.domains.institutions.dtos.InstitutionWithPermissionsResponse
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.permissions.PermissionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InstitutionService(
    private val institutionRepository: InstitutionRepository,
    private val permissionService: PermissionService
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
    fun update(request: InstitutionUpdateRequest): InstitutionWithPermissionsResponse {
        val institution = institutionRepository.findById(request.id)
            .orElseThrow { IllegalArgumentException("Institution with id ${request.id} not found") }

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

        return InstitutionWithPermissionsResponse.from(institutionRepository.save(institution))
    }

    @Transactional
    fun delete(id: Long) {
        institutionRepository.deleteById(id)
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
        return institutionRepository.findById(id).orElse(null)?.let { InstitutionWithPermissionsResponse.from(it) }
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Institution? {
        return institutionRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return institutionRepository.existsById(id)
    }

    private fun savePermission(institution: Institution, request: InstitutionPermissionRequest): Permission {
        val permission = Permission(
            id = EmployeeInstitutionRightsKey(employeeId = request.employeeId, institutionId = institution.id)
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
