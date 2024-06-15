package de.vinz.openfls.domains.overviews

import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanDto
import de.vinz.openfls.domains.overviews.dtos.AssistancePlanOverviewDTO
import de.vinz.openfls.domains.clients.dtos.ClientSimpleDto
import de.vinz.openfls.exceptions.IllegalTimeException
import de.vinz.openfls.exceptions.UserNotAllowedException
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.repositories.ServiceRepository
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.services.DateService
import de.vinz.openfls.services.NumberService
import org.modelmapper.ModelMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

@Service
class OverviewService(
        private val accessService: AccessService,
        private val serviceRepository: ServiceRepository,
        private val assistancePlanRepository: AssistancePlanRepository,
        private val clientRepository: ClientRepository,
        private val modelMapper: ModelMapper) {


    private val monthCount = 12
    private val logger: Logger = LoggerFactory.getLogger(OverviewService::class.java)

    @Throws(UserNotAllowedException::class, IllegalTimeException::class)
    fun getExecutedHoursOverview(token: String,
                                 year: Int,
                                 month: Int?,
                                 hourTypeId: Long,
                                 areaId: Long?,
                                 sponsorId: Long?): List<AssistancePlanOverviewDTO> {
        checkAccess(areaId, token)
        checkTime(year, month)

        val services = getServices(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = areaId,
                sponsorId = sponsorId)
        val clientSimpleDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) };

        // Monthly
        if (month != null) {
            val assistancePlanDTOs = getAssistancePlans(year, month, areaId, sponsorId)

            return getExecutedHoursMonthly(
                    services, assistancePlanDTOs, clientSimpleDTOs, year, month)
        }

        // Yearly
        val assistancePlanDTOs = getAssistancePlans(year, null, areaId, sponsorId)
        return getExecutedHoursYearly(services, assistancePlanDTOs, clientSimpleDTOs, year)
    }

    @Throws(UserNotAllowedException::class, IllegalTimeException::class)
    fun getApprovedHoursOverview(token: String,
                                 year: Int,
                                 month: Int?,
                                 hourTypeId: Long,
                                 areaId: Long?,
                                 sponsorId: Long?): List<AssistancePlanOverviewDTO> {
        checkAccess(areaId, token)
        checkTime(year, month)

        val clientSimpleDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) }

        // Monthly
        if (month != null) {
            val assistancePlanDTOs = getAssistancePlans(year, month, areaId, sponsorId)

            return getApprovedHoursMonthly(
                    assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year, month)
        }

        // Yearly
        val assistancePlanDTOs = getAssistancePlans(year, null, areaId, sponsorId)
        return getApprovedHoursYearly(assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year)
    }

    @Throws(UserNotAllowedException::class, IllegalTimeException::class)
    fun getDifferenceHoursOverview(token: String,
                                   year: Int,
                                   month: Int?,
                                   hourTypeId: Long,
                                   areaId: Long?,
                                   sponsorId: Long?): List<AssistancePlanOverviewDTO> {
        checkAccess(areaId, token)
        checkTime(year, month)

        val services = getServices(
                year = year,
                month = month,
                hourTypeId = hourTypeId,
                areaId = areaId,
                sponsorId = sponsorId)
        val clientSimpleDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) }

        // Monthly
        if (month != null) {
            val assistancePlanDTOs = getAssistancePlans(year, month, areaId, sponsorId)

            return getDifferenceHoursMonthly(
                    services, assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year, month)
        }

        // Yearly
        val assistancePlanDTOs = getAssistancePlans(year, null, areaId, sponsorId)
        return getDifferenceHoursYearly(services, assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year)
    }

    private fun getApprovedHoursMonthly(assistancePlanDTOs: List<AssistancePlanDto>,
                                        clientSimpleDTOs: List<ClientSimpleDto>,
                                        hourTypeId: Long?,
                                        year: Int,
                                        month: Int,
                                        toTimeDouble: Boolean = true): List<AssistancePlanOverviewDTO> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val assistancePlanOverviewDTOs =
                getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientSimpleDTOs, daysInMonth)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]
        allAssistancePlanOverviewDTO.assistancePlanDto.start =
                LocalDate.of(year, month, 1)
        allAssistancePlanOverviewDTO.assistancePlanDto.end =
                LocalDate.of(year, month, 1).plusMonths(1).minusDays(1)

        assistancePlanDTOs.forEach { assistancePlanDto ->
            val assistancePlanOverviewDTO =
                    assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == assistancePlanDto.id }
            val hoursPerDay = getDailyHoursOfAssistancePlanByHourType(assistancePlanDto, hourTypeId)

            if (assistancePlanOverviewDTO != null) {
                for (i in 1..daysInMonth) {
                    if (DateService.isDateInAssistancePlan(
                                    LocalDate.of(year, month, i), assistancePlanDto)) {
                        assistancePlanOverviewDTO.values[0] += hoursPerDay
                        assistancePlanOverviewDTO.values[i] = hoursPerDay

                        allAssistancePlanOverviewDTO.values[0] += hoursPerDay
                        allAssistancePlanOverviewDTO.values[i] += hoursPerDay
                    } else {
                        assistancePlanOverviewDTO.values[i] = 0.0
                    }
                }
            }
        }

        // convert to minute double
        if (toTimeDouble) convertDoubleToMinuteDouble(assistancePlanOverviewDTOs)

        return assistancePlanOverviewDTOs;
    }

    private fun getApprovedHoursYearly(assistancePlanDTOs: List<AssistancePlanDto>,
                                       clientSimpleDTOs: List<ClientSimpleDto>,
                                       hourTypeId: Long?,
                                       year: Int,
                                       toTimeDouble: Boolean = true): List<AssistancePlanOverviewDTO> {
        val assistancePlanOverviewDTOs =
                getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientSimpleDTOs, monthCount)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]

        assistancePlanDTOs.forEach { assistancePlanDto ->
            val assistancePlanOverviewDTO =
                    assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == assistancePlanDto.id }
            val hoursPerDay = getDailyHoursOfAssistancePlanByHourType(assistancePlanDto, hourTypeId)

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

    private fun getExecutedHoursYearly(services: List<de.vinz.openfls.entities.Service>,
                                       assistancePlanDTOs: List<AssistancePlanDto>,
                                       clientDTOs: List<ClientSimpleDto>,
                                       year: Int,
                                       toTimeDouble: Boolean = true): List<AssistancePlanOverviewDTO> {
        val assistancePlanOverviewDTOs = getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientDTOs, monthCount)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]
        allAssistancePlanOverviewDTO.assistancePlanDto.start = LocalDate.of(year, 1, 1)
        allAssistancePlanOverviewDTO.assistancePlanDto.end = LocalDate.of(year, 12, 31)

        services.forEach { service ->
            val assistancePlanOverviewDTO = assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == service.assistancePlan?.id }
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

    private fun getExecutedHoursMonthly(services: List<de.vinz.openfls.entities.Service>,
                                        assistancePlanDTOs: List<AssistancePlanDto>,
                                        clientDTOs: List<ClientSimpleDto>,
                                        year: Int,
                                        month: Int,
                                        toTimeDouble: Boolean = true): List<AssistancePlanOverviewDTO> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        val assistancePlanOverviewDTOs = getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientDTOs, daysInMonth)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]

        services.forEach { service ->
            val assistancePlanOverviewDTO = assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == service.assistancePlan?.id }
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

    private fun getDifferenceHoursYearly(services: List<de.vinz.openfls.entities.Service>,
                                         assistancePlanDTOs: List<AssistancePlanDto>,
                                         clientSimpleDTOs: List<ClientSimpleDto>,
                                         hourTypeId: Long?,
                                         year: Int,
                                         toTimeDouble: Boolean = true): List<AssistancePlanOverviewDTO> {
        val approvedOverviewDTOs = getApprovedHoursYearly(
                assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year, false)
        val executedOverviewDTOs = getExecutedHoursYearly(
                services, assistancePlanDTOs, clientSimpleDTOs, year, false)

        return subtractApprovedFromExecutedOverview(executedOverviewDTOs, approvedOverviewDTOs, toTimeDouble)
    }

    private fun getDifferenceHoursMonthly(services: List<de.vinz.openfls.entities.Service>,
                                          assistancePlanDTOs: List<AssistancePlanDto>,
                                          clientSimpleDTOs: List<ClientSimpleDto>,
                                          hourTypeId: Long?,
                                          year: Int,
                                          month: Int,
                                          toTimeDouble: Boolean = true): List<AssistancePlanOverviewDTO> {
        val approvedOverviewDTOs = getApprovedHoursMonthly(
                assistancePlanDTOs, clientSimpleDTOs, hourTypeId, year, month, false)
        val executedOverviewDTOs = getExecutedHoursMonthly(
                services, assistancePlanDTOs, clientSimpleDTOs, year, month, false)

        return subtractApprovedFromExecutedOverview(executedOverviewDTOs, approvedOverviewDTOs, toTimeDouble)
    }

    private fun subtractApprovedFromExecutedOverview(executedOverviewDTOs: List<AssistancePlanOverviewDTO>, approvedOverviewDTOs: List<AssistancePlanOverviewDTO>, toTimeDouble: Boolean): List<AssistancePlanOverviewDTO> {
        executedOverviewDTOs.map { executedOverviewDTO ->
            val singleApprovedOverviewDTO =
                    approvedOverviewDTOs.find { it.assistancePlanDto.id == executedOverviewDTO.assistancePlanDto.id }

            if (singleApprovedOverviewDTO != null) {
                for (i in 0 until executedOverviewDTO.values.size) {
                    executedOverviewDTO.values[i] -= singleApprovedOverviewDTO.values[i]
                }
            }
        }

        // convert to minute double
        if (toTimeDouble) convertDoubleToMinuteDouble(executedOverviewDTOs)

        return executedOverviewDTOs
    }

    private fun convertDoubleToMinuteDouble(assistancePlanOverviewDTOs: List<AssistancePlanOverviewDTO>) {
        assistancePlanOverviewDTOs.forEach { assistancePlanOverviewDTO ->
            assistancePlanOverviewDTO.values[0] = 0.0
            for (i in 1 until assistancePlanOverviewDTO.values.size) {
                assistancePlanOverviewDTO.values[i] =
                        NumberService.convertDoubleToTimeDouble(assistancePlanOverviewDTO.values[i])
                assistancePlanOverviewDTO.values[0] =
                        NumberService.sumTimeDoubles(
                                assistancePlanOverviewDTO.values[0],
                                assistancePlanOverviewDTO.values[i])
            }
        }
    }

    private fun convertMinutesValuesToHourValues(assistancePlanOverviewDTOs: List<AssistancePlanOverviewDTO>,
                                                 toTimeDouble: Boolean = true) {
        assistancePlanOverviewDTOs.forEach { assistancePlanOverviewDTO ->
            for (i in 0 until assistancePlanOverviewDTO.values.size) {
                assistancePlanOverviewDTO.values[i] =
                        if (toTimeDouble) NumberService.convertDoubleToTimeDouble(assistancePlanOverviewDTO.values[i] / 60)
                        else NumberService.roundDoubleToTwoDigits(assistancePlanOverviewDTO.values[i] / 60)
            }
        }
    }

    private fun getAssistancePlans(
            year: Int,
            month: Int?,
            institutionId: Long?,
            sponsorId: Long?) : List<AssistancePlanDto> {
        if (institutionId != null && sponsorId != null) {
            return if (month == null) {
                assistancePlanRepository
                        .findByInstitutionIdAndSponsorIdAndYear(institutionId, sponsorId, year)
                        .map { modelMapper.map(it, AssistancePlanDto::class.java) }
            } else {
                assistancePlanRepository
                        .findByInstitutionIdAndSponsorIdAndYear(institutionId, sponsorId, year)
                        .map { modelMapper.map(it, AssistancePlanDto::class.java) }
                        .filter { DateService.containsStartAndEndASpecificYearMonth(it.start, it.end, YearMonth.of(year, month)) }
            }
        } else if (institutionId != null) {
            return if (month == null) {
                assistancePlanRepository
                        .findByInstitutionIdAndYear(institutionId, year)
                        .map { modelMapper.map(it, AssistancePlanDto::class.java) }
            } else {
                assistancePlanRepository
                        .findByInstitutionIdAndYear(institutionId, year)
                        .map { modelMapper.map(it, AssistancePlanDto::class.java) }
                        .filter { DateService.containsStartAndEndASpecificYearMonth(it.start, it.end, YearMonth.of(year, month)) }
            }
        } else if (sponsorId != null) {
            return if (month == null) {
                assistancePlanRepository
                        .findBySponsorIdAndYear(sponsorId, year)
                        .map { modelMapper.map(it, AssistancePlanDto::class.java) }
            } else {
                assistancePlanRepository
                        .findBySponsorIdAndYear(sponsorId, year)
                        .map { modelMapper.map(it, AssistancePlanDto::class.java) }
                        .filter { DateService.containsStartAndEndASpecificYearMonth(it.start, it.end, YearMonth.of(year, month)) }
            }
        }

        return if (month == null) {
            assistancePlanRepository
                    .findAllByYear(year).toList()
                    .map { modelMapper.map(it, AssistancePlanDto::class.java) }
        } else {
            assistancePlanRepository
                    .findAllByYear(year).toList()
                    .map { modelMapper.map(it, AssistancePlanDto::class.java) }
                    .filter { DateService.containsStartAndEndASpecificYearMonth(it.start, it.end, YearMonth.of(year, month)) }
        }
    }

    private fun getServices(
            year: Int,
            month: Int?,
            hourTypeId: Long,
            areaId: Long?,
            sponsorId: Long?): List<de.vinz.openfls.entities.Service> {
        if (month == null) {
            if (areaId != null && sponsorId != null) {
                return serviceRepository
                        .findServiceByYearByHourTypeIdAndAreaIdAndSponsorId(
                                year = year,
                                hourTypeId = hourTypeId,
                                areaId = areaId,
                                sponsorId = sponsorId);
            } else if (areaId != null) {
                return serviceRepository
                        .findServiceByYearByHourTypeIdAndAreaId(
                                year = year,
                                hourTypeId = hourTypeId,
                                areaId = areaId);
            } else if (sponsorId != null) {
                return serviceRepository
                        .findServiceByYearByHourTypeIdAndSponsorId(
                                year = year,
                                hourTypeId = hourTypeId,
                                sponsorId = sponsorId);
            } else {
                return serviceRepository
                        .findServiceByYearByHourTypeId(
                                year = year,
                                hourTypeId = hourTypeId);
            }
        } else {
            if (areaId != null && sponsorId != null) {
                return serviceRepository
                        .findServiceByYearAndMonthAndHourTypeIdAndAreaIdAndSponsorId(
                                year = year,
                                month = month,
                                hourTypeId = hourTypeId,
                                areaId = areaId,
                                sponsorId = sponsorId);
            } else if (areaId != null) {
                return serviceRepository
                        .findServiceByYearAndMonthAndHourTypeIdAndAreaId(
                                year = year,
                                month = month,
                                hourTypeId = hourTypeId,
                                areaId = areaId);
            } else if (sponsorId != null) {
                return serviceRepository
                        .findServiceByYearAndMonthAndHourTypeIdAndSponsorId(
                                year = year,
                                month = month,
                                hourTypeId = hourTypeId,
                                sponsorId = sponsorId);
            } else {
                return serviceRepository
                        .findServiceByYearAndMonthAndHourTypeId(
                                year = year,
                                month = month,
                                hourTypeId = hourTypeId);
            }
        }
    }

    private fun getAssistancePlanOverviewDTOS(assistancePlanDTOs: List<AssistancePlanDto>,
                                              clientDTOs: List<ClientSimpleDto>,
                                              valuesCount: Int): MutableList<AssistancePlanOverviewDTO> {
        val allClient = ClientSimpleDto()
        allClient.lastName = "Gesamt"

        val result = assistancePlanDTOs
                .map { AssistancePlanOverviewDTO(it,
                        clientDTOs.find { client -> client.id == it.clientId } ?: throw IllegalArgumentException(),
                        DoubleArray(valuesCount + 1) { 0.0 })
                }
                .sortedBy { it.clientDto.lastName }
                .toMutableList()

        result.add(0, AssistancePlanOverviewDTO(AssistancePlanDto(), allClient, DoubleArray(valuesCount + 1) { 0.0 }))

        return result;
    }

    private fun getDailyHoursOfAssistancePlanByHourType(assistancePlanDto: AssistancePlanDto, hourTypeId: Long?): Double =
            if (hourTypeId == null) {
                0.0
            } else if (assistancePlanDto.hours.size > 0) {
                        assistancePlanDto.hours
                                .filter { it.hourTypeId == hourTypeId }
                                .sumOf { it.weeklyHours / 7 }
            } else {
                        assistancePlanDto.goals
                                .flatMap { it.hours }
                                .filter { it.hourTypeId == hourTypeId }
                                .sumOf { it.weeklyHours / 7 }
            }

    @Throws(IllegalTimeException::class)
    private fun checkTime(year: Int, month: Int?) {
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
    private fun checkAccess(areaId: Long?, token: String) {
        if (areaId == null && !accessService.isAdmin(token)) {
            throw UserNotAllowedException()
        }
        if (areaId != null && !accessService.canReadEntries(token, areaId)) {
            throw UserNotAllowedException()
        }
    }
}