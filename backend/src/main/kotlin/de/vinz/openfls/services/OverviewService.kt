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

        val assistancePlanDTOs = getAssistancePlans(areaId, sponsorId)
                .map { modelMapper.map(it, AssistancePlanDto::class.java) }

        val clientSimpleDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) };

        // Monthly
        if (month != null) {
            return getExecutedHoursMonthlyByAssistancePlansAndYear(
                    services, assistancePlanDTOs, clientSimpleDTOs, year, month)
        }

        // Yearly
        return getExecutedHoursYearlyByAssistancePlansAndYear(services, assistancePlanDTOs, clientSimpleDTOs)
    }

    fun getApprovedHoursOverview(year: Int,
                                 month: Int?,
                                 hourTypeId: Long,
                                 areaId: Long?,
                                 sponsorId: Long?): List<AssistancePlanOverviewDTO> {
        val assistancePlanDTOs = getAssistancePlans(areaId, sponsorId)

        val clientSimpleDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) }

        // Monthly
        if (month != null) {
            return getApprovedHoursMonthly(
                    assistancePlanDTOs, clientSimpleDTOs, year, month)
        }

        // Yearly
        return getApprovedHoursYearly(assistancePlanDTOs, clientSimpleDTOs, year)
    }

    private fun getApprovedHoursMonthly(assistancePlanDTOs: List<AssistancePlanDto>,
                                        clientSimpleDTOs: List<ClientSimpleDto>,
                                        year: Int,
                                        month: Int): List<AssistancePlanOverviewDTO> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val assistancePlanOverviewDTOs =
                getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientSimpleDTOs, daysInMonth)

        assistancePlanDTOs.forEach { assistancePlanDto ->
            val assistancePlanOverviewDTO =
                    assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == assistancePlanDto.id }
            val hoursPerDay = NumberService.convertDoubleToTimeDouble(assistancePlanDto.hours.sumOf { it.weeklyHours / 7 })

            if (assistancePlanOverviewDTO != null) {
                for (i in 0 until daysInMonth) {
                    if (DateService.isDateInAssistancePlan(
                                    LocalDate.of(year, month, i + 1), assistancePlanDto)) {
                        assistancePlanOverviewDTO.values[i] = hoursPerDay
                    } else {
                        assistancePlanOverviewDTO.values[i] = 0.0
                    }
                }
            }
        }

        return assistancePlanOverviewDTOs;
    }

    private fun getApprovedHoursYearly(assistancePlanDTOs: List<AssistancePlanDto>,
                                        clientSimpleDTOs: List<ClientSimpleDto>,
                                        year: Int): List<AssistancePlanOverviewDTO> {
        val assistancePlanOverviewDTOs =
                getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientSimpleDTOs, MONTH_COUNT)

        assistancePlanDTOs.forEach { assistancePlanDto ->
            val assistancePlanOverviewDTO =
                    assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == assistancePlanDto.id }
            val hoursPerDay = assistancePlanDto.hours.sumOf { it.weeklyHours / 7 }

            if (assistancePlanOverviewDTO != null) {
                for (i in 0 until MONTH_COUNT) {
                    val daysInMonth = DateService.countDaysOfAssistancePlan(year, i+1, assistancePlanDto)
                    assistancePlanOverviewDTO.values[i] =
                            NumberService.convertDoubleToTimeDouble(hoursPerDay * daysInMonth)
                }
            }
        }

        return assistancePlanOverviewDTOs;
    }

    private fun getExecutedHoursYearlyByAssistancePlansAndYear(services: List<de.vinz.openfls.model.Service>,
                                                               assistancePlanDTOs: List<AssistancePlanDto>,
                                                               clientDTOs: List<ClientSimpleDto>): List<AssistancePlanOverviewDTO> {
        val assistancePlanOverviewDTOs = getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientDTOs, MONTH_COUNT)

        services.forEach { service ->
            val assistancePlanOverviewDTO = assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == service.assistancePlan.id }
            if (assistancePlanOverviewDTO != null) {
                addMonthlyServiceMinutesToOverviewValues(service, assistancePlanOverviewDTO)
            }
        }
        
        // convert from minutes to hours
        convertMinutesValuesToHourValues(assistancePlanOverviewDTOs)

        return assistancePlanOverviewDTOs.sortedBy { it.clientDto.lastName }
    }

    private fun getExecutedHoursMonthlyByAssistancePlansAndYear(services: List<de.vinz.openfls.model.Service>,
                                                                assistancePlanDTOs: List<AssistancePlanDto>,
                                                                clientDTOs: List<ClientSimpleDto>,
                                                                year: Int,
                                                                month: Int): List<AssistancePlanOverviewDTO> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        val assistancePlanOverviewDTOs = getAssistancePlanOverviewDTOS(assistancePlanDTOs, clientDTOs, daysInMonth)


        services.forEach { service ->
            val resultEntity = assistancePlanOverviewDTOs.find { it.assistancePlanDto.id == service.assistancePlan.id }
            if (resultEntity != null) {
                addDailyServiceMinutesToOverviewValues(service, resultEntity)
            }
        }

        // convert from minutes to hours
        convertMinutesValuesToHourValues(assistancePlanOverviewDTOs)


        return assistancePlanOverviewDTOs.sortedBy { it.clientDto.lastName }
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
            institutionId: Long?,
            sponsorId: Long?) : List<AssistancePlanDto> {
        if (institutionId != null && sponsorId != null) {
            return assistancePlanRepository
                    .findByInstitutionIdAndSponsorId(institutionId, sponsorId)
                    .map { modelMapper.map(it, AssistancePlanDto::class.java) }
        } else if (institutionId != null) {
            return assistancePlanRepository
                    .findByInstitutionId(institutionId)
                    .map { modelMapper.map(it, AssistancePlanDto::class.java) }
        } else if (sponsorId != null) {
            return assistancePlanRepository
                    .findBySponsorId(sponsorId)
                    .map { modelMapper.map(it, AssistancePlanDto::class.java) }
        }

        return assistancePlanRepository
                .findAll().toList()
                .map { modelMapper.map(it, AssistancePlanDto::class.java) }
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

    private fun addMonthlyServiceMinutesToOverviewValues(service: de.vinz.openfls.model.Service,
                                                         resultEntity: AssistancePlanOverviewDTO) {
        val month = service.start.monthValue - 1;
        resultEntity.values[month] =
                NumberService.convertDoubleToTimeDouble(resultEntity.values[month] + service.minutes)
    }

    private fun addDailyServiceMinutesToOverviewValues(service: de.vinz.openfls.model.Service,
                                                       resultEntity: AssistancePlanOverviewDTO) {
        val day = service.start.dayOfMonth - 1;
        resultEntity.values[day] =
                NumberService.convertDoubleToTimeDouble(resultEntity.values[day] + service.minutes)
    }

    private fun getAssistancePlanOverviewDTOS(assistancePlanDTOs: List<AssistancePlanDto>,
                                              clientDTOs: List<ClientSimpleDto>,
                                              valuesCount: Int): List<AssistancePlanOverviewDTO> {
        return assistancePlanDTOs.map {
            AssistancePlanOverviewDTO(it,
                    clientDTOs.find { client -> client.id == it.clientId } ?: throw IllegalArgumentException(),
                    DoubleArray(valuesCount) { 0.0 })
        };
    }
}