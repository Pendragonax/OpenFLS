package de.vinz.openfls.domains.assistancePlans.services

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanAnalysisMonthCollectionDto
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanAnalysisMonthDto
import de.vinz.openfls.domains.assistancePlans.projections.AssistancePlanProjection
import de.vinz.openfls.exceptions.IllegalTimeException
import de.vinz.openfls.exceptions.UserNotAllowedException
import de.vinz.openfls.domains.goals.projections.GoalProjection
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import de.vinz.openfls.domains.services.ServiceService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class AssistancePlanAnalysisService(
        private val assistancePlanService: AssistancePlanService,
        private val serviceService: ServiceService,
        private val accessService: AccessService
) {
    fun getAnalysisByInstitutionAndSponsorAndHourTypeInMonth(year: Int,
                                                             month: Int,
                                                             institutionId: Long,
                                                             sponsorId: Long,
                                                             hourTypeId: Long): AssistancePlanAnalysisMonthCollectionDto {
        validateInstitutionAccess(institutionId)
        validateTime(year, month)

        if (sponsorId <= 0) {
            return getAnalysisByInstitutionAndHourTypeInMonth(year, month, institutionId, hourTypeId)
        }
        if (hourTypeId > 0 && institutionId <= 0) {
            return getAnalysisBySponsorAndHourTypeInMonth(year, month, sponsorId, hourTypeId)
        }

        val assistancePlans = assistancePlanService.getProjectionByYearMonthInstitutionIdSponsorId(year, month, institutionId, sponsorId)
        val analysis = getAnalysisByHourTypeIdInMonth(year, month, assistancePlans, hourTypeId)

        return createAssistancePlanAnalysisMonthCollectionDto(year, month, analysis)
    }

    fun getAnalysisByInstitutionAndHourTypeInMonth(year: Int,
                                                   month: Int,
                                                   institutionId: Long,
                                                   hourTypeId: Long): AssistancePlanAnalysisMonthCollectionDto {
        validateInstitutionAccess(institutionId)
        validateTime(year, month)

        if (hourTypeId <= 0 && institutionId <= 0) {
            return getAnalysisInMonth(year, month)
        }
        if (hourTypeId <= 0) {
            return getAnalysisByInstitutionInMonth(year, month, institutionId)
        }
        if (institutionId <= 0) {
            return getAnalysisByHourTypeInMonth(year, month, hourTypeId)
        }

        val assistancePlans = assistancePlanService.getProjectionByYearMonthInstitutionId(year, month, institutionId)
        val analysis = getAnalysisByHourTypeIdInMonth(year, month, assistancePlans, hourTypeId)

        return createAssistancePlanAnalysisMonthCollectionDto(year, month, analysis)
    }

    fun getAnalysisBySponsorAndHourTypeInMonth(year: Int,
                                               month: Int,
                                               sponsorId: Long,
                                               hourTypeId: Long): AssistancePlanAnalysisMonthCollectionDto {
        validateTime(year, month)

        if (hourTypeId <= 0 && sponsorId <= 0) {
            return getAnalysisInMonth(year, month)
        }
        if (hourTypeId <= 0) {
            return getAnalysisBySponsorInMonth(year, month, sponsorId)
        }
        if (sponsorId <= 0) {
            return getAnalysisByHourTypeInMonth(year, month, hourTypeId)
        }

        val assistancePlans = assistancePlanService.getProjectionByYearMonthSponsorId(year, month, sponsorId)
        val analysis = getAnalysisByHourTypeIdInMonth(year, month, assistancePlans, hourTypeId)

        return createAssistancePlanAnalysisMonthCollectionDto(year, month, analysis)
    }

    fun getAnalysisByHourTypeInMonth(year: Int,
                                     month: Int,
                                     hourTypeId: Long): AssistancePlanAnalysisMonthCollectionDto {
        validateAdmin()
        validateTime(year, month)

        if (hourTypeId <= 0) {
            return getAnalysisInMonth(year, month)
        }

        val assistancePlans = assistancePlanService.getProjectionByYearMonth(year, month)
        val analysis = getAnalysisByHourTypeIdInMonth(year, month, assistancePlans, hourTypeId)

        return createAssistancePlanAnalysisMonthCollectionDto(year, month, analysis)
    }

    fun getAnalysisBySponsorInMonth(year: Int,
                                    month: Int,
                                    sponsorId: Long): AssistancePlanAnalysisMonthCollectionDto {
        if (sponsorId <= 0) {
            return getAnalysisInMonth(year, month)
        }

        val assistancePlans = assistancePlanService.getProjectionByYearMonthSponsorId(year, month, sponsorId)
        val analysis = getAnalysisInMonth(year, month, assistancePlans)

        return createAssistancePlanAnalysisMonthCollectionDto(year, month, analysis)
    }

    fun getAnalysisByInstitutionInMonth(year: Int,
                                        month: Int,
                                        institutionId: Long): AssistancePlanAnalysisMonthCollectionDto {
        if (institutionId <= 0) {
            return getAnalysisInMonth(year, month)
        }

        val assistancePlans = assistancePlanService.getProjectionByYearMonthInstitutionId(year, month, institutionId)
        val analysis = getAnalysisInMonth(year, month, assistancePlans)

        return createAssistancePlanAnalysisMonthCollectionDto(year, month, analysis)
    }

    fun getAnalysisInMonth(year: Int,
                           month: Int): AssistancePlanAnalysisMonthCollectionDto {
        val assistancePlans = assistancePlanService.getProjectionByYearMonth(year, month)
        val analysis = getAnalysisInMonth(year, month, assistancePlans)

        return createAssistancePlanAnalysisMonthCollectionDto(year, month, analysis)
    }

    private fun createAssistancePlanAnalysisMonthCollectionDto(
            year: Int,
            month: Int,
            assistancePlanAnalysis: List<AssistancePlanAnalysisMonthDto>): AssistancePlanAnalysisMonthCollectionDto {
        val approvedHours =
                if (assistancePlanAnalysis.isNotEmpty())
                    assistancePlanAnalysis.map { it.approvedHours }.reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }
                else
                    0.0
        val executedHours =
                if (assistancePlanAnalysis.isNotEmpty())
                    assistancePlanAnalysis.map { it.executedHours }.reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }
                else
                    0.0
        val executedPercent =
                if (approvedHours > 0)
                    TimeDoubleService.roundDoubleToTwoDigits(executedHours * 100 / approvedHours)
                else
                    0.0
        val missingHours = TimeDoubleService.diffTimeDoubles(approvedHours, executedHours)

        return AssistancePlanAnalysisMonthCollectionDto(
                year = year,
                month = month,
                approvedHours = approvedHours,
                executedHours = executedHours,
                executedPercent = executedPercent,
                missingHours = missingHours,
                assistancePlanAnalysis = assistancePlanAnalysis.sortedBy { it.clientLastName })
    }

    fun getAnalysisByHourTypeIdInMonth(year: Int,
                                       month: Int,
                                       assistancePlans: List<AssistancePlanProjection>,
                                       hourTypeId: Long): List<AssistancePlanAnalysisMonthDto> {
        return assistancePlans
                .map { getAnalysisByHourTypeIdInMonth(year, month, it, hourTypeId) }
                .filter { it.approvedHours > 0 }
    }

    fun getAnalysisByHourTypeIdInMonth(year: Int,
                                       month: Int,
                                       assistancePlan: AssistancePlanProjection,
                                       hourTypeId: Long): AssistancePlanAnalysisMonthDto {
        val approvedHours =
                if (existsAssistancePlanHours(assistancePlan)) {
                    getApprovedAssistancePlanHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
                } else {
                    getApprovedGoalHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
                }
        val executedHours = getExecutedHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
        val executedPercent =
                if (approvedHours > 0)
                    TimeDoubleService.roundDoubleToTwoDigits(executedHours * 100 / approvedHours)
                else
                    0.0
        val missingHours = TimeDoubleService.diffTimeDoubles(approvedHours, executedHours)

        return getAnalysisInMonth(
                year,
                month,
                assistancePlan,
                approvedHours,
                executedHours,
                executedPercent,
                missingHours)
    }

    fun getAnalysisInMonth(year: Int,
                           month: Int,
                           assistancePlans: List<AssistancePlanProjection>): List<AssistancePlanAnalysisMonthDto> {
        return assistancePlans
                .map { getAnalysisInMonth(year, month, it) }
                .filter { it.approvedHours > 0 }
    }

    fun getAnalysisInMonth(year: Int,
                           month: Int,
                           assistancePlan: AssistancePlanProjection): AssistancePlanAnalysisMonthDto {
        val approvedHours =
                if (existsAssistancePlanHours(assistancePlan)) {
                    getApprovedAssistancePlanHoursInMonth(year, month, assistancePlan)
                } else {
                    getApprovedGoalHoursInMonth(year, month, assistancePlan)
                }
        val executedHours = getExecutedHoursInMonth(year, month, assistancePlan)
        val executedPercent =
                if (approvedHours > 0)
                    TimeDoubleService.roundDoubleToTwoDigits(executedHours * 100 / approvedHours)
                else
                    0.0
        val missingHours = TimeDoubleService.diffTimeDoubles(approvedHours, executedHours)

        return getAnalysisInMonth(
                year,
                month,
                assistancePlan,
                approvedHours,
                executedHours,
                executedPercent,
                missingHours)
    }

    fun getAnalysisInMonth(year: Int,
                           month: Int,
                           assistancePlan: AssistancePlanProjection,
                           approvedHours: Double,
                           executedHours: Double,
                           executedPercent: Double,
                           missingHours: Double): AssistancePlanAnalysisMonthDto {
        return AssistancePlanAnalysisMonthDto(
                assistancePlanId = assistancePlan.id,
                start = assistancePlan.start,
                end = assistancePlan.end,
                clientFirstName = assistancePlan.client.firstName,
                clientLastName = assistancePlan.client.lastName,
                year = year,
                month = month,
                approvedHours = approvedHours,
                executedHours = executedHours,
                executedPercent = executedPercent,
                missingHours = missingHours)
    }

    fun getApprovedAssistancePlanHoursInYear(year: Int, assistancePlans: List<AssistancePlanProjection>): List<Double> {
        val monthlyHours = ArrayList<Double>(List(13) { 0.0 })

        for (assistancePlan in assistancePlans) {
            for (month in 1..12) {
                monthlyHours[month] = TimeDoubleService.sumTimeDoubles(
                        monthlyHours[month],
                        getApprovedAssistancePlanHoursInMonth(year, month, assistancePlan))
            }
        }

        monthlyHours[0] = monthlyHours.reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }

        return monthlyHours
    }

    fun getApprovedAssistancePlanHoursByHourTypeInYear(year: Int,
                                                       assistancePlans: List<AssistancePlanProjection>,
                                                       hourTypeId: Long): List<Double> {
        val monthlyHours = ArrayList<Double>(List(13) { 0.0 })

        for (assistancePlan in assistancePlans) {
            for (month in 1..12) {
                monthlyHours[month] = TimeDoubleService.sumTimeDoubles(
                        monthlyHours[month],
                        getApprovedAssistancePlanHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId))
            }
        }

        monthlyHours[0] = monthlyHours.reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }

        return monthlyHours
    }

    fun getApprovedAssistancePlanHoursInYear(year: Int, assistancePlan: AssistancePlanProjection): List<Double> {
        val monthlyHours = ArrayList<Double>(List(13) { 0.0 })

        for (month in 1..12) {
            monthlyHours[month] = getApprovedAssistancePlanHoursInMonth(year, month, assistancePlan)
        }
        monthlyHours[0] = monthlyHours.reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }

        return monthlyHours
    }

    fun getApprovedAssistancePlanHoursByHourTypeIdInYear(year: Int,
                                                         assistancePlan: AssistancePlanProjection,
                                                         hourTypeId: Long): List<Double> {
        val monthlyHours = ArrayList<Double>(List(13) { 0.0 })

        for (month in 1..12) {
            monthlyHours[month] =
                    getApprovedAssistancePlanHoursByHourTypeIdInMonth(year, month, assistancePlan, hourTypeId)
        }
        monthlyHours[0] = monthlyHours.reduce { acc, d -> TimeDoubleService.sumTimeDoubles(acc, d) }

        return monthlyHours
    }

    fun getApprovedAssistancePlanHoursInMonth(year: Int,
                                              month: Int,
                                              assistancePlan: AssistancePlanProjection): Double {
        val dailyHours = assistancePlan.hours.sumOf { it.weeklyHours } / 7
        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        return TimeDoubleService.convertDoubleToTimeDouble(
                dailyHours * days)
    }

    fun getApprovedAssistancePlanHoursByHourTypeIdInMonth(year: Int,
                                                          month: Int,
                                                          assistancePlan: AssistancePlanProjection,
                                                          hourTypeId: Long): Double {
        val hours = assistancePlan.hours.filter { it.hourType.id == hourTypeId }
        val dailyHours = hours.sumOf { it.weeklyHours } / 7
        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        return TimeDoubleService.convertDoubleToTimeDouble(dailyHours * days)
    }

    fun getApprovedGoalHoursByHourTypeIdInMonth(year: Int,
                                                month: Int,
                                                assistancePlan: AssistancePlanProjection,
                                                hourTypeId: Long): Double {
        if (assistancePlan.goals.isEmpty()) {
            return 0.0
        }

        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        val approvedHours = sumGoalsHoursByHourTypeId(assistancePlan.goals, days, hourTypeId)
        return TimeDoubleService.convertDoubleToTimeDouble(approvedHours)
    }

    fun getApprovedGoalHoursInMonth(year: Int,
                                    month: Int,
                                    assistancePlan: AssistancePlanProjection): Double {
        if (assistancePlan.goals.isEmpty()) {
            return 0.0
        }

        val days = countMatchingDaysInMonth(year, month, assistancePlan)
        val approvedHours = sumGoalsHours(assistancePlan.goals, days)
        return TimeDoubleService.convertDoubleToTimeDouble(approvedHours)
    }

    fun getExecutedHoursInMonth(year: Int,
                                month: Int,
                                assistancePlan: AssistancePlanProjection): Double {
        val services = serviceService.getAllByAssistancePlanIdAndYearAndMonth(
                assistancePlanId = assistancePlan.id,
                year = year,
                month = month)
        val hours = services.sumOf { it.minutes.toDouble() } / 60

        return TimeDoubleService.convertDoubleToTimeDouble(hours)
    }

    fun getExecutedHoursByHourTypeIdInMonth(year: Int,
                                            month: Int,
                                            assistancePlan: AssistancePlanProjection,
                                            hourTypeId: Long): Double {
        val services = serviceService.getAllByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
                assistancePlanId = assistancePlan.id,
                hourTypeId = hourTypeId,
                year = year,
                month = month)
        val hours = services.sumOf { it.minutes.toDouble() } / 60

        return TimeDoubleService.convertDoubleToTimeDouble(hours)
    }

    private fun countMatchingDaysInMonth(year: Int, month: Int, assistancePlan: AssistancePlanProjection): Int {
        if (!isInYearMonth(year, month, assistancePlan)) {
            return 0
        }

        return DateService.countDaysOfMonthAndYearBetweenStartAndEnd(
                year,
                month,
                assistancePlan.start,
                assistancePlan.end)
    }

    private fun sumGoalsHours(goals: List<GoalProjection>, days: Int): Double {
        return goals.sumOf { sumGoalHours(it, days) }
    }

    private fun sumGoalsHoursByHourTypeId(goals: List<GoalProjection>, days: Int, hourTypeId: Long): Double {
        return goals.sumOf { sumGoalHoursByHourTypeId(it, days, hourTypeId) }
    }

    private fun sumGoalHours(goal: GoalProjection, days: Int): Double {
        if (goal.hours.isEmpty()) {
            return 0.0
        }

        return goal.hours.sumOf { hour -> (hour.weeklyHours / 7) * days }
    }

    private fun sumGoalHoursByHourTypeId(goal: GoalProjection, days: Int, hourTypeId: Long): Double {
        if (goal.hours.isEmpty()) {
            return 0.0
        }

        val hours = goal.hours.filter { it.hourType.id == hourTypeId }
        return hours.sumOf { hour -> (hour.weeklyHours / 7) * days }
    }

    private fun isInYearMonth(year: Int, month: Int, assistancePlan: AssistancePlanProjection): Boolean {
        val start = LocalDate.of(year, month, 1)
        val end = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        return assistancePlan.start <= end && assistancePlan.end >= start
    }

    private fun existsAssistancePlanHours(assistancePlan: AssistancePlanProjection): Boolean {
        return assistancePlan.hours.isNotEmpty()
    }

    @Throws(IllegalTimeException::class)
    private fun validateTime(year: Int, month: Int?) {
        if (year < 0) {
            throw IllegalTimeException("Year is below 0")
        }

        month?.let {
            if (it <= 0 || it > 12) {
                throw IllegalTimeException("Month is below 0 or higher than 12")
            }
        }
    }

    @Throws(UserNotAllowedException::class)
    private fun validateInstitutionAccess(institutionId: Long?) {
        if (institutionId == null && !accessService.isAdmin()) {
            throw UserNotAllowedException()
        }
        if (institutionId != null && !accessService.canReadEntries(institutionId)) {
            throw UserNotAllowedException()
        }
    }

    @Throws(UserNotAllowedException::class)
    private fun validateAdmin() {
        if (!accessService.isAdmin()) {
            throw UserNotAllowedException()
        }
    }
}
