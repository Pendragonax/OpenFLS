package de.vinz.openfls.services

import de.vinz.openfls.dtos.ServiceFilterDto
import de.vinz.openfls.entities.Service
import de.vinz.openfls.projections.ServiceProjection
import de.vinz.openfls.projections.ServiceSoloProjection
import de.vinz.openfls.repositories.ServiceRepository
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.transaction.Transactional

@org.springframework.stereotype.Service
class ServiceService(
    private val serviceRepository: ServiceRepository
): GenericService<Service> {

    @Transactional
    override fun create(value: Service): Service {
        if (value.id > 0)
            throw IllegalArgumentException("id is greater than 0")
        if (value.start >= value.end)
            throw IllegalArgumentException("start is equal or greater than end")

        value.minutes = Duration.between(value.start, value.end).toMinutes().toInt()

        return serviceRepository.save(value)
    }

    @Transactional
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

    @Transactional
    override fun delete(id: Long) {
        serviceRepository.deleteById(id)
    }

    override fun getAll(): List<Service> {
        return serviceRepository.findAll().toList()
    }

    fun getAllByInstitutionAndYear(institutionId: Long, year: Int): List<ServiceProjection> {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        return serviceRepository.findByInstitutionIdAndStartAndEnd(institutionId, start, end)
    }

    override fun getById(id: Long): Service? {
        return serviceRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return serviceRepository.existsById(id)
    }

    fun getByAssistancePlan(id: Long): List<Service> {
        return serviceRepository.findByAssistancePlan(id)
    }

    fun getByEmployeeAndDate(employeeId: Long, date: LocalDate): List<Service> {
        return serviceRepository.findByEmployeeAndDate(employeeId, date)
    }

    fun getByEmployeeAndStartAndEnd(employeeId: Long, start: LocalDate, end: LocalDate): List<Service> {
        return serviceRepository.findByEmployeeAndStartAndEnd(employeeId, start, end)
    }

    fun getByEmployeeAndStartEndDate(employeeId: Long, start: LocalDate, end: LocalDate): List<Service> {
        return serviceRepository.findByEmployeeAndStartEndDate(employeeId, start, end)
    }

    fun getByClientAndDate(clientId: Long, date: LocalDate): List<Service> {
        return serviceRepository.findByClientAndDate(clientId, date)
    }

    fun getByClientAndStartAndEnd(clientId: Long, start: LocalDate, end: LocalDate): List<Service> {
        return serviceRepository.findByClientAndStartAndEnd(clientId, start, end)
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

    fun getAllByAssistancePlanIdAndYearAndMonth(year: Int,
                                                month: Int,
                                                assistancePlanId: Long,): List<ServiceSoloProjection> {
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