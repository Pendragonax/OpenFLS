package de.vinz.openfls.services

import de.vinz.openfls.dtos.ServiceFilterDto
import de.vinz.openfls.model.Service
import de.vinz.openfls.repositories.ServiceRepository
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration
import java.time.LocalDate
import java.time.Period
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

    override fun getById(id: Long): Service? {
        return serviceRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return serviceRepository.existsById(id)
    }

    fun getByEmployeeAndDate(employeeId: Long, date: LocalDate): List<Service> {
        return serviceRepository.findByEmployeeAndDate(employeeId, date)
    }

    fun getByClientAndDate(clientId: Long, date: LocalDate): List<Service> {
        return serviceRepository.findByClientAndDate(clientId, date)
    }

    fun getByEmployeeAndFilter(employeeId: Long, filter: ServiceFilterDto): List<Service> {
        if (filter.date == null)
            return emptyList()

        if (filter.clientId != null)
            return serviceRepository.findByEmployeeAndClientAndDate(employeeId, filter.clientId!!, filter.date!!)

        return serviceRepository.findByEmployeeAndDate(employeeId, filter.date!!)
    }
}