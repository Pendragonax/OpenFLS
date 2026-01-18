package de.vinz.openfls.domains.permissions

import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import org.springframework.transaction.annotation.Transactional
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class PermissionService(
        val employeeRepository: EmployeeRepository,
        val employeeAccessRepository: EmployeeAccessRepository,
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

    @Transactional(readOnly = true)
    fun getPermissions(dtos: List<PermissionDto>): List<Permission> {
        val existingPermissions = dtos.mapNotNull {
            permissionRepository.findByIds(it.employeeId, it.institutionId)
        }

        for (permission in existingPermissions) {
            val dto = dtos.first { d -> d.employeeId == permission.id.employeeId && d.institutionId == permission.id.institutionId }
            permission.readEntries = dto.readEntries
            permission.writeEntries = dto.writeEntries
            permission.changeInstitution = dto.changeInstitution
            permission.affiliated = dto.affiliated
        }

        val newPermissions = dtos.filter { dto ->
            existingPermissions.none { perm ->
                perm.id.employeeId == dto.employeeId && perm.id.institutionId == dto.institutionId
            }
        }.map {
            Permission.of(it)
        }

        return listOf(existingPermissions, newPermissions).flatten()
    }

    @Transactional(readOnly = true)
    fun getByInstitutionId(institutionId: Long): List<Permission> {
        return permissionRepository.findByInstitutionId(institutionId).toList()
    }

    @Transactional(readOnly = true)
    fun getByEmployeeId(employeeId: Long): List<Permission> {
        return permissionRepository.findByEmployeeId(employeeId).toList()
    }

    @Transactional(readOnly = true)
    fun getByEmployeeIdAndInstitutionId(employeeId: Long, institutionId: Long): Permission? {
        return permissionRepository.findByIds(employeeId, institutionId)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): Permission? {
        return permissionRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<Permission> {
        return permissionRepository.findAll().toList()
    }

    @Transactional(readOnly = true)
    fun getPermissionByEmployee(employeeId: Long): List<Permission> {
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

    fun convertToPermissions(permissionDtos: Array<PermissionDto>?, employeeId: Long): MutableSet<Permission> {
        return permissionDtos
                ?.map {
                    modelMapper
                            .map(it, Permission::class.java)
                            .apply { it.employeeId = employeeId } }
                ?.toMutableSet() ?: mutableSetOf()
    }

    @Transactional(readOnly = true)
    fun isAdminByUserId(userId: Long): Boolean {
        val employeeAccess = employeeAccessRepository.findById(userId)

        if (employeeAccess.isPresent) {
            return employeeAccess.get().role == 1
        }

        return false
    }

    private fun convertToEntity(permissionDto: PermissionDto): Permission {
        val permission: Permission = modelMapper.map(permissionDto, Permission::class.java)

        permission.employee = employeeRepository.findById(permissionDto.employeeId).get()
        permission.institution = institutionRepository.findById(permissionDto.institutionId).get()

        return permission
    }
}
