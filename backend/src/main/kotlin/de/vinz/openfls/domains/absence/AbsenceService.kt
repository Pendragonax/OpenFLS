package de.vinz.openfls.domains.absence

import de.vinz.openfls.domains.absence.dtos.EmployeeAbsenceResponseDto
import de.vinz.openfls.domains.absence.dtos.YearAbsenceDto
import de.vinz.openfls.domains.permissions.AccessService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AbsenceService(private val absenceRepository: AbsenceRepository,
    private val accessService: AccessService
) {

    @Transactional
    fun create(absenceDate: LocalDate): EmployeeAbsenceResponseDto {
        val employeeId = accessService.getId()
        val existing = absenceRepository.findByEmployeeIdAndAbsenceDate(employeeId, absenceDate)
        val entity = existing ?: absenceRepository.save(Absence(id = 0, absenceDate, employeeId))
        return EmployeeAbsenceResponseDto(
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
    fun getAllByEmployeeId(employeeId: Long): EmployeeAbsenceResponseDto {
        val entities = absenceRepository.findAllByEmployeeId(employeeId)
        return EmployeeAbsenceResponseDto(
            employeeId = employeeId,
            absenceDates = entities.map { it.absenceDate }
        )
    }

    @Transactional
    fun getAllByYear(year: Int): YearAbsenceDto {
        val entities = absenceRepository.findAllByAbsenceDateBetween(
            LocalDate.of(year, 1, 1),
            LocalDate.of(year, 12, 31)
        )
        val employeeIds = entities.map { it.employeeId }.distinct()

        val employeeAbsences = mutableListOf<EmployeeAbsenceResponseDto>()
        for (employeeId in employeeIds) {
            val absenceDates = entities.filter { it.employeeId == employeeId }.map { it.absenceDate }
            employeeAbsences.add(EmployeeAbsenceResponseDto(
                employeeId = employeeId,
                absenceDates = absenceDates
            ))
        }
        return YearAbsenceDto(
            year = year,
            employeeAbsences = employeeAbsences
        )
    }
}
