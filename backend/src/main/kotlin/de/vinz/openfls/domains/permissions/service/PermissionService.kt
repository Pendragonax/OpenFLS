package de.vinz.openfls.domains.permissions.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.service.EmployeeAccessService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.service.InstitutionLookupService
import de.vinz.openfls.domains.permissions.dto.PermissionRequest
import de.vinz.openfls.domains.permissions.entity.Permission
import de.vinz.openfls.domains.permissions.entity.PermissionKey
import de.vinz.openfls.domains.permissions.repository.PermissionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PermissionService(
        private val employeeAccessService: EmployeeAccessService,
        private val institutionLookupService: InstitutionLookupService,
        private val permissionRepository: PermissionRepository
) {

    @InternalEntityApi
    @Transactional
    fun savePermissionEntity(permission: Permission): Permission {
        requireNotNull(permission.employee) { "employee is required" }
        requireNotNull(permission.institution) { "institution is required" }

        return permissionRepository.save(permission)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByEmployeeId(employeeId: Long): List<Permission> {
        return permissionRepository.findByEmployeeId(employeeId).toList()
    }

    @Transactional(readOnly = true)
    fun getLeadingInstitutionIdsByEmployee(employeeId: Long): List<Long> {
        return permissionRepository.findByEmployeeId(employeeId)
            .filter { it.changeInstitution }
            .map { it.id.institutionId ?: 0 }
            .toList()
    }

    @Transactional(readOnly = true)
    fun getReadableInstitutionIdsByEmployee(employeeId: Long): List<Long> {
        return permissionRepository.findByEmployeeId(employeeId)
            .filter { it.readEntries }
            .map { it.id.institutionId ?: 0 }
            .toList()
    }

    @Transactional(readOnly = true)
    fun getWritableInstitutionIdsByEmployee(employeeId: Long): List<Long> {
        return permissionRepository.findByEmployeeId(employeeId)
            .filter { it.writeEntries }
            .map { it.id.institutionId ?: 0 }
            .toList()
    }

    @Transactional(readOnly = true)
    fun getAffiliatedInstitutionIdsByEmployee(employeeId: Long): List<Long> {
        return permissionRepository.findByEmployeeId(employeeId)
            .filter { it.affiliated }
            .map { it.id.institutionId ?: 0 }
            .toList()
    }

    /** The institutions the requests refer to by id, or `null` if one of them does not exist. */
    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getInstitutionsForRequests(permissionRequests: List<PermissionRequest>): Map<Long, Institution>? {
        return permissionRequests.map { it.institutionId }.distinct()
            .associateWith { institutionLookupService.getEntityById(it) ?: return null }
    }

    @InternalEntityApi
    fun convertToPermissions(
        permissionRequests: List<PermissionRequest>,
        employee: Employee,
        institutions: Map<Long, Institution>
    ): MutableSet<Permission> {
        return permissionRequests
            .map { request ->
                Permission(
                    id = PermissionKey(employeeId = employee.id, institutionId = request.institutionId),
                    employee = employee,
                    institution = institutions.getValue(request.institutionId),
                    readEntries = request.readEntries,
                    writeEntries = request.writeEntries,
                    changeInstitution = request.changeInstitution,
                    affiliated = request.affiliated
                )
            }
            .toMutableSet()
    }

    @Transactional(readOnly = true)
    fun isAdminByUserId(userId: Long): Boolean {
        return employeeAccessService.isAdminById(userId)
    }
}
