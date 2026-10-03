package de.vinz.openfls.domains.hourReports.service

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanEditDto
import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourCorridors.service.HourCorridorService
import de.vinz.openfls.domains.hourReports.dto.HourReportRowResponse
import de.vinz.openfls.domains.hourReports.dto.HourReportResult
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
@Transactional(readOnly = true)
class HourReportService(
    private val accessService: AccessService,
    private val serviceService: ServiceService,
    private val assistancePlanService: AssistancePlanService,
    private val hourCorridorService: HourCorridorService,
    private val clientService: ClientService
) {

    private val monthCount = 12

    fun getExecutedHoursReport(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): HourReportResult {
        validateAccess(areaId)?.let { return it }
        validateYearMonth(year, month)?.let { return it }

        val services = serviceService.getAllEntitiesByYearAndMonthAndHourTypeIdAndInstitutionIdAndSponsorId(
            year, month, hourTypeId, areaId, sponsorId
        )
        val context = loadReportContext(year, month, areaId, sponsorId)

        return HourReportResult.Success(buildExecutedHoursReport(services, context, year, month))
    }

    fun getExecutedHoursGroupServiceReport(
        year: Int,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): HourReportResult {
        validateAccess(areaId)?.let { return it }
        validateYearMonth(year, null)?.let { return it }

        val services = serviceService
            .getAllEntitiesByYearAndMonthAndHourTypeIdAndInstitutionIdAndSponsorId(year, null, hourTypeId, areaId, sponsorId)
            .filter { it.groupService }
        val context = loadReportContext(year, null, areaId, sponsorId)

        return HourReportResult.Success(buildExecutedHoursReport(services, context, year, null))
    }

    fun getApprovedHoursReport(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): HourReportResult {
        validateAccess(areaId)?.let { return it }
        validateYearMonth(year, month)?.let { return it }

        val context = loadReportContext(year, month, areaId, sponsorId)
        val rows = if (month != null) {
            buildApprovedHoursMonthly(context.assistancePlanDtos, context.clientDtos, hourTypeId, year, month)
        } else {
            buildApprovedHoursYearly(context.assistancePlanDtos, context.clientDtos, hourTypeId, year)
        }

        return HourReportResult.Success(rows)
    }

    fun getDifferenceHoursReport(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): HourReportResult {
        validateAccess(areaId)?.let { return it }
        validateYearMonth(year, month)?.let { return it }

        val services = serviceService.getAllEntitiesByYearAndMonthAndHourTypeIdAndInstitutionIdAndSponsorId(
            year, month, hourTypeId, areaId, sponsorId
        )
        val context = loadReportContext(year, month, areaId, sponsorId)
        val rows = if (month != null) {
            buildDifferenceHoursMonthly(services, context.assistancePlanDtos, context.clientDtos, hourTypeId, year, month)
        } else {
            buildDifferenceHoursYearly(services, context.assistancePlanDtos, context.clientDtos, hourTypeId, year)
        }

        return HourReportResult.Success(rows)
    }

    private fun buildExecutedHoursReport(
        services: List<de.vinz.openfls.domains.services.entity.Service>,
        context: HourReportContext,
        year: Int,
        month: Int?
    ): List<HourReportRowResponse> {
        return if (month != null) {
            buildExecutedHoursMonthly(services, context.assistancePlanDtos, context.clientDtos, year, month)
        } else {
            buildExecutedHoursYearly(services, context.assistancePlanDtos, context.clientDtos, year)
        }
    }

    private fun buildApprovedHoursMonthly(
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientSimpleDtos: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        month: Int,
        toTimeDouble: Boolean = true
    ): List<HourReportRowResponse> {

        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val rows =
            buildEmptyReportRows(assistancePlanDtos, clientSimpleDtos, daysInMonth)

        // Set date range for the aggregated "all" DTO at the beginning of the list
        val totalRow = rows.first().apply {
            assistancePlanDto.start = LocalDate.of(year, month, 1)
            assistancePlanDto.end = assistancePlanDto.start.plusMonths(1).minusDays(1)
        }

        // Populate values for each assistance plan
        val hourCorridors = loadHourCorridors(assistancePlanDtos)
        assistancePlanDtos.forEach { planDto ->
            val row =
                rows.find { it.assistancePlanDto.id == planDto.id } ?: return@forEach
            val hoursPerDay = getDailyHoursOfAssistancePlanByHourType(planDto, hourTypeId, hourCorridors)

            (1..daysInMonth).forEach { day ->
                val date = LocalDate.of(year, month, day)
                if (DateService.isDateInAssistancePlan(date, planDto.start, planDto.end)) {
                    row.values[0] += hoursPerDay
                    row.values[day] = hoursPerDay
                    totalRow.values[0] += hoursPerDay
                    totalRow.values[day] += hoursPerDay
                } else {
                    row.values[day] = 0.0
                }
            }
        }

        // Optional conversion to minute double values
        if (toTimeDouble) convertDoubleToMinuteDouble(rows)

        return rows
    }

    private fun buildApprovedHoursYearly(
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientSimpleDtos: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        toTimeDouble: Boolean = true
    ): List<HourReportRowResponse> {
        val rows =
            buildEmptyReportRows(assistancePlanDtos, clientSimpleDtos, monthCount)
        val totalRow = rows[0]

        val hourCorridors = loadHourCorridors(assistancePlanDtos)
        assistancePlanDtos.forEach { assistancePlanDto ->
            val row =
                rows.find { it.assistancePlanDto.id == assistancePlanDto.id }
            val hoursPerDay = getDailyHoursOfAssistancePlanByHourType(assistancePlanDto, hourTypeId, hourCorridors)

            if (row != null) {
                for (i in 1..monthCount) {
                    val daysInMonth = DateService.countDaysOfAssistancePlan(year, i, assistancePlanDto.start, assistancePlanDto.end)
                    val hoursPerMonth = hoursPerDay * daysInMonth
                    row.values[0] += hoursPerMonth
                    row.values[i] = hoursPerMonth

                    totalRow.values[0] += hoursPerMonth
                    totalRow.values[i] += hoursPerMonth
                }
            }
        }

        // convert to minute double
        if (toTimeDouble) convertDoubleToMinuteDouble(rows)

        return rows
    }

    private fun buildExecutedHoursYearly(
        services: List<de.vinz.openfls.domains.services.entity.Service>,
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientDtos: List<ClientSimpleDto>,
        year: Int,
        toTimeDouble: Boolean = true
    ): List<HourReportRowResponse> {
        val rows =
            buildEmptyReportRows(assistancePlanDtos, clientDtos, monthCount)
        val totalRow = rows[0]
        totalRow.assistancePlanDto.start = LocalDate.of(year, 1, 1)
        totalRow.assistancePlanDto.end = LocalDate.of(year, 12, 31)

        services.forEach { service ->
            val row =
                rows.find { it.assistancePlanDto.id == service.assistancePlan?.id }
            if (row != null) {
                val month = service.start.monthValue
                row.values[0] += service.minutes.toDouble()
                row.values[month] += service.minutes.toDouble()

                totalRow.values[0] += service.minutes.toDouble()
                totalRow.values[month] += service.minutes.toDouble()
            }
        }

        // convert from minutes to hours
        convertMinutesValuesToHourValues(rows, toTimeDouble)

        return rows
    }

    private fun buildExecutedHoursMonthly(
        services: List<de.vinz.openfls.domains.services.entity.Service>,
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientDtos: List<ClientSimpleDto>,
        year: Int,
        month: Int,
        toTimeDouble: Boolean = true
    ): List<HourReportRowResponse> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val rows =
            buildEmptyReportRows(assistancePlanDtos, clientDtos, daysInMonth)
        val totalRow = rows[0]

        services.forEach { service ->
            val row =
                rows.find { it.assistancePlanDto.id == service.assistancePlan?.id }
            if (row != null) {
                val day = service.start.dayOfMonth
                row.values[0] += service.minutes.toDouble()
                row.values[day] += service.minutes.toDouble()

                totalRow.values[0] += service.minutes.toDouble()
                totalRow.values[day] += service.minutes.toDouble()
            }
        }

        // convert from minutes to hours
        convertMinutesValuesToHourValues(rows, toTimeDouble)

        return rows
    }

    private fun buildDifferenceHoursYearly(
        services: List<de.vinz.openfls.domains.services.entity.Service>,
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientSimpleDtos: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        toTimeDouble: Boolean = true
    ): List<HourReportRowResponse> {
        val approvedRows = buildApprovedHoursYearly(
            assistancePlanDtos, clientSimpleDtos, hourTypeId, year, false
        )
        val executedRows = buildExecutedHoursYearly(
            services, assistancePlanDtos, clientSimpleDtos, year, false
        )

        return subtractApprovedFromExecutedReport(
            executedRows,
            approvedRows,
            year,
            null,
            hourTypeId,
            toTimeDouble
        )
    }

    private fun buildDifferenceHoursMonthly(
        services: List<de.vinz.openfls.domains.services.entity.Service>,
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientSimpleDtos: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        month: Int,
        toTimeDouble: Boolean = true
    ): List<HourReportRowResponse> {
        val approvedRows = buildApprovedHoursMonthly(
            assistancePlanDtos, clientSimpleDtos, hourTypeId, year, month, false
        )
        val executedRows = buildExecutedHoursMonthly(
            services, assistancePlanDtos, clientSimpleDtos, year, month, false
        )

        return subtractApprovedFromExecutedReport(
            executedRows,
            approvedRows,
            year,
            month,
            hourTypeId,
            toTimeDouble
        )
    }

    private fun subtractApprovedFromExecutedReport(
        executedRows: List<HourReportRowResponse>,
        approvedRows: List<HourReportRowResponse>,
        year: Int,
        month: Int?,
        hourTypeId: Long?,
        toTimeDouble: Boolean
    ): List<HourReportRowResponse> {
        val hourCorridors = loadHourCorridors(executedRows.map { it.assistancePlanDto })
        executedRows.forEach { executedRow ->
            val approvedRow =
                approvedRows.find { it.assistancePlanDto.id == executedRow.assistancePlanDto.id }

            if (approvedRow != null) {
                if (isCorridor(executedRow.assistancePlanDto)) {
                    val corridor = hourCorridors[executedRow.assistancePlanDto.hourCorridorId]
                    if (corridor != null && (hourTypeId == null || (corridor.hourType?.id ?: 0) == hourTypeId)) {
                        for (i in 1 until executedRow.values.size) {
                            val daysInPeriod = if (month != null) {
                                1
                            } else {
                                DateService.countDaysOfAssistancePlan(year, i, executedRow.assistancePlanDto.start, executedRow.assistancePlanDto.end).toInt()
                            }
                            val approvedHoursFrom =
                                corridorApprovedHoursForDays(daysInPeriod, corridor.weeklyMinutesFrom)
                            val approvedHoursTo =
                                corridorApprovedHoursForDays(daysInPeriod, corridor.weeklyMinutesTill)
                            executedRow.values[i] =
                                calculateCorridorDifference(executedRow.values[i], approvedHoursFrom, approvedHoursTo)
                        }
                        executedRow.values[0] = executedRow.values.drop(1).sum()
                    } else {
                        for (i in 0 until executedRow.values.size) {
                            executedRow.values[i] -= approvedRow.values[i]
                        }
                    }
                } else {
                    for (i in 0 until executedRow.values.size) {
                        executedRow.values[i] -= approvedRow.values[i]
                    }
                }
            }
        }

        // convert to minute double
        if (toTimeDouble) convertDoubleToMinuteDouble(executedRows)

        return executedRows
    }

    private fun convertDoubleToMinuteDouble(rows: List<HourReportRowResponse>) {
        rows.forEach { row ->
            row.values[0] = 0.0
            for (i in 1 until row.values.size) {
                row.values[i] =
                    TimeDoubleService.convertDoubleToTimeDouble(row.values[i])
                row.values[0] =
                    TimeDoubleService.sumTimeDoubles(
                        row.values[0],
                        row.values[i]
                    )
            }
        }
    }

    private fun convertMinutesValuesToHourValues(
        rows: List<HourReportRowResponse>,
        toTimeDouble: Boolean = true
    ) {
        rows.forEach { row ->
            for (i in 0 until row.values.size) {
                row.values[i] =
                    if (toTimeDouble) TimeDoubleService.convertDoubleToTimeDouble(row.values[i] / 60)
                    else TimeDoubleService.roundDoubleToTwoDigits(row.values[i] / 60)
            }
        }
    }

    private data class HourReportContext(
        val assistancePlanDtos: List<AssistancePlanEditDto>,
        val clientDtos: List<ClientSimpleDto>
    )

    private fun loadReportContext(year: Int, month: Int?, institutionId: Long?, sponsorId: Long?): HourReportContext {
        val clientDtos = clientService.getAllClientSimpleDto(includeArchived = true)
        val assistancePlanDtos = getAssistancePlansForYearMonth(year, month, institutionId, sponsorId)
        return HourReportContext(assistancePlanDtos, clientDtos)
    }

    private fun getAssistancePlansForYearMonth(
        year: Int,
        month: Int?,
        institutionId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanEditDto> {
        val plans = assistancePlanService.getAllEditDtosByYearAndInstitutionIdAndSponsorId(year, institutionId, sponsorId)

        return if (month != null) {
            plans.filter {
                DateService.containsStartAndEndASpecificYearMonth(it.start, it.end, YearMonth.of(year, month))
            }
        } else {
            plans
        }
    }

    private fun buildEmptyReportRows(
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientDtos: List<ClientSimpleDto>,
        valuesCount: Int
    ): MutableList<HourReportRowResponse> {

        val allClient = ClientSimpleDto().apply { lastName = "Gesamt" }
        val defaultValuesArray = DoubleArray(valuesCount + 1) { 0.0 }

        val result = assistancePlanDtos.map { plan ->
            val client = clientDtos.find { it.id == plan.clientId }
                ?: throw IllegalArgumentException("Client with ID ${plan.clientId} not found")

            HourReportRowResponse(plan, copyClient(client), defaultValuesArray.copyOf())
        }.sortedBy { it.clientDto.lastName }
            .toMutableList()

        result.add(0, HourReportRowResponse(AssistancePlanEditDto(), allClient, defaultValuesArray))

        return result
    }

    private fun getDailyHoursOfAssistancePlanByHourType(
        assistancePlanDto: AssistancePlanEditDto,
        hourTypeId: Long?,
        hourCorridors: Map<Long, HourCorridor>
    ): Double =
        if (hourTypeId == null) {
            0.0
        } else if (isCorridor(assistancePlanDto)) {
            val corridor = hourCorridors[assistancePlanDto.hourCorridorId] ?: return 0.0
            if ((corridor.hourType?.id ?: 0) != hourTypeId) {
                0.0
            } else {
                val weeklyMinutesMean = (corridor.weeklyMinutesFrom + corridor.weeklyMinutesTill) / 2.0
                weeklyMinutesMean / 7.0 / 60.0
            }
        } else if (assistancePlanDto.hours.size > 0) {
            assistancePlanDto.hours
                .filter { it.hourTypeId == hourTypeId }
                .sumOf { it.weeklyMinutes / 7.0 / 60.0 }
        } else {
            assistancePlanDto.goals
                .flatMap { it.hours }
                .filter { it.hourTypeId == hourTypeId }
                .sumOf { it.weeklyMinutes / 7.0 / 60.0 }
        }

    private fun loadHourCorridors(assistancePlanDtos: List<AssistancePlanEditDto>): Map<Long, HourCorridor> {
        val ids = assistancePlanDtos.asSequence()
            .filter(::isCorridor)
            .map { it.hourCorridorId }
            .filter { it > 0 }
            .distinct()
            .toList()
        if (ids.isEmpty()) return emptyMap()
        return hourCorridorService.getAllEntitiesByIds(ids).associateBy { it.id }
    }

    private fun isCorridor(assistancePlanDto: AssistancePlanEditDto): Boolean {
        return assistancePlanDto.hourMode == de.vinz.openfls.domains.assistancePlans.AssistancePlanHourMode.CORRIDOR
    }

    private fun corridorApprovedHoursForDays(days: Int, weeklyMinutes: Int): Double {
        return TimeDoubleService.roundDoubleToTwoDigits((weeklyMinutes / 7.0) * days / 60.0)
    }

    private fun calculateCorridorDifference(executedHours: Double, approvedHoursFrom: Double, approvedHoursTo: Double): Double {
        return when {
            executedHours < approvedHoursFrom -> executedHours - approvedHoursFrom
            executedHours > approvedHoursTo -> executedHours - approvedHoursTo
            else -> 0.0
        }
    }

    private fun copyClient(client: ClientSimpleDto): ClientSimpleDto {
        return ClientSimpleDto().apply {
            id = client.id
            firstName = client.firstName
            lastName = client.lastName
            phoneNumber = client.phoneNumber
            email = client.email
            archived = client.archived
            institution = client.institution
        }
    }

    private fun validateYearMonth(year: Int, month: Int?): HourReportResult.InvalidTimeRange? {
        if (year < 0) {
            return HourReportResult.InvalidTimeRange
        }
        if (month != null && (month <= 0 || month > 12)) {
            return HourReportResult.InvalidTimeRange
        }
        return null
    }

    private fun validateAccess(areaId: Long?): HourReportResult.Forbidden? {
        if (areaId == null && !accessService.isAdmin()) {
            return HourReportResult.Forbidden
        }
        if (areaId != null && !accessService.canReadEntries(areaId)) {
            return HourReportResult.Forbidden
        }
        return null
    }
}
