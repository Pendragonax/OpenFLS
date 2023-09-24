package de.vinz.openfls.services

import de.vinz.openfls.dtos.AssistancePlanDto
import de.vinz.openfls.dtos.AssistancePlanOverviewDTO
import de.vinz.openfls.dtos.ClientSimpleDto
import de.vinz.openfls.repositories.AssistancePlanRepository
import de.vinz.openfls.repositories.ClientRepository
import de.vinz.openfls.repositories.ServiceRepository
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth

private const val MONTH_COUNT = 12

@Service
class OverviewService(
        private val permissionService: PermissionService,
        private val serviceRepository: ServiceRepository,
        private val assistancePlanRepository: AssistancePlanRepository,
        private val clientRepository: ClientRepository,
        private val modelMapper: ModelMapper) {
    fun getExecutedHoursOverview(year: Int,
                                 month: Int?,
                                 hourTypeId: Long,
                                 areaId: Long?,
                                 sponsorId: Long?): List<AssistancePlanOverviewDTO> {
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

            return getExecutedHoursMonthlyByAssistancePlansAndYear(
                    services, assistancePlanDTOs, clientSimpleDTOs, year, month)
        }

        // Yearly
        val assistancePlanDTOs = getAssistancePlans(year, null, areaId, sponsorId)
        return getExecutedHoursYearlyByAssistancePlansAndYear(services, assistancePlanDTOs, clientSimpleDTOs)
    }

    fun getApprovedHoursOverview(year: Int,
                                 month: Int?,
                                 hourTypeId: Long,
                                 areaId: Long?,
                                 sponsorId: Long?): List<AssistancePlanOverviewDTO> {

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

    private fun getApprovedHoursMonthly(assistancePlanDTOs: List<AssistancePlanDto>,
                                        clientSimpleDTOs: List<ClientSimpleDto>,
                                        hourTypeId: Long?,
                                        year: Int,
                                        month: Int): List<AssistancePlanOverviewDTO> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val assistancePlanOverviewDTOs =
                getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientSimpleDTOs, daysInMonth)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]

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
        convertDoubleToMinuteDouble(assistancePlanOverviewDTOs)

        return assistancePlanOverviewDTOs;
    }

    private fun getApprovedHoursYearly(assistancePlanDTOs: List<AssistancePlanDto>,
                                       clientSimpleDTOs: List<ClientSimpleDto>,
                                       hourTypeId: Long?,
                                       year: Int): List<AssistancePlanOverviewDTO> {
        val assistancePlanOverviewDTOs =
                getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientSimpleDTOs, MONTH_COUNT)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]

        assistancePlanDTOs.forEach { assistancePlanDto ->
            val assistancePlanOverviewDTO =
                    assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == assistancePlanDto.id }
            val hoursPerDay = getDailyHoursOfAssistancePlanByHourType(assistancePlanDto, hourTypeId)

            if (assistancePlanOverviewDTO != null) {
                for (i in 1..MONTH_COUNT) {
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
        convertDoubleToMinuteDouble(assistancePlanOverviewDTOs)

        return assistancePlanOverviewDTOs;
    }

    private fun getExecutedHoursYearlyByAssistancePlansAndYear(services: List<de.vinz.openfls.model.Service>,
                                                               assistancePlanDTOs: List<AssistancePlanDto>,
                                                               clientDTOs: List<ClientSimpleDto>): List<AssistancePlanOverviewDTO> {
        val assistancePlanOverviewDTOs = getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientDTOs, MONTH_COUNT)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]

        services.forEach { service ->
            val assistancePlanOverviewDTO = assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == service.assistancePlan.id }
            if (assistancePlanOverviewDTO != null) {
                val month = service.start.monthValue;
                assistancePlanOverviewDTO.values[0] += service.minutes.toDouble()
                assistancePlanOverviewDTO.values[month] += service.minutes.toDouble()

                allAssistancePlanOverviewDTO.values[0] += service.minutes.toDouble()
                allAssistancePlanOverviewDTO.values[month] += service.minutes.toDouble()
            }
        }
        
        // convert from minutes to hours
        convertMinutesValuesToHourValues(assistancePlanOverviewDTOs)

        return assistancePlanOverviewDTOs
    }

    private fun getExecutedHoursMonthlyByAssistancePlansAndYear(services: List<de.vinz.openfls.model.Service>,
                                                                assistancePlanDTOs: List<AssistancePlanDto>,
                                                                clientDTOs: List<ClientSimpleDto>,
                                                                year: Int,
                                                                month: Int): List<AssistancePlanOverviewDTO> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        val assistancePlanOverviewDTOs = getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientDTOs, daysInMonth)
        val allAssistancePlanOverviewDTO = assistancePlanOverviewDTOs[0]


        services.forEach { service ->
            val assistancePlanOverviewDTO = assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == service.assistancePlan.id }
            if (assistancePlanOverviewDTO != null) {
                val day = service.start.dayOfMonth;
                assistancePlanOverviewDTO.values[0] += service.minutes.toDouble()
                assistancePlanOverviewDTO.values[day] += service.minutes.toDouble()

                allAssistancePlanOverviewDTO.values[0] += service.minutes.toDouble()
                allAssistancePlanOverviewDTO.values[day] += service.minutes.toDouble()
            }
        }

        // convert from minutes to hours
        convertMinutesValuesToHourValues(assistancePlanOverviewDTOs)

        return assistancePlanOverviewDTOs
    }

    private fun convertDoubleToMinuteDouble(assistancePlanOverviewDTOs: List<AssistancePlanOverviewDTO>) {
        assistancePlanOverviewDTOs.forEach { assistancePlanOverviewDTO ->
            for (i in 0 until assistancePlanOverviewDTO.values.size) {
                assistancePlanOverviewDTO.values[i] =
                        NumberService.convertDoubleToTimeDouble(assistancePlanOverviewDTO.values[i])
            }
        }
    }

    private fun convertMinutesValuesToHourValues(assistancePlanOverviewDTOs: List<AssistancePlanOverviewDTO>) {
        assistancePlanOverviewDTOs.forEach { assistancePlanOverviewDTO ->
            for (i in 0 until assistancePlanOverviewDTO.values.size) {
                assistancePlanOverviewDTO.values[i] =
                        NumberService.convertDoubleToTimeDouble(assistancePlanOverviewDTO.values[i] / 60)
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
                        .filter { DateService.containsStartAndEndASpecificYearMonth(it.start, it.end, YearMonth.of(year, month))}
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
                        .filter { DateService.containsStartAndEndASpecificYearMonth(it.start, it.end, YearMonth.of(year, month))}
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
                        .filter { DateService.containsStartAndEndASpecificYearMonth(it.start, it.end, YearMonth.of(year, month))}
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
                    .filter { DateService.containsStartAndEndASpecificYearMonth(it.start, it.end, YearMonth.of(year, month))}
        }
    }

    private fun getServices(
            year: Int,
            month: Int?,
            hourTypeId: Long,
            areaId: Long?,
            sponsorId: Long?): List<de.vinz.openfls.model.Service> {
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
                        DoubleArray(valuesCount + 1) { 0.0 })}
                .sortedBy { it.clientDto.lastName }
                .toMutableList()

        result.add(0, AssistancePlanOverviewDTO(AssistancePlanDto(), allClient, DoubleArray(valuesCount + 1) { 0.0 }))

        return result;
    }

    private fun getDailyHoursOfAssistancePlanByHourType(assistancePlanDto: AssistancePlanDto, hourTypeId: Long?): Double =
            if (hourTypeId == null) {
                0.0
            } else if (assistancePlanDto.hours.size > 0) {
                NumberService.roundDoubleToTwoDigits(
                        assistancePlanDto.hours
                                .filter { it.hourTypeId == hourTypeId }
                                .sumOf { it.weeklyHours / 7 })
            } else {
                NumberService.roundDoubleToTwoDigits(
                        assistancePlanDto.goals
                                .flatMap { it.hours }
                                .filter { it.hourTypeId == hourTypeId }
                                .sumOf { it.weeklyHours / 7 })
            }
}