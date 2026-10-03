package de.vinz.openfls.domains.permissions.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.service.EmployeeAccessService
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.permissions.dto.PermissionRequest
import de.vinz.openfls.domains.permissions.entity.Permission
import de.vinz.openfls.domains.permissions.repository.PermissionRepository
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PermissionService(
        private val employeeAccessService: EmployeeAccessService,
        private val institutionRepository: InstitutionRepository,
        private val permissionRepository: PermissionRepository,
        private val modelMapper: ModelMapper
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

    @InternalEntityApi
    fun convertToPermissions(permissionRequests: List<PermissionRequest>?, employee: Employee): MutableSet<Permission> {
        val entities = permissionRequests
                ?.map { modelMapper.map(it, Permission::class.java) }
                ?.toMutableSet() ?: mutableSetOf()

        for (permission in entities) {
            permission.employee = employee
            permission.institution = institutionRepository.findById(permission.id.institutionId!!).get()
        }

        return entities
    }

    @Transactional(readOnly = true)
    fun isAdminByUserId(userId: Long): Boolean {
        return employeeAccessService.isAdminById(userId)
    }
}
