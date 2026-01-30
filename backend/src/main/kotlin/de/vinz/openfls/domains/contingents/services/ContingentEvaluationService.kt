package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.domains.absence.AbsenceService
import de.vinz.openfls.domains.absence.dtos.YearAbsenceDTO
import de.vinz.openfls.domains.contingents.dtos.ContingentEvaluationDto
import de.vinz.openfls.domains.contingents.dtos.EmployeeContingentEvaluationDto
import de.vinz.openfls.domains.contingents.projections.ContingentProjection
import de.vinz.openfls.domains.services.projections.ContingentEvaluationServiceProjection
import de.vinz.openfls.domains.services.services.ServiceService
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ContingentEvaluationService(
    private val contingentService: ContingentService,
    private val serviceService: ServiceService,
    private val absenceService: AbsenceService
) {

    @Transactional(readOnly = true)
    fun generateContingentEvaluationFor(year: Int, institutionId: Long): ContingentEvaluationDto {
        val services = serviceService.getContingentEvaluationServiceDTOsBy(institutionId, year)
        val contingents = contingentService.getAllByInstitutionAndYear(institutionId, year)
        val yearlyAbsences = absenceService.getAllByYear(year)
        val employeeContingentEvaluations =
            getEmployeeContingentEvaluations(year, contingents, services, yearlyAbsences)

        return ContingentEvaluationDto(institutionId, employeeContingentEvaluations)
    }

    fun getEmployeeContingentEvaluations(
        year: Int,
        contingents: List<ContingentProjection>,
        services: List<ContingentEvaluationServiceProjection>,
        yearlyAbsences: YearAbsenceDTO
    ): List<EmployeeContingentEvaluationDto> {
        val employeeEvaluations = ArrayList<EmployeeContingentEvaluationDto>()

        for (contingent in contingents) {
            val contingentHours = contingentService.getContingentHoursByYear(year, contingent, yearlyAbsences)

            // employee got NO evaluation
            if (employeeEvaluations.none { it.employeeId == contingent.employee.id }) {
                val executedEmployeeHours = getExecutedHoursByYearAndEmployee(year, contingent.employee.id, services, yearlyAbsences)
                val summedExecutedPercent = getSummedExecutedPercent(contingentHours, executedEmployeeHours)
                val executedPercent = getExecutedPercent(contingentHours, executedEmployeeHours)
                val missingHours = getMissingHours(contingentHours, executedEmployeeHours)

                employeeEvaluations.add(
                    EmployeeContingentEvaluationDto(
                        employeeId = contingent.employee.id,
                        lastname = contingent.employee.lastname,
                        firstname = contingent.employee.firstname,
                        contingentHours = contingentHours,
                        executedHours = executedEmployeeHours,
                        executedPercent = executedPercent,
                        summedExecutedPercent = summedExecutedPercent,
                        missingHours = missingHours
                    )
                )
            }
            // employee got an evaluation
            else {
                val contingentEmployeeEvaluation: EmployeeContingentEvaluationDto =
                    employeeEvaluations.first { it.employeeId == contingent.employee.id }
                employeeEvaluations.remove(contingentEmployeeEvaluation)

                val employeeContingentHours = contingentEmployeeEvaluation.contingentHours
                    .mapIndexed { index, value -> TimeDoubleService.sumTimeDoubles(value, contingentHours[index]) }
                val employeeExecutedPercent = getExecutedPercent(
                    employeeContingentHours, contingentEmployeeEvaluation.executedHours
                )
                val employeeSummedExecutedPercent = getSummedExecutedPercent(
                    employeeContingentHours, contingentEmployeeEvaluation.executedHours
                )
                val employeeMissingHours = getMissingHours(
                    employeeContingentHours, contingentEmployeeEvaluation.executedHours
                )

                employeeEvaluations.add(
                    EmployeeContingentEvaluationDto(
                        employeeId = contingent.employee.id,
                        lastname = contingent.employee.lastname,
                        firstname = contingent.employee.firstname,
                        contingentHours = employeeContingentHours,
                        executedHours = contingentEmployeeEvaluation.executedHours,
                        executedPercent = employeeExecutedPercent,
                        summedExecutedPercent = employeeSummedExecutedPercent,
                        missingHours = employeeMissingHours
                    )
                )
            }
        }

        return employeeEvaluations.sortedBy { it.lastname }
    }

    fun getExecutedHoursByYearAndEmployee(
        year: Int,
        employeeId: Long,
        services: List<ContingentEvaluationServiceProjection>,
        yearlyAbsences: YearAbsenceDTO
    ): List<Double> {
        val employeeServices = services.filter { it.employeeId == employeeId }
        val monthlyHours = List(13) { 0 }.toMutableList()

        for (month in 1..12) {
            for (service in employeeServices) {
                if (isAbsent(employeeId, service.start, yearlyAbsences)) {
                    continue
                }

                if ((service.start.year == year) && (service.start.monthValue == month)) {
                    monthlyHours[month] += service.minutes
                    monthlyHours[0] += service.minutes
                }
            }
        }

        return monthlyHours.map { TimeDoubleService.convertMinutesToTimeDouble(it) }
    }

    fun getMissingHours(contingentHours: List<Double>, executedHours: List<Double>): List<Double> {
        return contingentHours
            .mapIndexed { index, hours ->
                if (hours <= 0) 0.0 else TimeDoubleService.diffTimeDoubles(
                    hours,
                    executedHours[index]
                )
            }
            .map { TimeDoubleService.roundDoubleToTwoDigits(it) }
            .toList()
    }

    fun getExecutedPercent(contingentHours: List<Double>, executedHours: List<Double>): List<Double> {
        return contingentHours
            .mapIndexed { index, hours -> calculatePercent(executedHours[index], hours) }
            .map { TimeDoubleService.roundDoubleToTwoDigits(it) }
            .toList()
    }

    private fun calculatePercent(timeDouble: Double, timeDoubleOf: Double): Double {
        if (timeDoubleOf <= 0) {
            return 0.0
        }

        return TimeDoubleService.convertTimeDoubleToDouble(timeDouble) * 100 /
                TimeDoubleService.convertTimeDoubleToDouble(timeDoubleOf)
    }

    fun getSummedExecutedPercent(contingentHours: List<Double>, executedHours: List<Double>): List<Double> {
        val contingentsHoursWithoutAll = contingentHours.drop(1)
            .map { TimeDoubleService.convertDoubleToTimeDouble(TimeDoubleService.convertTimeDoubleToDouble(it) * 0.777) }
        val executedHoursWithoutAll = executedHours.drop(1)
        val monthlyPercent = contingentsHoursWithoutAll.indices
            .map { index -> calculateSummedPercent(executedHoursWithoutAll, contingentsHoursWithoutAll, index) }
            .map { TimeDoubleService.roundDoubleToTwoDigits(it) }
        val allPercent =
            TimeDoubleService.roundDoubleToTwoDigits(calculatePercent(executedHours.first(), contingentHours.first()))

        return listOf(allPercent) + monthlyPercent
    }

    private fun calculateSummedPercent(timeDoubles: List<Double>, timeDoublesOf: List<Double>, index: Int): Double {
        val summedTimeDoubles = timeDoubles
            .take(index + 1)
            .reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }
        val summedTimeDoublesOf = timeDoublesOf
            .take(index + 1)
            .reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }

        if (summedTimeDoublesOf <= 0) {
            return 0.0
        }

        return TimeDoubleService.convertTimeDoubleToDouble(summedTimeDoubles) * 100 /
                TimeDoubleService.convertTimeDoubleToDouble(summedTimeDoublesOf)
    }

    private fun isAbsent(employeeId: Long, date: LocalDateTime, yearlyAbsences: YearAbsenceDTO): Boolean {
        val absencesForEmployee = yearlyAbsences.employeeAbsences.filter { it.employeeId == employeeId }
        return absencesForEmployee.any { it.absenceDates.contains(date.toLocalDate()) }
    }
}
