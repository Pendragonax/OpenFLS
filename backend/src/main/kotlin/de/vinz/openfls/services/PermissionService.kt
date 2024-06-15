package de.vinz.openfls.services

import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.entities.Permission
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.dtos.PermissionDto
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.repositories.PermissionRepository
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper

@Service
class PermissionService(
        val employeeRepository: EmployeeRepository,
        val institutionRepository: InstitutionRepository,
        val permissionRepository: PermissionRepository,
        val modelMapper: ModelMapper
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

    fun convertToPermissions(permissionDtos: Array<PermissionDto>?, employeeId: Long): MutableSet<Permission> {
        return permissionDtos
                ?.map {
                    modelMapper
                            .map(it, Permission::class.java)
                            .apply { it.employeeId = employeeId } }
                ?.toMutableSet() ?: mutableSetOf()
    }
}