package de.vinz.openfls.domains.overviews

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto
import de.vinz.openfls.domains.hourCorridors.HourCorridor
import de.vinz.openfls.domains.hourCorridors.HourCorridorRepository
import de.vinz.openfls.domains.overviews.dtos.AssistancePlanOverviewDTO
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
    private val hourCorridorRepository: HourCorridorRepository,
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
    ): List<AssistancePlanOverviewDTO> {
        checkAccess(areaId)
        checkYearMonth(year, month)

        val clientSimpleDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) };

        // Monthly
        if (month != null) {
            val assistancePlanDTOs = getAssistancePlans(year, month, areaId, sponsorId)

            return getExecutedHoursMonthly(
                services, assistancePlanDTOs, clientSimpleDTOs, year, month
            )
        }

        // Yearly
        val assistancePlanDTOs = getAssistancePlans(year, null, areaId, sponsorId)
        return getExecutedHoursYearly(services, assistancePlanDTOs, clientSimpleDTOs, year)
    }

    @Throws(UserNotAllowedException::class, IllegalTimeException::class)
    fun getExecutedHoursOverview(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanOverviewDTO> {
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
    ): List<AssistancePlanOverviewDTO> {
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
    ): List<AssistancePlanOverviewDTO> {
        checkAccess(areaId)
        checkYearMonth(year, month)

        val clientSimpleDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) }

        // Monthly
        if (month != null) {
            val assistancePlanDTOs = getAssistancePlans(year, month, areaId, sponsorId)

            return getApprovedHoursMonthly(
                assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year, month
            )
        }

        // Yearly
        val assistancePlanDTOs = getAssistancePlans(year, null, areaId, sponsorId)
        return getApprovedHoursYearly(assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year)
    }

    @Throws(UserNotAllowedException::class, IllegalTimeException::class)
    fun getDifferenceHoursOverview(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        areaId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanOverviewDTO> {
        checkAccess(areaId)
        checkYearMonth(year, month)

        val services = getServices(
            year = year,
            month = month,
            hourTypeId = hourTypeId,
            areaId = areaId,
            sponsorId = sponsorId
        )
        val clientSimpleDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) }

        // Monthly
        if (month != null) {
            val assistancePlanDTOs = getAssistancePlans(year, month, areaId, sponsorId)

            return getDifferenceHoursMonthly(
                services, assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year, month
            )
        }

        // Yearly
        val assistancePlanDTOs = getAssistancePlans(year, null, areaId, sponsorId)
        return getDifferenceHoursYearly(services, assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year)
    }

    internal fun getApprovedHoursMonthly(
        assistancePlanDTOs: List<AssistancePlanDto>,
        clientSimpleDTOs: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        month: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDTO> {

        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val assistancePlanOverviewDTOs =
            getAssistancePlanOverviewDTOsWithoutValues(assistancePlanDTOs, clientSimpleDTOs, daysInMonth)

        // Set date range for the aggregated "all" DTO at the beginning of the list
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs.first().apply {
            assistancePlanDto.start = LocalDate.of(year, month, 1)
            assistancePlanDto.end = assistancePlanDto.start.plusMonths(1).minusDays(1)
        }

        // Populate values for each assistance plan
        val hourCorridors = loadHourCorridors(assistancePlanDTOs)
        assistancePlanDTOs.forEach { planDto ->
            val overviewDTO =
                assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == planDto.id } ?: return@forEach
            val hoursPerDay = getDailyHoursOfAssistancePlanByHourType(planDto, hourTypeId, hourCorridors)

            (1..daysInMonth).forEach { day ->
                val date = LocalDate.of(year, month, day)
                if (DateService.isDateInAssistancePlan(date, planDto)) {
                    overviewDTO.values[0] += hoursPerDay
                    overviewDTO.values[day] = hoursPerDay
                    allAssistancePlanOverviewDTO.values[0] += hoursPerDay
                    allAssistancePlanOverviewDTO.values[day] += hoursPerDay
                } else {
                    overviewDTO.values[day] = 0.0
                }
            }
        }

        // Optional conversion to minute double values
        if (toTimeDouble) convertDoubleToMinuteDouble(assistancePlanOverviewDTOs)

        return assistancePlanOverviewDTOs
    }

    internal fun getApprovedHoursYearly(
        assistancePlanDTOs: List<AssistancePlanDto>,
        clientSimpleDTOs: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDTO> {
        val assistancePlanOverviewDTOs =
            getAssistancePlanOverviewDTOsWithoutValues(assistancePlanDTOs, clientSimpleDTOs, monthCount)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]

        val hourCorridors = loadHourCorridors(assistancePlanDTOs)
        assistancePlanDTOs.forEach { assistancePlanDto ->
            val assistancePlanOverviewDTO =
                assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == assistancePlanDto.id }
            val hoursPerDay = getDailyHoursOfAssistancePlanByHourType(assistancePlanDto, hourTypeId, hourCorridors)

            if (assistancePlanOverviewDTO != null) {
                for (i in 1..monthCount) {
                    val daysInMonth = DateService.countDaysOfAssistancePlan(year, i, assistancePlanDto)
                    val hoursPerMonth = hoursPerDay * daysInMonth
                    assistancePlanOverviewDTO.values[0] += hoursPerMonth
                    assistancePlanOverviewDTO.values[i] = hoursPerMonth

                    allAssistancePlanOverviewDTO.values[0] += hoursPerMonth
                    allAssistancePlanOverviewDTO.values[i] += hoursPerMonth
                }
            }
        }

        // convert to minute double
        if (toTimeDouble) convertDoubleToMinuteDouble(assistancePlanOverviewDTOs)

        return assistancePlanOverviewDTOs;
    }

    internal fun getExecutedHoursYearly(
        services: List<de.vinz.openfls.domains.services.Service>,
        assistancePlanDTOs: List<AssistancePlanDto>,
        clientDTOs: List<ClientSimpleDto>,
        year: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDTO> {
        val assistancePlanOverviewDTOs =
            getAssistancePlanOverviewDTOsWithoutValues(assistancePlanDTOs, clientDTOs, monthCount)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]
        allAssistancePlanOverviewDTO.assistancePlanDto.start = LocalDate.of(year, 1, 1)
        allAssistancePlanOverviewDTO.assistancePlanDto.end = LocalDate.of(year, 12, 31)

        services.forEach { service ->
            val assistancePlanOverviewDTO =
                assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == service.assistancePlan?.id }
            if (assistancePlanOverviewDTO != null) {
                val month = service.start.monthValue;
                assistancePlanOverviewDTO.values[0] += service.minutes.toDouble()
                assistancePlanOverviewDTO.values[month] += service.minutes.toDouble()

                allAssistancePlanOverviewDTO.values[0] += service.minutes.toDouble()
                allAssistancePlanOverviewDTO.values[month] += service.minutes.toDouble()
            }
        }

        // convert from minutes to hours
        convertMinutesValuesToHourValues(assistancePlanOverviewDTOs, toTimeDouble)

        return assistancePlanOverviewDTOs
    }

    internal fun getExecutedHoursMonthly(
        services: List<de.vinz.openfls.domains.services.Service>,
        assistancePlanDTOs: List<AssistancePlanDto>,
        clientDTOs: List<ClientSimpleDto>,
        year: Int,
        month: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDTO> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        val assistancePlanOverviewDTOs =
            getAssistancePlanOverviewDTOsWithoutValues(assistancePlanDTOs, clientDTOs, daysInMonth)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]

        services.forEach { service ->
            val assistancePlanOverviewDTO =
                assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == service.assistancePlan?.id }
            if (assistancePlanOverviewDTO != null) {
                val day = service.start.dayOfMonth;
                assistancePlanOverviewDTO.values[0] += service.minutes.toDouble()
                assistancePlanOverviewDTO.values[day] += service.minutes.toDouble()

                allAssistancePlanOverviewDTO.values[0] += service.minutes.toDouble()
                allAssistancePlanOverviewDTO.values[day] += service.minutes.toDouble()
            }
        }

        // convert from minutes to hours
        convertMinutesValuesToHourValues(assistancePlanOverviewDTOs, toTimeDouble)

        return assistancePlanOverviewDTOs
    }

    internal fun getDifferenceHoursYearly(
        services: List<de.vinz.openfls.domains.services.Service>,
        assistancePlanDTOs: List<AssistancePlanDto>,
        clientSimpleDTOs: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDTO> {
        val approvedOverviewDTOs = getApprovedHoursYearly(
            assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year, false
        )
        val executedOverviewDTOs = getExecutedHoursYearly(
            services, assistancePlanDTOs, clientSimpleDTOs, year, false
        )

        return subtractApprovedFromExecutedOverview(
            executedOverviewDTOs,
            approvedOverviewDTOs,
            year,
            null,
            hourTypeId,
            toTimeDouble
        )
    }

    internal fun getDifferenceHoursMonthly(
        services: List<de.vinz.openfls.domains.services.Service>,
        assistancePlanDTOs: List<AssistancePlanDto>,
        clientSimpleDTOs: List<ClientSimpleDto>,
        hourTypeId: Long?,
        year: Int,
        month: Int,
        toTimeDouble: Boolean = true
    ): List<AssistancePlanOverviewDTO> {
        val approvedOverviewDTOs = getApprovedHoursMonthly(
            assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year, month, false
        )
        val executedOverviewDTOs = getExecutedHoursMonthly(
            services, assistancePlanDTOs, clientSimpleDTOs, year, month, false
        )

        return subtractApprovedFromExecutedOverview(
            executedOverviewDTOs,
            approvedOverviewDTOs,
            year,
            month,
            hourTypeId,
            toTimeDouble
        )
    }

    internal fun subtractApprovedFromExecutedOverview(
        executedOverviewDTOs: List<AssistancePlanOverviewDTO>,
        approvedOverviewDTOs: List<AssistancePlanOverviewDTO>,
        year: Int,
        month: Int?,
        hourTypeId: Long?,
        toTimeDouble: Boolean
    ): List<AssistancePlanOverviewDTO> {
        val hourCorridors = loadHourCorridors(executedOverviewDTOs.map { it.assistancePlanDto })
        executedOverviewDTOs.forEach { executedOverviewDTO ->
            val singleApprovedOverviewDTO =
                approvedOverviewDTOs.find { it.assistancePlanDto.id == executedOverviewDTO.assistancePlanDto.id }

            if (singleApprovedOverviewDTO != null) {
                if (isCorridor(executedOverviewDTO.assistancePlanDto)) {
                    val corridor = hourCorridors[executedOverviewDTO.assistancePlanDto.hourCorridorId]
                    if (corridor != null && (hourTypeId == null || (corridor.hourType?.id ?: 0) == hourTypeId)) {
                        for (i in 1 until executedOverviewDTO.values.size) {
                            val daysInPeriod = if (month != null) {
                                1
                            } else {
                                DateService.countDaysOfAssistancePlan(year, i, executedOverviewDTO.assistancePlanDto).toInt()
                            }
                            val approvedHoursFrom =
                                corridorApprovedHoursForDays(daysInPeriod, corridor.weeklyMinutesFrom)
                            val approvedHoursTo =
                                corridorApprovedHoursForDays(daysInPeriod, corridor.weeklyMinutesTill)
                            executedOverviewDTO.values[i] =
                                calculateCorridorDifference(executedOverviewDTO.values[i], approvedHoursFrom, approvedHoursTo)
                        }
                        executedOverviewDTO.values[0] = executedOverviewDTO.values.drop(1).sum()
                    } else {
                        for (i in 0 until executedOverviewDTO.values.size) {
                            executedOverviewDTO.values[i] -= singleApprovedOverviewDTO.values[i]
                        }
                    }
                } else {
                    for (i in 0 until executedOverviewDTO.values.size) {
                        executedOverviewDTO.values[i] -= singleApprovedOverviewDTO.values[i]
                    }
                }
            }
        }

        // convert to minute double
        if (toTimeDouble) convertDoubleToMinuteDouble(executedOverviewDTOs)

        return executedOverviewDTOs
    }

    internal fun convertDoubleToMinuteDouble(assistancePlanOverviewDTOs: List<AssistancePlanOverviewDTO>) {
        assistancePlanOverviewDTOs.forEach { assistancePlanOverviewDTO ->
            assistancePlanOverviewDTO.values[0] = 0.0
            for (i in 1 until assistancePlanOverviewDTO.values.size) {
                assistancePlanOverviewDTO.values[i] =
                    TimeDoubleService.convertDoubleToTimeDouble(assistancePlanOverviewDTO.values[i])
                assistancePlanOverviewDTO.values[0] =
                    TimeDoubleService.sumTimeDoubles(
                        assistancePlanOverviewDTO.values[0],
                        assistancePlanOverviewDTO.values[i]
                    )
            }
        }
    }

    internal fun convertMinutesValuesToHourValues(
        assistancePlanOverviewDTOs: List<AssistancePlanOverviewDTO>,
        toTimeDouble: Boolean = true
    ) {
        assistancePlanOverviewDTOs.forEach { assistancePlanOverviewDTO ->
            for (i in 0 until assistancePlanOverviewDTO.values.size) {
                assistancePlanOverviewDTO.values[i] =
                    if (toTimeDouble) TimeDoubleService.convertDoubleToTimeDouble(assistancePlanOverviewDTO.values[i] / 60)
                    else TimeDoubleService.roundDoubleToTwoDigits(assistancePlanOverviewDTO.values[i] / 60)
            }
        }
    }

    internal fun getAssistancePlans(
        year: Int,
        month: Int?,
        institutionId: Long?,
        sponsorId: Long?
    ): List<AssistancePlanDto> {
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

        val mappedPlans = plans.map { modelMapper.map(it, AssistancePlanDto::class.java) }

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

    internal fun getAssistancePlanOverviewDTOsWithoutValues(
        assistancePlanDTOs: List<AssistancePlanDto>,
        clientDTOs: List<ClientSimpleDto>,
        valuesCount: Int
    ): MutableList<AssistancePlanOverviewDTO> {

        val allClient = ClientSimpleDto().apply { lastName = "Gesamt" }
        val defaultValuesArray = DoubleArray(valuesCount + 1) { 0.0 }

        val result = assistancePlanDTOs.map { plan ->
            val client = clientDTOs.find { it.id == plan.clientId }
                ?: throw IllegalArgumentException("Client with ID ${plan.clientId} not found")

            AssistancePlanOverviewDTO(plan, copyClient(client), defaultValuesArray.copyOf())
        }.sortedBy { it.clientDto.lastName }
            .toMutableList()

        result.add(0, AssistancePlanOverviewDTO(AssistancePlanDto(), allClient, defaultValuesArray))

        return result
    }

    internal fun getDailyHoursOfAssistancePlanByHourType(
        assistancePlanDto: AssistancePlanDto,
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

    private fun loadHourCorridors(assistancePlanDTOs: List<AssistancePlanDto>): Map<Long, HourCorridor> {
        val ids = assistancePlanDTOs.asSequence()
            .filter(::isCorridor)
            .map { it.hourCorridorId }
            .filter { it > 0 }
            .distinct()
            .toList()
        if (ids.isEmpty()) return emptyMap()
        return hourCorridorRepository.findAllById(ids).associateBy { it.id }
    }

    private fun isCorridor(assistancePlanDto: AssistancePlanDto): Boolean {
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
