package de.vinz.openfls.services

import de.vinz.openfls.dtos.ServiceFilterDto
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.model.Service
import de.vinz.openfls.repositories.ServiceRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration
import java.time.LocalDate
import javax.transaction.Transactional

@org.springframework.stereotype.Service
class ServiceService(
    private val serviceRepository: ServiceRepository
): GenericService<Service> {

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    private val logger: Logger = LoggerFactory.getLogger(ServiceService::class.java)

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
        val startMs = System.currentTimeMillis()
        val result = serviceRepository.findByEmployeeAndStartAndEnd(employeeId, start, end)

        if (logPerformance) {
            logger.info(String.format("%s getByEmployeeAndStartAndEnd took %s ms and found %d entities",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    result.size))
        }
        return result
    }

    fun getByEmployeeAndStartEndDate(employeeId: Long, start: LocalDate, end: LocalDate): List<Service> {
        val startMs = System.currentTimeMillis()
        val result = serviceRepository.findByEmployeeAndStartEndDate(employeeId, start, end)

        if (logPerformance) {
            logger.info(String.format("%s getByEmployeeAndStartEndDate took %s ms and found %d entities",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    result.size))
        }
        return result
    }

    fun getByClientAndDate(clientId: Long, date: LocalDate): List<Service> {
        val startMs = System.currentTimeMillis()
        val result = serviceRepository.findByClientAndDate(clientId, date)

        if (logPerformance) {
            logger.info(String.format("%s getByClientAndDate took %s ms and found %d entities",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    result.size))
        }
        return result
    }

    fun getByClientAndStartAndEnd(clientId: Long, start: LocalDate, end: LocalDate): List<Service> {
        val startMs = System.currentTimeMillis()
        val result = serviceRepository.findByClientAndStartAndEnd(clientId, start, end)

        if (logPerformance) {
            logger.info(String.format("%s getByClientAndStartAndEnd took %s ms and found %d entities",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs,
                    result.size))
        }
        return result
    }

    fun getByEmployeeAndFilter(employeeId: Long, filter: ServiceFilterDto): List<Service> {
        if (filter.date == null)
            return emptyList()

        if (filter.clientId != null)
            return serviceRepository.findByEmployeeAndClientAndDate(employeeId, filter.clientId!!, filter.date!!)

        return serviceRepository.findByEmployeeAndDate(employeeId, filter.date!!)
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