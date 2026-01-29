package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.domains.absence.dtos.YearAbsenceDTO
import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.contingents.ContingentRepository
import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.contingents.projections.ContingentProjection
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.ceil

@Service
class ContingentService(
    private val contingentRepository: ContingentRepository,
    private val institutionService: InstitutionService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService,
    @param:Value("\${openfls.general.workdays.real}") private val workdaysReal: Long,
    @param:Value("\${openfls.general.workdays.assumption}") private val workdaysAssumption: Long,
) {

    @Transactional
    fun create(contingentDto: ContingentDto): ContingentDto {
        if (contingentDto.end != null && contingentDto.start >= contingentDto.end) {
            throw IllegalArgumentException("end before start")
        }

        val entity = Contingent.of(contingentDto)
        entity.employee = employeeService.getById(contingentDto.employeeId, true)
        entity.institution = institutionService.getEntityById(contingentDto.institutionId)

        return ContingentDto.from(contingentRepository.save(entity))
    }

    @Transactional
    fun update(contingentDto: ContingentDto): ContingentDto {
        if (!existsById(contingentDto.id))
            throw IllegalArgumentException("contingent not found")
        if (contingentDto.end != null && contingentDto.start >= contingentDto.end)
            throw IllegalArgumentException("end before start")

        val entity = contingentRepository.save(Contingent.of(contingentDto))
        return ContingentDto.from(entity)
    }

    @Transactional
    fun delete(id: Long) {
        if (!existsById(id))
            throw IllegalArgumentException("contingent not found")

        contingentRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getAll(): List<ContingentDto> {
        val entities = contingentRepository.findAll()
        return entities.map { ContingentDto.from(it) }
            .sortedBy { it.start }
    }

    @Transactional(readOnly = true)
    fun getAllByInstitutionAndYear(institutionId: Long, year: Int): List<ContingentProjection> {
        return contingentRepository.findByInstitutionIdAndStartAndEnd(
            institutionId,
            LocalDate.of(year, 1, 1),
            LocalDate.of(year, 12, 31)
        )
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): ContingentDto? {
        val entity = contingentRepository.findByIdOrNull(id)
        return entity?.let { ContingentDto.from(it) }
    }

    @Transactional(readOnly = true)
    fun getDtoById(id: Long): ContingentDto? {
        val entity = contingentRepository.findByIdOrNull(id)
        return if (entity == null) null else ContingentDto.from(entity)
    }

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean {
        return contingentRepository.existsById(id)
    }

    @Transactional(readOnly = true)
    fun getByEmployeeId(id: Long): List<ContingentDto> {
        val entities = contingentRepository.findAllByEmployeeId(id)
        return entities.map { ContingentDto.from(it) }
            .sortedBy { it.employeeId }
    }

    @Transactional(readOnly = true)
    fun getByInstitutionId(id: Long): List<ContingentDto> {
        val entities = contingentRepository.findAllByInstitutionId(id)
        return entities.map { ContingentDto.from(it) }
            .sortedBy { it.institutionId }
    }

    @Transactional(readOnly = true)
    fun canModifyContingent(contingentId: Long): Boolean {
        return try {
            // ADMIN
            if (accessService.isAdmin())
                return true

            val institutionId = getById(contingentId)?.institutionId ?: 0

            accessService.isLeader(accessService.getId(), institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun getContingentHoursByYear(year: Int, contingents: List<ContingentProjection>, absences: YearAbsenceDTO): List<Double> {
        val monthlyHours = ArrayList<Double>(List(13) { 0.0 })

        for (contingent in contingents) {
            val workdayDailyHours = TimeDoubleService.convertTimeDoubleToDouble(contingent.weeklyServiceHours) / 5
            val workdays =
                DateService.countDaysOfYearBetweenStartAndEnd(year, contingent.start, contingent.end) * 5.0 / 7.0
            val workdaysWithoutVacationInContingent = workdays * (workdaysAssumption.toDouble() / workdaysReal)
            for (month in 1..12) {
                monthlyHours[month] = TimeDoubleService.sumTimeDoubles(
                    monthlyHours[month],
                    getContingentHoursByYearAndMonth(year, month, contingent, absences)
                )
            }

            monthlyHours[0] = TimeDoubleService.sumTimeDoubles(
                monthlyHours[0],
                TimeDoubleService.convertDoubleToTimeDouble(workdayDailyHours * workdaysWithoutVacationInContingent)
            )
        }

        return monthlyHours
    }

    fun getContingentHoursByYear(year: Int, contingent: ContingentProjection, absences: YearAbsenceDTO): List<Double> {
        val workdayDailyHours = contingent.weeklyServiceHours / 5
        val workdays = DateService.calculateWorkdaysInHesseBetween(contingent.start, contingent.end, year)
        val absenceDays = countAbsenceDaysInContingentForYear(year, contingent, absences)
        val realWorkDays = workdays - absenceDays

        val monthlyHours = ArrayList<Double>()
        monthlyHours.add(0.0)

        for (month in 1..12) {
            monthlyHours.add(getContingentHoursByYearAndMonth(year, month, contingent, absences))
        }

        monthlyHours[0] =
            TimeDoubleService.convertDoubleToTimeDouble(realWorkDays * workdayDailyHours)

        return monthlyHours
    }

    fun getContingentHoursByYearAndMonth(year: Int, month: Int, contingent: ContingentProjection, absences: YearAbsenceDTO): Double {
        if (!isContingentInYearMonth(year, month, contingent)) {
            return 0.0
        }

        // end date or the last day of the year when there is no end set
        val end = contingent.end ?: LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)
        val workdays = DateService.countWorkDaysOfMonthAndYearBetweenStartAndEnd(year, month, contingent.start, end)
        val absenceDays = countAbsenceDaysInContingentForYearAndMonth(year, month, contingent, absences)
        return TimeDoubleService.convertDoubleToTimeDouble((workdays - absenceDays) * (contingent.weeklyServiceHours / 5))
    }

    private fun countAbsenceDaysInContingentForYearAndMonth(
        year: Int,
        month: Int,
        contingent: ContingentProjection,
        absences: YearAbsenceDTO
    ): Int {
        val employeeAbsences = absences.employeeAbsences.filter { absence ->
            absence.employeeId == contingent.employee.id
        }.flatMap { it.absenceDates }

        val absenceDaysInMonth = employeeAbsences
            .filter { absence ->
                absence.year == year && absence.monthValue == month && absence >= contingent.start &&
                        (contingent.end?.let { absence <= it } ?: true)
            }
            .count()

        return absenceDaysInMonth
    }

    private fun countAbsenceDaysInContingentForYear(
        year: Int,
        contingent: ContingentProjection,
        absences: YearAbsenceDTO
    ): Int {
        val employeeAbsences = absences.employeeAbsences.filter { absence ->
            absence.employeeId == contingent.employee.id
        }.flatMap { it.absenceDates }

        val absenceDaysInMonth = employeeAbsences
            .filter { absence ->
                absence.year == year && absence >= contingent.start &&
                        (contingent.end?.let { absence <= it } ?: true)
            }
            .count()

        return absenceDaysInMonth
    }

    fun isContingentInYearMonth(year: Int, month: Int, contingent: ContingentProjection): Boolean {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return (contingent.start <= end) &&
                ((contingent.end?.let { it >= start } ?: true))
    }

    fun getContingentMinutesForWorkday(
        date: LocalDate,
        contingents: List<ContingentDto>
    ): Double {
        if (!DateService.isWorkday(date)) {
            return 0.0
        }

        val contingentWeeklyHours = contingents.firstOrNull { contingent ->
            (contingent.start.isBefore(date) || contingent.start.isEqual(date)) &&
                    (contingent.end == null || contingent.end!!.isAfter(date) || contingent.end!!.isEqual(date))
        }

        return if (contingentWeeklyHours != null) {
            (contingentWeeklyHours.weeklyServiceHours * 60) / 5
        } else {
            0.0
        }
    }

    fun getContingentMinutesFor(
        start: LocalDate,
        end: LocalDate,
        contingents: List<ContingentDto>
    ): Int {
        var totalContingentMinutes = 0.0
        var currentDate = start

        while (!currentDate.isAfter(end)) {
            totalContingentMinutes += getContingentMinutesForWorkday(currentDate, contingents)
            currentDate = currentDate.plusDays(1)
        }

        return ceil(totalContingentMinutes).toInt()
    }
}
