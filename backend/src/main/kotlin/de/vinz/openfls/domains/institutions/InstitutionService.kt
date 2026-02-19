package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.institutions.dtos.*
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.permissions.PermissionDto
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InstitutionService(
    private val institutionRepository: InstitutionRepository,
    private val employeeRepository: EmployeeRepository,
    private val modelMapper: ModelMapper
) {

    @Transactional
    fun create(dto: CreateInstitutionDTO): CreateInstitutionDTO {
        val entityToCreate = Institution.of(dto)
        entityToCreate.permissions = createPermissions(dto, entityToCreate)
        val entity = institutionRepository.save(entityToCreate)
        return CreateInstitutionDTO.of(entity)
    }

    @Transactional
    fun update(dto: UpdateInstitutionDTO): UpdateInstitutionDTO {
        val entity = getEntityById(dto.id) ?: throw IllegalArgumentException("Institution with id ${dto.id} not found")

        entity.permissions.removeIf { dto.permissions.none { p -> p.employeeId == it.id.employeeId && p.institutionId == it.id.institutionId } }
        entity.permissions.addAll(getNewPermissions(entity, dto.permissions))
        updatePermissions(entity, dto.permissions)

        entity.name = dto.name
        entity.email = dto.email
        entity.phonenumber = dto.phonenumber

        val savedEntity = institutionRepository.save(entity)

        return UpdateInstitutionDTO.of(savedEntity)
    }

    @Transactional
    fun delete(id: Long) {
        institutionRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAllSoloDTOs(): List<ResponseAllReadableInstitutionDTO> {
        val institutions = institutionRepository.findInstitutionSoloProjectionOrderedByName()
        return institutions.map { ResponseAllReadableInstitutionDTO.of(it) }.sortedBy { it.name }
    }

    @Transactional(readOnly = true)
    fun getAllDTOs(): List<ResponseAllInstitutionDTO> {
        return getAllEntities()
            .map { ResponseAllInstitutionDTO.of(it) }
            .sortedBy { it.name }
    }

    @Transactional(readOnly = true)
    fun getAllEntities(): List<Institution> {
        return institutionRepository.findAll().sortedBy { it.name }.toList()
    }

    @Transactional(readOnly = true)
    fun getDTOById(id: Long): ResponseByIDInstitutionDTO? {
        val entity = institutionRepository.findById(id).orElse(null)
        return modelMapper.map(entity, ResponseByIDInstitutionDTO::class.java)
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
        for (permissionDTO in permissions) {
            val permission =
                entity.permissions.find { it.id.employeeId == permissionDTO.employeeId && it.id.institutionId == permissionDTO.institutionId }
            if (permission != null) {
                continue
            }

            newPermissions.add(Permission.of(permissionDTO))
        }

        return newPermissions
    }

    private fun updatePermissions(entity: Institution, permissions: List<PermissionDto>): Institution {
        for (permission in entity.permissions) {
            val permissionDTO =
                permissions.find { it.employeeId == permission.id.employeeId && it.institutionId == permission.id.institutionId }
            if (permissionDTO == null) {
                continue
            }

            permission.readEntries = permissionDTO.readEntries
            permission.writeEntries = permissionDTO.writeEntries
            permission.changeInstitution = permissionDTO.changeInstitution
            permission.affiliated = permissionDTO.affiliated
        }

        return entity
    }

    private fun createPermissions(
        dto: CreateInstitutionDTO,
        entityToCreate: Institution
    ): MutableSet<Permission> = Permission.of(dto.permissions).map {
        it.institution = entityToCreate
        it.employee = employeeRepository.findById(it.id.employeeId ?: 0).orElse(null)
        it
    }.toMutableSet()
}
