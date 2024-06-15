package de.vinz.openfls.domains.permissions

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class PermissionService(
        val employeeRepository: EmployeeRepository,
        val institutionRepository: InstitutionRepository,
        val permissionRepository: PermissionRepository,
        val modelMapper: ModelMapper
) {
    @Transactional
    fun savePermission(permissionDto: PermissionDto): Permission {
        return permissionRepository.save(convertToEntity(permissionDto))
    }

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

    @Transactional
    fun deleteById(id: Long) {
        permissionRepository.deleteById(id)
    }

    fun getByInstitutionId(institutionId: Long): List<Permission> {
        return permissionRepository.findByInstitutionId(institutionId).toList()
    }

    fun getByEmployeeId(employeeId: Long): List<Permission> {
        return permissionRepository.findByEmployeeId(employeeId).toList()
    }

    fun getByEmployeeIdAndInstitutionId(employeeId: Long, institutionId: Long): Permission? {
        return permissionRepository.findByIds(employeeId, institutionId)
    }

    fun getById(id: Long): Permission? {
        return permissionRepository.findById(id).orElse(null)
    }

    fun getAll(): List<Permission> {
        return permissionRepository.findAll().toList()
    }

    fun getPermissionByEmployee(employeeId: Long): List<Permission> {
        return permissionRepository.findByEmployeeId(employeeId).toList()
    }

    fun getLeadingInstitutionIdsByEmployee(employeeId: Long): List<Long> {
        return permissionRepository.findByEmployeeId(employeeId)
            .filter { it.changeInstitution }
            .map { it.id.institutionId ?: 0 }
            .toList()
    }

    fun getReadableInstitutionIdsByEmployee(employeeId: Long): List<Long> {
        return permissionRepository.findByEmployeeId(employeeId)
            .filter { it.readEntries }
            .map { it.id.institutionId ?: 0 }
            .toList()
    }

    fun convertToPermissions(permissionDtos: Array<PermissionDto>?, employeeId: Long): MutableSet<Permission> {
        return permissionDtos
                ?.map {
                    modelMapper
                            .map(it, Permission::class.java)
                            .apply { it.employeeId = employeeId } }
                ?.toMutableSet() ?: mutableSetOf()
    }

    private fun convertToEntity(permissionDto: PermissionDto): Permission {
        val permission: Permission = modelMapper.map(permissionDto, Permission::class.java)

        permission.employee = employeeRepository.findById(permissionDto.employeeId).get()
        permission.institution = institutionRepository.findById(permissionDto.institutionId).get()

        return permission
    }
}