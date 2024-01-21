package de.vinz.openfls.services

import de.vinz.openfls.entities.Employee
import de.vinz.openfls.entities.Institution
import de.vinz.openfls.entities.Permission
import de.vinz.openfls.repositories.EmployeeRepository
import de.vinz.openfls.repositories.InstitutionRepository
import de.vinz.openfls.repositories.PermissionRepository
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import javax.transaction.Transactional

@Service
class PermissionService(
    val employeeRepository: EmployeeRepository,
    val institutionRepository: InstitutionRepository,
    val permissionRepository: PermissionRepository
) {
    @Transactional
    fun savePermission(permission: Permission): Permission {
        if (permission.id.employeeId == null || permission.id.institutionId == null) {
            throw IllegalArgumentException()
        }

        permission.employee = employeeRepository
            .findById(permission.id.employeeId!!)
            .orElseThrow { throw IllegalArgumentException("employee not found") }

        permission.institution = institutionRepository
            .findById(permission.id.institutionId!!)
            .orElseThrow { throw IllegalArgumentException("institution not found") }

        return permissionRepository.save(permission)
    }

    @Transactional
    fun savePermissions(permissions: MutableSet<Permission>): MutableSet<Permission> {
        return permissions.map { savePermission(it) }.toMutableSet()
    }

    @Transactional
    fun savePermissionByEmployee(permission: Permission, employee: Employee): Permission {
        if (permission.id.institutionId == null) {
            throw IllegalArgumentException()
        }

        permission.institution = institutionRepository
            .findById(permission.id.institutionId!!)
            .orElseThrow { throw IllegalArgumentException("institution not found") }

        return permissionRepository.save(permission)
    }

    @Transactional
    fun savePermissionsByEmployee(permissions: MutableSet<Permission>, employee: Employee): MutableSet<Permission> {
        return permissions.map { savePermissionByEmployee(it, employee) }.toMutableSet()
    }

    @Transactional
    fun savePermissionByInstitution(permission: Permission, institution: Institution): Permission {
        if (permission.id.employeeId == null) {
            throw IllegalArgumentException()
        }

        permission.employee = employeeRepository
            .findById(permission.id.employeeId!!)
            .orElseThrow { throw IllegalArgumentException("employee not found") }
        permission.institution = institution

        return permissionRepository.save(permission)
    }

    @Transactional
    fun savePermissionsByInstitution(permissions: MutableSet<Permission>, institution: Institution): MutableSet<Permission> {
        return permissions.map { savePermissionByInstitution(it, institution) }.toMutableSet()
    }

    fun getPermissionByIdAndInstitution(employeeId: Long, institutionId: Long): Permission? {
        return permissionRepository.findByIds(employeeId, institutionId)
    }

    fun getPermissionByEmployee(employeeId: Long): List<Permission> {
        return permissionRepository.findByEmployeeId(employeeId).toList()
    }

    fun getLeadingInstitutionIdsByEmployee(employeeId: Long): List<Long> {
        return permissionRepository.findByEmployeeId(employeeId)
            .filter { it.changeInstitution }
            .map { it.id.institutionId ?: 0 }
            .toList();
    }

    fun getAffiliatedInstitutionIdsByEmployee(employeeId: Long): List<Long> {
        return permissionRepository.findByEmployeeId(employeeId)
            .filter { it.affiliated }
            .map { it.id.institutionId ?: 0 }
            .toList();
    }

    fun getReadableInstitutionIdsByEmployee(employeeId: Long): List<Long> {
        return permissionRepository.findByEmployeeId(employeeId)
            .filter { it.readEntries }
            .map { it.id.institutionId ?: 0 }
            .toList();
    }
}