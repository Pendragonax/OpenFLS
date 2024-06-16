package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.services.GenericService
import de.vinz.openfls.domains.permissions.PermissionService
import org.springframework.stereotype.Service
import jakarta.transaction.Transactional
import org.modelmapper.ModelMapper

@Service
class InstitutionService(
        private val institutionRepository: InstitutionRepository,
        private val permissionServiceImpl: PermissionService,
        private val modelMapper: ModelMapper
): GenericService<Institution> {

    @Transactional
    fun create(institutionDto: InstitutionDto): InstitutionDto {
        val entity = save(modelMapper.map(institutionDto.apply { assistancePlans = null }, Institution::class.java))

        // only created permissions
        institutionDto.permissions = institutionDto
                .permissions
                ?.filter {
                    entity
                            .permissions
                            ?.any { permission -> permission.id.employeeId == it.employeeId } ?: false }
                ?.map { it.apply { institutionId = entity.id!! } }
                ?.toTypedArray()

        // assistance plans
        institutionDto.assistancePlans = entity.assistancePlans
                ?.map { modelMapper.map(it, AssistancePlanDto::class.java) }?.toTypedArray()

        return institutionDto
    }

    @Transactional
    override fun create(value: Institution): Institution {
        return save(value)
    }

    @Transactional
    fun update(valueDto: InstitutionDto): InstitutionDto {
        val entity = modelMapper.map(valueDto
                .apply { assistancePlans = null }, Institution::class.java)
        val savedEntity = update(entity)

        // only created or updated permissions
        valueDto.permissions = valueDto
                .permissions
                ?.filter { savedEntity
                        .permissions
                        ?.any { permission -> permission.id.employeeId == it.employeeId } ?: false }
                ?.map { it.apply { institutionId = savedEntity.id!! } }
                ?.toTypedArray()

        // assistance plans
        valueDto.assistancePlans = savedEntity.assistancePlans
                ?.map { modelMapper.map(it, AssistancePlanDto::class.java) }?.toTypedArray()

        return valueDto
    }

    @Transactional
    override fun update(value: Institution): Institution {
        return save(value)
    }

    @Transactional
    private fun save(institution: Institution): Institution {
        val tmpPermissions = institution.permissions
        institution.permissions = null

        val tmpInstitution = institutionRepository.save(institution)
        tmpInstitution.permissions = permissionServiceImpl
            .savePermissionsByInstitution(tmpPermissions ?: mutableSetOf(), tmpInstitution)

        return tmpInstitution
    }

    @Transactional
    override fun delete(id: Long) {
        institutionRepository.deleteById(id)
    }

    fun getAllDtos(): List<InstitutionDto> {
        return getAll()
                .sortedBy { it.name.lowercase() }
                .map { value -> modelMapper.map(value, InstitutionDto::class.java) }
    }

    override fun getAll(): List<Institution> {
        return institutionRepository.findAll().toList()
    }

    fun getDtoById(id: Long): InstitutionDto? {
        val entity = institutionRepository.findById(id).orElse(null)
        return modelMapper.map(entity, InstitutionDto::class.java)
    }

    override fun getById(id: Long): Institution? {
        return institutionRepository.findById(id).orElse(null)
    }

    override fun existsById(id: Long): Boolean {
        return institutionRepository.existsById(id)
    }
}