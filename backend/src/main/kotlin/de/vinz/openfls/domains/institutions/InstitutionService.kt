package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.institutions.dtos.*
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.permissions.PermissionDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InstitutionService(
    private val institutionRepository: InstitutionRepository,
    private val employeeRepository: EmployeeRepository
) {

    @Transactional
    fun create(dto: CreateInstitutionDto): InstitutionDto {
        val entityToCreate = Institution.of(dto)
        entityToCreate.permissions = createPermissions(dto, entityToCreate)
        val entity = institutionRepository.save(entityToCreate)
        return InstitutionDto.of(entity)
    }

    @Transactional
    fun update(dto: UpdateInstitutionDto): InstitutionDto {
        val entity = getEntityById(dto.id) ?: throw IllegalArgumentException("Institution with id ${dto.id} not found")

        entity.permissions.removeIf { dto.permissions.none { p -> p.employeeId == it.id.employeeId && p.institutionId == it.id.institutionId } }
        entity.permissions.addAll(getNewPermissions(entity, dto.permissions))
        updatePermissions(entity, dto.permissions)

        entity.name = dto.name
        entity.email = dto.email
        entity.phonenumber = dto.phonenumber

        val savedEntity = institutionRepository.save(entity)

        return InstitutionDto.of(savedEntity)
    }

    @Transactional
    fun delete(id: Long) {
        institutionRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAllSolo(): List<InstitutionSoloDto> {
        return InstitutionSoloDto.ofSoloProjection(
            institutionRepository.findInstitutionSoloProjectionOrderedByName()
        ).sortedBy { it.name }
    }

    @Transactional(readOnly = true)
    fun getAll(): List<InstitutionDto> {
        return getAllEntities()
            .map { InstitutionDto.of(it) }
            .sortedBy { it.name }
    }

    @Transactional(readOnly = true)
    fun getAllEntities(): List<Institution> {
        return institutionRepository.findAll().sortedBy { it.name }.toList()
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): InstitutionDto? {
        return institutionRepository.findById(id).orElse(null)?.let(InstitutionDto::of)
    }

    @Transactional(readOnly = true)
    fun getEntityById(id: Long): Institution? {
        return institutionRepository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return institutionRepository.existsById(id)
    }

    private fun getNewPermissions(
        entity: Institution,
        permissions: List<PermissionDto>
    ): List<Permission> {
        val newPermissions = mutableListOf<Permission>()
        for (permissionDto in permissions) {
            val permission =
                entity.permissions.find { it.id.employeeId == permissionDto.employeeId && it.id.institutionId == permissionDto.institutionId }
            if (permission != null) {
                continue
            }

            newPermissions.add(Permission.of(permissionDto))
        }

        return newPermissions
    }

    private fun updatePermissions(entity: Institution, permissions: List<PermissionDto>): Institution {
        for (permission in entity.permissions) {
            val permissionDto =
                permissions.find { it.employeeId == permission.id.employeeId && it.institutionId == permission.id.institutionId }
            if (permissionDto == null) {
                continue
            }

            permission.readEntries = permissionDto.readEntries
            permission.writeEntries = permissionDto.writeEntries
            permission.changeInstitution = permissionDto.changeInstitution
            permission.affiliated = permissionDto.affiliated
        }

        return entity
    }

    private fun createPermissions(
        dto: CreateInstitutionDto,
        entityToCreate: Institution
    ): MutableSet<Permission> = Permission.of(dto.permissions).map {
        it.institution = entityToCreate
        it.employee = employeeRepository.findById(it.id.employeeId ?: 0).orElse(null)
        it
    }.toMutableSet()
}
