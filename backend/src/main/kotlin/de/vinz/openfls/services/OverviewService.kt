package de.vinz.openfls.services

import de.vinz.openfls.controller.OverviewController
import de.vinz.openfls.dtos.AssistancePlanDto
import de.vinz.openfls.dtos.AssistancePlanOverviewDTO
import de.vinz.openfls.dtos.ClientSimpleDto
import de.vinz.openfls.repositories.AssistancePlanRepository
import de.vinz.openfls.repositories.ClientRepository
import de.vinz.openfls.repositories.ServiceRepository
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Service

@Service
class OverviewService(
        private val permissionService: PermissionService,
        private val serviceRepository: ServiceRepository,
        private val assistancePlanRepository: AssistancePlanRepository,
        private val clientRepository: ClientRepository,
        private val modelMapper: ModelMapper) {
    fun getExecutedHoursOverviewFromAssistancePlanByYear(
            year: Int,
            hourTypeId: Long,
            areaId: Long,
            sponsorId: Long): List<AssistancePlanOverviewDTO> {
        return getExecutedHoursOverviewFromAssistancePlanByYearAndMonth(
                year, 0, hourTypeId, areaId, sponsorId);
    }

    fun getExecutedHoursOverviewFromAssistancePlanByYearAndMonth(
            year: Int,
            month: Int,
            hourTypeId: Long,
            areaId: Long,
            sponsorId: Long): List<AssistancePlanOverviewDTO> {
        val services = serviceRepository
                .findServiceByYearByHourTypeIdAndAreaIdAndSponsorId(
                    year = year,
                    hourTypeId = hourTypeId,
                    areaId = areaId,
                    sponsorId = sponsorId);

        val assistancePlanDTOs = assistancePlanRepository
                .findByInstitutionIdAndSponsorId(areaId, sponsorId)
                .map { modelMapper.map(it, AssistancePlanDto::class.java) }

        val clientDTOs = clientRepository.findAll().map { modelMapper.map(it, ClientSimpleDto::class.java) };

        return extractExecutedHoursYearlyByAssistancePlansAndYear(services, assistancePlanDTOs, clientDTOs, year);
    }

    private fun extractExecutedHoursYearlyByAssistancePlansAndYear(services: List<de.vinz.openfls.model.Service>,
                                                                   assistancePlanDTOs: List<AssistancePlanDto>,
                                                                   clientDTOs: List<ClientSimpleDto>,
                                                                   year: Int): List<AssistancePlanOverviewDTO> {
        val numberOfDays = getNumberOfDaysInYear(year);

        val result = assistancePlanDTOs.map {
            AssistancePlanOverviewDTO(it,
                    clientDTOs.find { client -> client.id == it.clientId } ?: throw IllegalArgumentException(),
                    DoubleArray(numberOfDays) { 0.0 })
        }

        services.forEach { service ->
            val resultEntity = result.find { it.assistancePlanDto.id == service.assistancePlan.id }
            if (resultEntity != null) {
                val dayOfYear = service.start.dayOfYear - 1;
                resultEntity.values[dayOfYear] += resultEntity.values[dayOfYear] + service.minutes;
            }
        }

        return result;
    }

    private fun getNumberOfDaysInYear(year: Int): Int {
        // Check if it's a leap year (divisible by 4, not divisible by 100, or divisible by 400)
        return if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            366 // Leap year has 366 days
        } else {
            365 // Regular year has 365 days
        }
    }
}