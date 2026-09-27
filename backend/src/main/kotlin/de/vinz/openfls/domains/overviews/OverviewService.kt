package de.vinz.openfls.domains.overviews

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanEditDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto
import de.vinz.openfls.domains.hourCorridors.HourCorridor
import de.vinz.openfls.domains.hourCorridors.HourCorridorService
import de.vinz.openfls.domains.overviews.dtos.AssistancePlanOverviewDto
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.exceptions.IllegalTimeException
import de.vinz.openfls.exceptions.UserNotAllowedException
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.TimeDoubleService
import org.springframework.transaction.annotation.Transactional
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
@Transactional(readOnly = true)
class OverviewService(
    private val accessService: AccessService,
    private val serviceRepository: ServiceRepository,
    private val assistancePlanRepository: AssistancePlanRepository,
    private val hourCorridorService: HourCorridorService,
    private val clientRepository: ClientRepository,
    private val modelMapper: ModelMapper
) {

    private val monthCount = 12


    @Throws(UserNotAllowedException::class, IllegalTimeException::class)
    fun getExecutedHoursOverview(
        services: List<de.vinz.openfls.domains.services.Service>,
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanOverviewDto> {
        checkAccess(areaId)
        checkYearMonth(year, month)

        val clientSimpleDtos = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) };

        // Monthly
        if (month != null) {
            val assistancePlanDtos = getAssistancePlans(year, month, areaId, sponsorId)

            return getExecutedHoursMonthly(
                services, assistancePlanDtos, clientSimpleDtos, year, month
            )
        }

        // Yearly
        val assistancePlanDtos = getAssistancePlans(year, null, areaId, sponsorId)
        return getExecutedHoursYearly(services, assistancePlanDtos, clientSimpleDtos, year)
    }

    @Throws(UserNotAllowedException::class, IllegalTimeException::class)
    fun getExecutedHoursOverview(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanOverviewDto> {
        checkAccess(areaId)
        checkYearMonth(year, month)

        val services = getServices(
            year = year,
            month = month,
            hourTypeId = hourTypeId,
            areaId = areaId,
            sponsorId = sponsorId
        )

        return getExecutedHoursOverview(services, year, month, hourTypeId, areaId, sponsorId)
    }

    fun getExecutedHoursGroupServiceOverview(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanOverviewDto> {
        checkAccess(areaId)
        checkYearMonth(year, month)

        val services = getServices(
            year = year,
            month = month,
            hourTypeId = hourTypeId,
            areaId = areaId,
            sponsorId = sponsorId,
            onlyGroupServices = true
        )

        return getExecutedHoursOverview(services, year, month, hourTypeId, areaId, sponsorId)
    }

    @Throws(UserNotAllowedException::class, IllegalTimeException::class)
    fun getApprovedHoursOverview(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanOverviewDto> {
        checkAccess(areaId)
        checkYearMonth(year, month)

        val clientSimpleDtos = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) }

        // Monthly
        if (month != null) {
            val assistancePlanDtos = getAssistancePlans(year, month, areaId, sponsorId)

            return getApprovedHoursMonthly(
                assistancePlanDtos, clientSimpleDtos, hourTypeId, year, month
            )
        }

        // Yearly
        val assistancePlanDtos = getAssistancePlans(year, null, areaId, sponsorId)
        return getApprovedHoursYearly(assistancePlanDtos, clientSimpleDtos, hourTypeId, year)
    }

    @Throws(UserNotAllowedException::class, IllegalTimeException::class)
    fun getDifferenceHoursOverview(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanOverviewDto> {
        checkAccess(areaId)
        checkYearMonth(year, month)

        val services = getServices(
            year = year,
            month = month,
            hourTypeId = hourTypeId,
            areaId = areaId,
            sponsorId = sponsorId
        )
        val clientSimpleDtos = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) }

        // Monthly
        if (month != null) {
            val assistancePlanDtos = getAssistancePlans(year, month, areaId, sponsorId)

            return getDifferenceHoursMonthly(
                services, assistancePlanDtos, clientSimpleDtos, hourTypeId, year, month
            )
        }

        // Yearly
        val assistancePlanDtos = getAssistancePlans(year, null, areaId, sponsorId)
        return getDifferenceHoursYearly(services, assistancePlanDtos, clientSimpleDtos, hourTypeId, year)
    }

    internal fun getApprovedHoursMonthly(
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientSimpleDtos: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        month: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDto> {

        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val assistancePlanOverviewDtos =
            getAssistancePlanOverviewDtosWithoutValues(assistancePlanDtos, clientSimpleDtos, daysInMonth)

        // Set date range for the aggregated "all" DTO at the beginning of the list
        val allAssistancePlanOverviewDto = assistancePlanOverviewDtos.first().apply {
            assistancePlanDto.start = LocalDate.of(year, month, 1)
            assistancePlanDto.end = assistancePlanDto.start.plusMonths(1).minusDays(1)
        }

        // Populate values for each assistance plan
        val hourCorridors = loadHourCorridors(assistancePlanDtos)
        assistancePlanDtos.forEach { planDto ->
            val overviewDto =
                assistancePlanOverviewDtos.find { it.assistancePlanDto.id == planDto.id } ?: return@forEach
            val hoursPerDay = getDailyHoursOfAssistancePlanByHourType(planDto, hourTypeId, hourCorridors)

            (1..daysInMonth).forEach { day ->
                val date = LocalDate.of(year, month, day)
                if (DateService.isDateInAssistancePlan(date, planDto.start, planDto.end)) {
                    overviewDto.values[0] += hoursPerDay
                    overviewDto.values[day] = hoursPerDay
                    allAssistancePlanOverviewDto.values[0] += hoursPerDay
                    allAssistancePlanOverviewDto.values[day] += hoursPerDay
                } else {
                    overviewDto.values[day] = 0.0
                }
            }
        }

        // Optional conversion to minute double values
        if (toTimeDouble) convertDoubleToMinuteDouble(assistancePlanOverviewDtos)

        return assistancePlanOverviewDtos
    }

    internal fun getApprovedHoursYearly(
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientSimpleDtos: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDto> {
        val assistancePlanOverviewDtos =
            getAssistancePlanOverviewDtosWithoutValues(assistancePlanDtos, clientSimpleDtos, monthCount)
        val allAssistancePlanOverviewDto = assistancePlanOverviewDtos[0]

        val hourCorridors = loadHourCorridors(assistancePlanDtos)
        assistancePlanDtos.forEach { assistancePlanDto ->
            val assistancePlanOverviewDto =
                assistancePlanOverviewDtos.find { it.assistancePlanDto.id == assistancePlanDto.id }
            val hoursPerDay = getDailyHoursOfAssistancePlanByHourType(assistancePlanDto, hourTypeId, hourCorridors)

            if (assistancePlanOverviewDto != null) {
                for (i in 1..monthCount) {
                    val daysInMonth = DateService.countDaysOfAssistancePlan(year, i, assistancePlanDto.start, assistancePlanDto.end)
                    val hoursPerMonth = hoursPerDay * daysInMonth
                    assistancePlanOverviewDto.values[0] += hoursPerMonth
                    assistancePlanOverviewDto.values[i] = hoursPerMonth

                    allAssistancePlanOverviewDto.values[0] += hoursPerMonth
                    allAssistancePlanOverviewDto.values[i] += hoursPerMonth
                }
            }
        }

        // convert to minute double
        if (toTimeDouble) convertDoubleToMinuteDouble(assistancePlanOverviewDtos)

        return assistancePlanOverviewDtos;
    }

    internal fun getExecutedHoursYearly(
        services: List<de.vinz.openfls.domains.services.Service>,
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientDtos: List<ClientSimpleDto>,
        year: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDto> {
        val assistancePlanOverviewDtos =
            getAssistancePlanOverviewDtosWithoutValues(assistancePlanDtos, clientDtos, monthCount)
        val allAssistancePlanOverviewDto = assistancePlanOverviewDtos[0]
        allAssistancePlanOverviewDto.assistancePlanDto.start = LocalDate.of(year, 1, 1)
        allAssistancePlanOverviewDto.assistancePlanDto.end = LocalDate.of(year, 12, 31)

        services.forEach { service ->
            val assistancePlanOverviewDto =
                assistancePlanOverviewDtos.find { it.assistancePlanDto.id == service.assistancePlan?.id }
            if (assistancePlanOverviewDto != null) {
                val month = service.start.monthValue;
                assistancePlanOverviewDto.values[0] += service.minutes.toDouble()
                assistancePlanOverviewDto.values[month] += service.minutes.toDouble()

                allAssistancePlanOverviewDto.values[0] += service.minutes.toDouble()
                allAssistancePlanOverviewDto.values[month] += service.minutes.toDouble()
            }
        }

        // convert from minutes to hours
        convertMinutesValuesToHourValues(assistancePlanOverviewDtos, toTimeDouble)

        return assistancePlanOverviewDtos
    }

    internal fun getExecutedHoursMonthly(
        services: List<de.vinz.openfls.domains.services.Service>,
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientDtos: List<ClientSimpleDto>,
        year: Int,
        month: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDto> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        val assistancePlanOverviewDtos =
            getAssistancePlanOverviewDtosWithoutValues(assistancePlanDtos, clientDtos, daysInMonth)
        val allAssistancePlanOverviewDto = assistancePlanOverviewDtos[0]

        services.forEach { service ->
            val assistancePlanOverviewDto =
                assistancePlanOverviewDtos.find { it.assistancePlanDto.id == service.assistancePlan?.id }
            if (assistancePlanOverviewDto != null) {
                val day = service.start.dayOfMonth;
                assistancePlanOverviewDto.values[0] += service.minutes.toDouble()
                assistancePlanOverviewDto.values[day] += service.minutes.toDouble()

                allAssistancePlanOverviewDto.values[0] += service.minutes.toDouble()
                allAssistancePlanOverviewDto.values[day] += service.minutes.toDouble()
            }
        }

        // convert from minutes to hours
        convertMinutesValuesToHourValues(assistancePlanOverviewDtos, toTimeDouble)

        return assistancePlanOverviewDtos
    }

    internal fun getDifferenceHoursYearly(
        services: List<de.vinz.openfls.domains.services.Service>,
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientSimpleDtos: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDto> {
        val approvedOverviewDtos = getApprovedHoursYearly(
            assistancePlanDtos, clientSimpleDtos, hourTypeId, year, false
        )
        val executedOverviewDtos = getExecutedHoursYearly(
            services, assistancePlanDtos, clientSimpleDtos, year, false
        )

        return subtractApprovedFromExecutedOverview(
            executedOverviewDtos,
            approvedOverviewDtos,
            year,
            null,
            hourTypeId,
            toTimeDouble
        )
    }

    internal fun getDifferenceHoursMonthly(
        services: List<de.vinz.openfls.domains.services.Service>,
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientSimpleDtos: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        month: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDto> {
        val approvedOverviewDtos = getApprovedHoursMonthly(
            assistancePlanDtos, clientSimpleDtos, hourTypeId, year, month, false
        )
        val executedOverviewDtos = getExecutedHoursMonthly(
            services, assistancePlanDtos, clientSimpleDtos, year, month, false
        )

        return subtractApprovedFromExecutedOverview(
            executedOverviewDtos,
            approvedOverviewDtos,
            year,
            month,
            hourTypeId,
            toTimeDouble
        )
    }

    internal fun subtractApprovedFromExecutedOverview(
        executedOverviewDtos: List<AssistancePlanOverviewDto>,
        approvedOverviewDtos: List<AssistancePlanOverviewDto>,
        year: Int,
        month: Int?,
        hourTypeId: Long?,
        toTimeDouble: Boolean
    ): List<AssistancePlanOverviewDto> {
        val hourCorridors = loadHourCorridors(executedOverviewDtos.map { it.assistancePlanDto })
        executedOverviewDtos.forEach { executedOverviewDto ->
            val singleApprovedOverviewDto =
                approvedOverviewDtos.find { it.assistancePlanDto.id == executedOverviewDto.assistancePlanDto.id }

            if (singleApprovedOverviewDto != null) {
                if (isCorridor(executedOverviewDto.assistancePlanDto)) {
                    val corridor = hourCorridors[executedOverviewDto.assistancePlanDto.hourCorridorId]
                    if (corridor != null && (hourTypeId == null || (corridor.hourType?.id ?: 0) == hourTypeId)) {
                        for (i in 1 until executedOverviewDto.values.size) {
                            val daysInPeriod = if (month != null) {
                                1
                            } else {
                                DateService.countDaysOfAssistancePlan(year, i, executedOverviewDto.assistancePlanDto.start, executedOverviewDto.assistancePlanDto.end).toInt()
                            }
                            val approvedHoursFrom =
                                corridorApprovedHoursForDays(daysInPeriod, corridor.weeklyMinutesFrom)
                            val approvedHoursTo =
                                corridorApprovedHoursForDays(daysInPeriod, corridor.weeklyMinutesTill)
                            executedOverviewDto.values[i] =
                                calculateCorridorDifference(executedOverviewDto.values[i], approvedHoursFrom, approvedHoursTo)
                        }
                        executedOverviewDto.values[0] = executedOverviewDto.values.drop(1).sum()
                    } else {
                        for (i in 0 until executedOverviewDto.values.size) {
                            executedOverviewDto.values[i] -= singleApprovedOverviewDto.values[i]
                        }
                    }
                } else {
                    for (i in 0 until executedOverviewDto.values.size) {
                        executedOverviewDto.values[i] -= singleApprovedOverviewDto.values[i]
                    }
                }
            }
        }

        // convert to minute double
        if (toTimeDouble) convertDoubleToMinuteDouble(executedOverviewDtos)

        return executedOverviewDtos
    }

    internal fun convertDoubleToMinuteDouble(assistancePlanOverviewDtos: List<AssistancePlanOverviewDto>) {
        assistancePlanOverviewDtos.forEach { assistancePlanOverviewDto ->
            assistancePlanOverviewDto.values[0] = 0.0
            for (i in 1 until assistancePlanOverviewDto.values.size) {
                assistancePlanOverviewDto.values[i] =
                    TimeDoubleService.convertDoubleToTimeDouble(assistancePlanOverviewDto.values[i])
                assistancePlanOverviewDto.values[0] =
                    TimeDoubleService.sumTimeDoubles(
                        assistancePlanOverviewDto.values[0],
                        assistancePlanOverviewDto.values[i]
                    )
            }
        }
    }

    internal fun convertMinutesValuesToHourValues(
        assistancePlanOverviewDtos: List<AssistancePlanOverviewDto>,
        toTimeDouble: Boolean = true
    ) {
        assistancePlanOverviewDtos.forEach { assistancePlanOverviewDto ->
            for (i in 0 until assistancePlanOverviewDto.values.size) {
                assistancePlanOverviewDto.values[i] =
                    if (toTimeDouble) TimeDoubleService.convertDoubleToTimeDouble(assistancePlanOverviewDto.values[i] / 60)
                    else TimeDoubleService.roundDoubleToTwoDigits(assistancePlanOverviewDto.values[i] / 60)
            }
        }
    }

    internal fun getAssistancePlans(
        year: Int,
        month: Int?,
        institutionId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanEditDto> {
        val plans = when {
            institutionId != null && sponsorId != null ->
                assistancePlanRepository.findByInstitutionIdAndSponsorIdAndYear(institutionId, sponsorId, year)

            institutionId != null ->
                assistancePlanRepository.findByInstitutionIdAndYear(institutionId, year)

            sponsorId != null ->
                assistancePlanRepository.findBySponsorIdAndYear(sponsorId, year)

            else ->
                assistancePlanRepository.findAllByYear(year).toList()
        }

        val mappedPlans = plans.map { modelMapper.map(it, AssistancePlanEditDto::class.java) }

        return if (month != null) {
            mappedPlans.filter {
                DateService.containsStartAndEndASpecificYearMonth(
                    it.start,
                    it.end,
                    YearMonth.of(year, month)
                )
            }
        } else {
            mappedPlans
        }
    }

    internal fun getServices(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?,
        onlyGroupServices: Boolean = false
    ): List<de.vinz.openfls.domains.services.Service> {
        val services = when {
            areaId != null && sponsorId != null && month != null ->
                serviceRepository.findServiceByYearAndMonthAndHourTypeIdAndAreaIdAndSponsorId(
                    year = year,
                    month = month,
                    hourTypeId = hourTypeId,
                    areaId = areaId,
                    sponsorId = sponsorId
                )

            areaId != null && sponsorId != null ->
                serviceRepository.findServiceByYearByHourTypeIdAndAreaIdAndSponsorId(
                    year = year,
                    hourTypeId = hourTypeId,
                    areaId = areaId,
                    sponsorId = sponsorId
                )

            areaId != null && month != null ->
                serviceRepository.findServiceByYearAndMonthAndHourTypeIdAndAreaId(
                    year = year,
                    month = month,
                    hourTypeId = hourTypeId,
                    areaId = areaId
                )

            sponsorId != null && month != null ->
                serviceRepository.findServiceByYearAndMonthAndHourTypeIdAndSponsorId(
                    year = year,
                    month = month,
                    hourTypeId = hourTypeId,
                    sponsorId = sponsorId
                )

            areaId != null ->
                serviceRepository.findServiceByYearByHourTypeIdAndAreaId(
                    year = year,
                    hourTypeId = hourTypeId,
                    areaId = areaId
                )

            sponsorId != null ->
                serviceRepository.findServiceByYearByHourTypeIdAndSponsorId(
                    year = year,
                    hourTypeId = hourTypeId,
                    sponsorId = sponsorId
                )

            month != null ->
                serviceRepository.findServiceByYearAndMonthAndHourTypeId(
                    year = year,
                    month = month,
                    hourTypeId = hourTypeId
                )

            else ->
                serviceRepository.findServiceByYearByHourTypeId(
                    year = year,
                    hourTypeId = hourTypeId
                )
        }

        // Filter for group service if needed
        return if (onlyGroupServices) {
            services.filter { it.groupService }
        } else {
            services
        }
    }

    internal fun getAssistancePlanOverviewDtosWithoutValues(
        assistancePlanDtos: List<AssistancePlanEditDto>,
        clientDtos: List<ClientSimpleDto>,
        valuesCount: Int
    ): MutableList<AssistancePlanOverviewDto> {

        val allClient = ClientSimpleDto().apply { lastName = "Gesamt" }
        val defaultValuesArray = DoubleArray(valuesCount + 1) { 0.0 }

        val result = assistancePlanDtos.map { plan ->
            val client = clientDtos.find { it.id == plan.clientId }
                ?: throw IllegalArgumentException("Client with ID ${plan.clientId} not found")

            AssistancePlanOverviewDto(plan, copyClient(client), defaultValuesArray.copyOf())
        }.sortedBy { it.clientDto.lastName }
            .toMutableList()

        result.add(0, AssistancePlanOverviewDto(AssistancePlanEditDto(), allClient, defaultValuesArray))

        return result
    }

    internal fun getDailyHoursOfAssistancePlanByHourType(
        assistancePlanDto: AssistancePlanEditDto,
        hourTypeId: Long?,
        hourCorridors: Map<Long, HourCorridor> = loadHourCorridors(listOf(assistancePlanDto))
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

    @Throws(IllegalTimeException::class)
    internal fun checkYearMonth(year: Int, month: Int?) {
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
    internal fun checkAccess(areaId: Long?) {
        if (areaId == null && !accessService.isAdmin()) {
            throw UserNotAllowedException()
        }
        if (areaId != null && !accessService.canReadEntries(areaId)) {
            throw UserNotAllowedException()
        }
    }
}
