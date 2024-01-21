package de.vinz.openfls.services

import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.entities.Client
import de.vinz.openfls.repositories.ClientRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import javax.transaction.Transactional

@Service
class ClientService(
    private val clientRepository: ClientRepository,
    private val institutionService: InstitutionService,
    private val categoryTemplateService: CategoryTemplateService
): GenericService<Client> {

    private val logger: Logger = LoggerFactory.getLogger(ClientService::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @Transactional
    override fun create(value: Client): Client {
        // performance
        val startMs = System.currentTimeMillis()

        value.institution = institutionService.getById(value.institution.id ?: 0)
            ?: throw IllegalArgumentException("institution not found")
        value.categoryTemplate = categoryTemplateService.getById(value.categoryTemplate.id ?: 0)
            ?: throw IllegalArgumentException("category template not found")

        val entity = clientRepository.save(value)

        if (logPerformance) {
            logger.info(String.format("%s create took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    @Transactional
    override fun update(value: Client): Client {
        // performance
        val startMs = System.currentTimeMillis()

        if (!clientRepository.existsById(value.id))
            throw IllegalArgumentException("client not found")

        value.institution = institutionService.getById(value.institution.id ?: 0)
            ?: throw IllegalArgumentException("institution not found")
        value.categoryTemplate = categoryTemplateService.getById(value.categoryTemplate.id ?: 0)
            ?: throw IllegalArgumentException("category template not found")

        val entity = clientRepository.save(value)

        if (logPerformance) {
            logger.info(String.format("%s update took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    @Transactional
    override fun delete(id: Long) {
        // performance
        val startMs = System.currentTimeMillis()

        clientRepository.deleteById(id)

        if (logPerformance) {
            logger.info(String.format("%s delete took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }
    }

    override fun getAll(): List<Client> {
        // performance
        val startMs = System.currentTimeMillis()

        val entity = clientRepository.findAll().toList()

        if (logPerformance) {
            logger.info(String.format("%s getAll took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    override fun getById(id: Long): Client? {
        // performance
        val startMs = System.currentTimeMillis()

        val entity = clientRepository.findById(id).orElse(null)

        if (logPerformance) {
            logger.info(String.format("%s getById took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    override fun existsById(id: Long): Boolean {
        // performance
        val startMs = System.currentTimeMillis()

        val entity = clientRepository.existsById(id)

        if (logPerformance) {
            logger.info(String.format("%s existsById took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }

    fun existById(id: Long): Boolean {
        // performance
        val startMs = System.currentTimeMillis()

        val entity = clientRepository.existsById(id)

        if (logPerformance) {
            logger.info(String.format("%s existById took %s ms",
                    PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                    System.currentTimeMillis() - startMs))
        }

        return entity
    }
}