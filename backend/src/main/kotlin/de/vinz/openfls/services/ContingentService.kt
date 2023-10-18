package de.vinz.openfls.services

import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.model.Contingent
import de.vinz.openfls.repositories.ContingentRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class ContingentService(
    private val contingentRepository: ContingentRepository
) : GenericService<Contingent> {

    private val logger: Logger = LoggerFactory.getLogger(ContingentService::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    override fun create(value: Contingent): Contingent {
        // performance
        val startMs = System.currentTimeMillis()

        val entity = contingentRepository.save(value)

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    override fun update(value: Contingent): Contingent {
        // performance
        val startMs = System.currentTimeMillis()

        if (!contingentRepository.existsById(value.id ?: 0))
            throw IllegalArgumentException("id not found")

        val entity = contingentRepository.save(value)

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    override fun delete(id: Long) {
        // performance
        val startMs = System.currentTimeMillis()

        val entity = contingentRepository.deleteById(id)

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    override fun getAll(): List<Contingent> {
        // performance
        val startMs = System.currentTimeMillis()

        val entities = contingentRepository.findAll().toList()

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entities
    }

    override fun getById(id: Long): Contingent? {
        // performance
        val startMs = System.currentTimeMillis()

        val entity = contingentRepository.findByIdOrNull(id)

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    override fun existsById(id: Long): Boolean {
        // performance
        val startMs = System.currentTimeMillis()

        val entity = contingentRepository.existsById(id)

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    fun getByEmployeeId(id: Long): List<Contingent> {
        // performance
        val startMs = System.currentTimeMillis()

        val entities = contingentRepository.findAllByEmployeeId(id)

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entities
    }

    fun getByInstitutionId(id: Long): List<Contingent> {
        // performance
        val startMs = System.currentTimeMillis()

        val entities = contingentRepository.findAllByInstitutionId(id)

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entities
    }
}