package de.vinz.openfls.domains.absence

import de.vinz.openfls.domains.absence.dtos.EmployeeAbsenceResponseDTO
import de.vinz.openfls.domains.permissions.AccessService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AbsenceService(private val absenceRepository: AbsenceRepository,
    private val accessService: AccessService
) {

    @Transactional
    fun create(absenceDate: LocalDate): EmployeeAbsenceResponseDTO {
        val entity = absenceRepository.save(Absence(id = 0, absenceDate, accessService.getId()))
        return EmployeeAbsenceResponseDTO(
            employeeId = entity.employeeId,
            absenceDates = listOf(entity.absenceDate)
        )
    }

    @Transactional
    fun remove(absenceDate: LocalDate) {
        val entity = absenceRepository.findByEmployeeIdAndAbsenceDate(accessService.getId(), absenceDate)
            ?: throw IllegalArgumentException("Absence not found")

        absenceRepository.remove(entity.id)
    }

    @Transactional
    fun getAllByEmployeeId(employeeId: Long): EmployeeAbsenceResponseDTO {
        val entities = absenceRepository.findAllByEmployeeId(employeeId)
        return EmployeeAbsenceResponseDTO(
            employeeId = employeeId,
            absenceDates = entities.map { it.absenceDate }
        )
    }
}