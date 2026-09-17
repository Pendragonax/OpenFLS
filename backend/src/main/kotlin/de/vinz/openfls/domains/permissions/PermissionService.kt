package de.vinz.openfls.domains.permissions

import de.vinz.openfls.domains.employees.EmployeeAccessRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import org.springframework.transaction.annotation.Transactional
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service
import de.vinz.openfls.domains.institutions.InstitutionRepository

@Service
class PermissionService(
        val employeeRepository: EmployeeRepository,
        val employeeAccessRepository: EmployeeAccessRepository,
        val institutionRepository: InstitutionRepository,
        val permissionRepository: PermissionRepository,
        val modelMapper: ModelMapper
) {

    // ---- Controller-facing (DTO in / DTO out) ----------------------------------

    @Transactional
    fun savePermission(permissionDto: PermissionDto): PermissionDto {
        return PermissionDto.of(permissionRepository.save(convertToEntity(permissionDto)))
    }

    @Transactional
    fun deleteById(id: Long) {
        permissionRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<PermissionDto> {
        return permissionRepository.findAll().map { PermissionDto.of(it) }
    }

    @Transactional(readOnly = true)
    fun getByInstitutionId(institutionId: Long): List<PermissionDto> {
        return permissionRepository.findByInstitutionId(institutionId).map { PermissionDto.of(it) }
    }

    @Transactional(readOnly = true)
    fun getByEmployeeId(employeeId: Long): List<PermissionDto> {
        return permissionRepository.findByEmployeeId(employeeId).map { PermissionDto.of(it) }
    }

    @Transactional(readOnly = true)
    fun getByEmployeeIdAndInstitutionId(employeeId: Long, institutionId: Long): PermissionDto? {
        return permissionRepository.findByIds(employeeId, institutionId)?.let { PermissionDto.of(it) }
    }

    // ---- Internal: entity composition for other services ----------------------

    @Transactional
    fun savePermissionEntity(permission: Permission): Permission {
        if (permission.id.employeeId == null || permission.id.institutionId == null) {
            throw IllegalArgumentException()
        }

        permission.employee = employeeRepository
            .findById(permission.id.employeeId!!)
            .orElseThrow { IllegalArgumentException("employee not found") }

        permission.institution = institutionRepository
            .findById(permission.id.institutionId!!)
            .orElseThrow { IllegalArgumentException("institution not found") }

        return permissionRepository.save(permission)
    }

    @Transactional(readOnly = true)
    fun getEntitiesByEmployeeId(employeeId: Long): List<Permission> {
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

    fun convertToPermissions(permissionDtos: List<PermissionDto>?, employee: Employee): MutableSet<Permission> {
        val entities = permissionDtos
                ?.map {
                    modelMapper
                            .map(it, Permission::class.java)
                }
                ?.toMutableSet() ?: mutableSetOf()

        for (permission in entities) {
            permission.employee = employee
            permission.institution = institutionRepository.findById(permission.id.institutionId!!).get()
        }

        return entities
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
