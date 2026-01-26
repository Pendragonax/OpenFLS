package de.vinz.openfls.domains.services.services

import de.vinz.openfls.domains.services.Service
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.domains.services.dtos.ServiceDto
import de.vinz.openfls.domains.services.dtos.ServiceFilterDto
import de.vinz.openfls.domains.services.dtos.ServiceXLDto
import de.vinz.openfls.domains.services.projections.ServiceProjection
import de.vinz.openfls.domains.services.projections.ServiceSoloProjection
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration
import java.time.LocalDate

@org.springframework.stereotype.Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val modelMapper: org.modelmapper.ModelMapper
) : de.vinz.openfls.services.GenericService<Service> {

    @org.springframework.transaction.annotation.Transactional
    fun create(serviceDto: ServiceDto): ServiceDto {
        val entity = modelMapper.map(serviceDto, Service::class.java)

        entity.employee?.unprofessionals = null
        println(entity.toString())
        return modelMapper.map(create(entity), ServiceDto::class.java)
    }

    @org.springframework.transaction.annotation.Transactional
    override fun create(value: Service): Service {
        if (value.id > 0)
            throw IllegalArgumentException("id is greater than 0")
        if (value.start >= value.end)
            throw IllegalArgumentException("start is equal or greater than end")

        value.minutes = Duration.between(value.start, value.end).toMinutes().toInt()

        return serviceRepository.save(value)
    }

    @org.springframework.transaction.annotation.Transactional
    fun update(serviceDto: ServiceDto): ServiceDto {
        val entity = modelMapper.map(serviceDto, Service::class.java)

        val savedEntity = update(entity)

        return modelMapper.map(savedEntity, ServiceDto::class.java)
    }

    @org.springframework.transaction.annotation.Transactional
    override fun update(value: Service): Service {
        if (value.id <= 0)
            throw IllegalArgumentException("id is set")
        if (!serviceRepository.existsById(value.id))
            throw IllegalArgumentException("id not found")
        if (value.start >= value.end)
            throw IllegalArgumentException("start is equal or greater than end")

        value.minutes = Duration.between(value.start, value.end).toMinutes().toInt()

        return serviceRepository.save(value)
    }

    @org.springframework.transaction.annotation.Transactional
    override fun delete(id: Long) {
        serviceRepository.deleteById(id)
    }

    fun getAllDtos(): List<ServiceDto> {
        return getAll().map { modelMapper.map(it, ServiceDto::class.java) }
    }

    override fun getAll(): List<Service> {
        return serviceRepository.findAll().toList()
    }

    fun getProjectionsByInstitutionIdsAndStartAndEnd(institutionIds: List<Long>,
                                                     start: LocalDate,
                                                     end: LocalDate
    ): List<ServiceProjection> {
        return serviceRepository.findProjectionsByInstitutionIdsAndStartAndEnd(institutionIds, start, end)
    }

    fun getAllByInstitutionAndYear(institutionId: Long, year: Int): List<ServiceProjection> {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        return serviceRepository.findByInstitutionIdAndStartAndEnd(institutionId, start, end)
    }

    fun getDtoById(id: Long): ServiceDto? {
        return modelMapper.map(getById(id), ServiceDto::class.java)
    }

    override fun getById(id: Long): Service? {
        return serviceRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return serviceRepository.existsById(id)
    }

    fun getXLDtosByAssistancePlan(id: Long): List<ServiceXLDto> {
        return getByAssistancePlan(id).map {
            modelMapper.map(it, ServiceXLDto::class.java) }
    }

    fun getByAssistancePlan(id: Long): List<Service> {
        return serviceRepository.findByAssistancePlan(id)
    }

    fun getIllegalByAssistancePlan(id: Long): List<ServiceProjection> {
        return serviceRepository.findIllegalByAssistancePlan(id)
    }

    fun getByAssistancePlanAndNotBetweenStartAndEnd(id: Long, start: LocalDate, end: LocalDate): List<ServiceProjection> {
        return serviceRepository.findByAssistancePlanAndNotBetweenStartAndEnd(id, start, end)
    }

    fun getDtosByEmployeeAndDate(employeeId: Long, date: LocalDate): List<ServiceDto> {
        return getByEmployeeAndDate(employeeId, date).map { modelMapper.map(it, ServiceDto::class.java) }
    }

    fun getIllegalByEmployee(employeeId: Long): List<ServiceProjection> {
        return serviceRepository.findIllegalByEmployee(employeeId)
    }

    fun getByEmployeeAndDate(employeeId: Long, date: LocalDate): List<Service> {
        return serviceRepository.findByEmployeeAndDate(employeeId, date)
    }

    fun getDtosByEmployeeAndStartAndEnd(employeeId: Long, start: LocalDate, end: LocalDate): List<ServiceDto> {
        return getByEmployeeAndStartAndEnd(employeeId, start, end)
                .map { modelMapper.map(it, ServiceDto::class.java) }
    }

    fun getByEmployeeAndStartAndEnd(employeeId: Long, start: LocalDate, end: LocalDate): List<ServiceProjection> {
        return serviceRepository.findByEmployeeAndStartAndEnd(employeeId, start, end)
    }

    fun getDtosByEmployeeAndStartEndDate(employeeId: Long, start: LocalDate, end: LocalDate): List<ServiceDto> {
        return getByEmployeeAndStartEndDate(employeeId, start, end)
                .map { modelMapper.map(it, ServiceDto::class.java) }
    }

    fun getByEmployeeAndStartEndDate(employeeId: Long, start: LocalDate, end: LocalDate): List<Service> {
        return serviceRepository.findByEmployeeAndStartEndDate(employeeId, start, end)
    }

    fun getIllegalByInstitutionId(id: Long): List<ServiceProjection> {
        return serviceRepository.findIllegalByInstitutionId(id)
    }

    fun getDtosByInstitutionIdAndDate(institutionId: Long, date: LocalDate): List<ServiceProjection> {
        return getByInstitutionIdAndDate(institutionId, date)
    }

    fun getByInstitutionIdAndDate(institutionId: Long, date: LocalDate): List<ServiceProjection> {
        return serviceRepository.findByInstitutionIdAndDate(institutionId, date)
    }

    fun getDtosByInstitutionIdAndStartAndEnd(institutionId: Long,
                                             start: LocalDate,
                                             end: LocalDate
    ): List<ServiceProjection> {
        return getByInstitutionIdAndStartAndEnd(institutionId, start, end)
    }

    fun getByInstitutionIdAndStartAndEnd(institutionId: Long,
                                         start: LocalDate,
                                         end: LocalDate
    ): List<ServiceProjection> {
        return serviceRepository.findByInstitutionIdAndStartAndEnd(institutionId, start, end).sortedBy { it.start }
    }

    fun getProjections(institutionId: Long,
                       clientId: Long,
                       start: LocalDate,
                       end: LocalDate,
                       allowedInstitutionIds: List<Long>): List<ServiceProjection> {
        return if (institutionId > 0 && clientId > 0) {
            serviceRepository.findByInstitutionIdAndClientIdAndStartAndEnd(institutionId, clientId, start, end)
        } else if (institutionId > 0) {
            getDtosByInstitutionIdAndStartAndEnd(institutionId, start, end)
        } else if (clientId > 0) {
            getProjectionsByInstitutionIdsAndClientIdAndStartAndEnd(
                    allowedInstitutionIds, clientId, start, end)
        } else {
            getProjectionsByInstitutionIdsAndStartAndEnd(allowedInstitutionIds, start, end)
        }
    }

    fun getProjections(institutionId: Long,
                       employeeId: Long,
                       clientId: Long,
                       start: LocalDate,
                       end: LocalDate,
                       allowedInstitutionIds: List<Long>): List<ServiceProjection> {
        return serviceRepository.findProjectionsBy(institutionId, allowedInstitutionIds, employeeId, clientId, start, end)
    }

    fun getDtosByClientAndDate(clientId: Long, date: LocalDate): List<ServiceDto> {
        return getByClientAndDate(clientId, date)
                .map { modelMapper.map(it, ServiceDto::class.java) }
    }

    fun getByClientAndDate(clientId: Long, date: LocalDate): List<Service> {
        return serviceRepository.findByClientAndDate(clientId, date)
    }

    fun getProjectionsByInstitutionIdsAndClientIdAndStartAndEnd(institutionIds: List<Long>,
                                                                clientId: Long,
                                                                start: LocalDate,
                                                                end: LocalDate): List<ServiceProjection> {
        return serviceRepository.findProjectionsByInstitutionIdsAndClientIdAndStartAndEnd(
                institutionIds, clientId, start, end)
    }

    fun getDtosByClientAndStartAndEnd(clientId: Long, start: LocalDate, end: LocalDate): List<ServiceDto> {
        return getByClientAndStartAndEnd(clientId, start, end)
                .map { modelMapper.map(it, ServiceDto::class.java) }
    }

    fun getByClientAndStartAndEnd(clientId: Long, start: LocalDate, end: LocalDate): List<Service> {
        return serviceRepository.findByClientAndStartAndEnd(clientId, start, end)
    }

    fun getDtosByEmployeeAndFilter(employeeId: Long, filter: ServiceFilterDto): List<ServiceDto> {
        return getByEmployeeAndFilter(employeeId, filter)
                .map { modelMapper.map(it, ServiceDto::class.java) }
    }

    fun getByEmployeeAndFilter(employeeId: Long, filter: ServiceFilterDto): List<Service> {
        if (filter.date == null)
            return emptyList()

        if (filter.clientId != null)
            return serviceRepository.findByEmployeeAndClientAndDate(employeeId, filter.clientId!!, filter.date!!)

        return serviceRepository.findByEmployeeAndDate(employeeId, filter.date!!)
    }

    fun getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(year: Int,
                                                             month: Int,
                                                             assistancePlanId: Long,
                                                             hourTypeId: Long): List<ServiceSoloProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return serviceRepository.findByAssistancePlanIdAndHourTypeIdAndStartAndEnd(
                assistancePlanId, hourTypeId, start, end)
    }

    fun getAllByAssistancePlanIdAndYearAndMonth(
            year: Int,
            month: Int,
            assistancePlanId: Long,
    ): List<ServiceSoloProjection> {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return serviceRepository.findByAssistancePlanIdAndStartAndEnd(
                assistancePlanId, start, end)
    }

    fun countByEmployee(employeeId: Long): Long {
        return serviceRepository.countByEmployeeId(employeeId)
    }

    fun countByClient(clientId: Long): Long {
        return serviceRepository.countByClientId(clientId)
    }

    fun countByAssistancePlan(assistancePlanId: Long): Long {
        return serviceRepository.countByAssistancePlanId(assistancePlanId)
    }

    fun countByGoal(goalId: Long): Long {
        return serviceRepository.countByGoalId(goalId)
    }
}