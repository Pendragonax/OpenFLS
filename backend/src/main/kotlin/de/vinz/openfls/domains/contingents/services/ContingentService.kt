package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.contingents.ContingentRepository
import de.vinz.openfls.domains.contingents.projections.ContingentProjection
import de.vinz.openfls.logback.PerformanceLogbackFilter
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.GenericService
import de.vinz.openfls.services.TimeDoubleService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.sql.Time
import java.time.LocalDate

@Service
class ContingentService(
    private val contingentRepository: ContingentRepository,
    @Value("\${openfls.general.workdays.real}") private val workdaysReal: Long,
    @Value("\${openfls.general.workdays.assumption}") private val workdaysAssumption: Long,
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

    fun getAllByInstitutionAndYear(institutionId: Long, year: Int): List<ContingentProjection> {
        return contingentRepository.findByInstitutionIdAndStartAndEnd(
                institutionId,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31))
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

    fun getContingentHoursByYear(year: Int, contingents: List<ContingentProjection>): List<Double> {
        val monthlyHours = ArrayList<Double>(List(13) { 0.0 })

        for (contingent in contingents) {
            val workdayDailyHours = TimeDoubleService.convertTimeDoubleToDouble(contingent.weeklyServiceHours) / 5
            val workdays = DateService.countDaysOfYearBetweenStartAndEnd(year, contingent.start, contingent.end) * 5.0 / 7.0
            val workdaysWithoutVacationInContingent = workdays * (workdaysAssumption.toDouble() / workdaysReal)
            for (month in 1..12) {
                monthlyHours[month] = TimeDoubleService.sumTimeDoubles(
                        monthlyHours[month],
                        getContingentHoursByYearAndMonth(year, month, contingent))
            }

            monthlyHours[0] = TimeDoubleService.sumTimeDoubles(monthlyHours[0],
                    TimeDoubleService.convertDoubleToTimeDouble(workdayDailyHours * workdaysWithoutVacationInContingent))
        }

        return monthlyHours
    }

    fun getContingentHoursByYear(year: Int, contingent: ContingentProjection): List<Double> {
        val workdayDailyHours = contingent.weeklyServiceHours / 5
        val yearlyWorkdays = DateService.calculateWorkdaysInHesse(year)
        val workdays = DateService.calculateWorkdaysInHesseBetween(contingent.start, contingent.end, year)
        val workdaysWithoutVacationInContingent = workdays * (workdaysAssumption.toDouble() / yearlyWorkdays)
        val monthlyHours = ArrayList<Double>()
        monthlyHours.add(0.0)

        for (month in 1..12) {
            monthlyHours.add(getContingentHoursByYearAndMonth(year, month, contingent))
        }

        monthlyHours[0] = TimeDoubleService.convertDoubleToTimeDouble(workdayDailyHours * workdaysWithoutVacationInContingent)

        return monthlyHours
    }

    fun getContingentHoursByYearAndMonth(year: Int, month: Int, contingent: ContingentProjection): Double {
        if (!isContingentInYearMonth(year, month, contingent)) {
            return 0.0
        }

        // end date or the last day of the year when there is no end set
        val end = contingent.end ?: LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)
        val days = DateService.countDaysOfMonthAndYearBetweenStartAndEnd(year, month, contingent.start, end)
        return TimeDoubleService.convertDoubleToTimeDouble(days * (contingent.weeklyServiceHours / 7))
    }

    fun isContingentInYearMonth(year: Int, month: Int, contingent: ContingentProjection): Boolean {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return (contingent.start <= end) &&
                ((contingent.end?.let { it >= start } ?: true))
    }
}