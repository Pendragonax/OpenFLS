package de.vinz.openfls.domains.absence

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.absence.dtos.AbsenceCreateRequest
import de.vinz.openfls.domains.absence.dtos.EmployeeAbsenceResponse
import de.vinz.openfls.domains.permissions.AccessService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AbsenceService(
    private val absenceRepository: AbsenceRepository,
    private val accessService: AccessService
) {

    @Transactional
    fun create(request: AbsenceCreateRequest): EmployeeAbsenceResponse {
        val employeeId = accessService.getId()
        val existing = absenceRepository.findByEmployeeIdAndAbsenceDate(employeeId, request.absenceDate)
        val entity = existing ?: absenceRepository.save(Absence(absenceDate = request.absenceDate, employeeId = employeeId))
        return EmployeeAbsenceResponse(
            employeeId = entity.employeeId,
            absenceDates = listOf(entity.absenceDate)
        )
    }

    @Transactional
    fun delete(absenceDate: LocalDate) {
        val entity = absenceRepository.findByEmployeeIdAndAbsenceDate(accessService.getId(), absenceDate) ?: return
        absenceRepository.deleteById(entity.id)
    }

    @Transactional(readOnly = true)
    fun getAllByEmployeeId(employeeId: Long): EmployeeAbsenceResponse {
        return EmployeeAbsenceResponse(
            employeeId = employeeId,
            absenceDates = absenceRepository.findAllByEmployeeId(employeeId).map { it.absenceDate }
        )
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByEmployeeId(employeeId: Long): List<Absence> {
        return absenceRepository.findAllByEmployeeId(employeeId)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByYear(year: Int): List<Absence> {
        return absenceRepository.findAllByAbsenceDateBetween(
            LocalDate.of(year, 1, 1),
            LocalDate.of(year, 12, 31)
        )
    }
}
