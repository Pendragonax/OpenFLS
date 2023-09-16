package de.vinz.openfls.services

import de.vinz.openfls.dtos.AssistancePlanDto
import de.vinz.openfls.dtos.AssistancePlanOverviewDTO
import de.vinz.openfls.dtos.ClientSimpleDto
import de.vinz.openfls.model.AssistancePlan
import de.vinz.openfls.repositories.AssistancePlanRepository
import de.vinz.openfls.repositories.ClientRepository
import de.vinz.openfls.repositories.ServiceRepository
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.util.*

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

        val clientDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) };

        if (month != null) {
            return getExecutedHoursMonthlyByAssistancePlansAndYear(services, assistancePlanDTOs, clientDTOs, year, month)
        }

        return getExecutedHoursYearlyByAssistancePlansAndYear(services, assistancePlanDTOs, clientDTOs)
    }

    private fun getAssistancePlans(
            areaId: Long?,
            sponsorId: Long?) : List<AssistancePlan> {
        if (areaId != null && sponsorId != null) {
            return assistancePlanRepository
                    .findByInstitutionIdAndSponsorId(areaId, sponsorId)
        } else if (areaId != null) {
            return assistancePlanRepository
                    .findByInstitutionId(areaId);
        } else if (sponsorId != null) {
            return assistancePlanRepository
                    .findBySponsorId(sponsorId);
        }

        return emptyList();
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

    private fun getExecutedHoursYearlyByAssistancePlansAndYear(services: List<de.vinz.openfls.model.Service>,
                                                               assistancePlanDTOs: List<AssistancePlanDto>,
                                                               clientDTOs: List<ClientSimpleDto>): List<AssistancePlanOverviewDTO> {
        val result = assistancePlanDTOs.map {
            AssistancePlanOverviewDTO(it,
                    clientDTOs.find { client -> client.id == it.clientId } ?: throw IllegalArgumentException(),
                    DoubleArray(12) { 0.0 })
        }

        services.forEach { service ->
            val resultEntity = result.find { it.assistancePlanDto.id == service.assistancePlan.id }
            if (resultEntity != null) {
                val month = service.start.monthValue - 1;
                resultEntity.values[month] = resultEntity.values[month] + service.minutes;
            }
        }

        return result;
    }

    private fun getExecutedHoursMonthlyByAssistancePlansAndYear(services: List<de.vinz.openfls.model.Service>,
                                                                assistancePlanDTOs: List<AssistancePlanDto>,
                                                                clientDTOs: List<ClientSimpleDto>,
                                                                year: Int,
                                                                month: Int): List<AssistancePlanOverviewDTO> {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        val result = assistancePlanDTOs.map {
            AssistancePlanOverviewDTO(it,
                    clientDTOs.find { client -> client.id == it.clientId } ?: throw IllegalArgumentException(),
                    DoubleArray(daysInMonth) { 0.0 })
        }


        services.forEach { service ->
            val resultEntity = result.find { it.assistancePlanDto.id == service.assistancePlan.id }
            if (resultEntity != null) {
                val day = service.start.dayOfMonth - 1;
                resultEntity.values[day] = resultEntity.values[day] + service.minutes;
            }
        }

        return result;
    }
}