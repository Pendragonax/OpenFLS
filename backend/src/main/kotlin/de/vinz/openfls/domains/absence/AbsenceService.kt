package de.vinz.openfls.domains.absence

import de.vinz.openfls.domains.absence.dtos.EmployeeAbsenceResponseDTO
import de.vinz.openfls.domains.absence.dtos.YearAbsenceDTO
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
        val employeeId = accessService.getId()
        val existing = absenceRepository.findByEmployeeIdAndAbsenceDate(employeeId, absenceDate)
        val entity = existing ?: absenceRepository.save(Absence(id = 0, absenceDate, employeeId))
        return EmployeeAbsenceResponseDTO(
            employeeId = entity.employeeId,
            absenceDates = listOf(entity.absenceDate)
        )
    }

    @Transactional
    fun remove(absenceDate: LocalDate) {
        val entity = absenceRepository.findByEmployeeIdAndAbsenceDate(accessService.getId(), absenceDate) ?: return
        absenceRepository.deleteById(entity.id)
    }

    @Transactional
    fun getAllByEmployeeId(employeeId: Long): EmployeeAbsenceResponseDTO {
        val entities = absenceRepository.findAllByEmployeeId(employeeId)
        return EmployeeAbsenceResponseDTO(
            employeeId = employeeId,
            absenceDates = entities.map { it.absenceDate }
        )
    }

    @Transactional
    fun getAllByYear(year: Int): YearAbsenceDTO {
        val entities = absenceRepository.findAllByYear(year)
        val employeeIds = entities.map { it.employeeId }.distinct()

        val employeeAbsences = mutableListOf<EmployeeAbsenceResponseDTO>()
        for (employeeId in employeeIds) {
            val absenceDates = entities.filter { it.employeeId == employeeId }.map { it.absenceDate }
            employeeAbsences.add(EmployeeAbsenceResponseDTO(
                employeeId = employeeId,
                absenceDates = absenceDates
            ))
        }
        return YearAbsenceDTO(
            year = year,
            employeeAbsences = employeeAbsences
        )
    }
}
