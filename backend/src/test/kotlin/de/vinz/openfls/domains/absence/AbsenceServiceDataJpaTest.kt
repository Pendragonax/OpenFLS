package de.vinz.openfls.domains.absence

import de.vinz.openfls.domains.absence.dto.AbsenceCreateRequest
import de.vinz.openfls.domains.absence.entity.Absence
import de.vinz.openfls.domains.absence.service.AbsenceService
import de.vinz.openfls.domains.absence.repository.AbsenceRepository
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate

@DataJpaTest
@Import(AbsenceService::class, TestBeans::class)
class AbsenceServiceDataJpaTest {

    @Autowired
    lateinit var absenceService: AbsenceService

    @Autowired
    lateinit var absenceRepository: AbsenceRepository

    @MockitoBean
    lateinit var accessService: AccessService

    @Test
    fun create_newAbsence_persistsEntry() {
        // Given
        val employeeId = 10L
        val absenceDate = LocalDate.of(2026, 2, 1)
        whenever(accessService.getId()).thenReturn(employeeId)

        // When
        val result = absenceService.create(AbsenceCreateRequest(absenceDate))

        // Then
        val saved = absenceRepository.findByEmployeeIdAndAbsenceDate(employeeId, absenceDate)
        assertThat(saved).isNotNull
        assertThat(result.employeeId).isEqualTo(employeeId)
        assertThat(result.absenceDates).containsExactly(absenceDate)
    }

    @Test
    fun create_existingAbsence_returnsExistingWithoutDuplicate() {
        // Given
        val employeeId = 12L
        val absenceDate = LocalDate.of(2026, 1, 31)
        whenever(accessService.getId()).thenReturn(employeeId)
        absenceRepository.save(Absence(absenceDate = absenceDate, employeeId = employeeId))

        // When
        val result = absenceService.create(AbsenceCreateRequest(absenceDate))

        // Then
        val all = absenceRepository.findAllByEmployeeId(employeeId)
        assertThat(all).hasSize(1)
        assertThat(result.absenceDates).containsExactly(absenceDate)
    }
    @Test
    fun delete_existingAbsence_removesEntry() {
        // Given
        val employeeId = 14L
        val absenceDate = LocalDate.of(2026, 3, 2)
        whenever(accessService.getId()).thenReturn(employeeId)
        absenceRepository.save(Absence(absenceDate = absenceDate, employeeId = employeeId))

        // When
        absenceService.delete(absenceDate)

        // Then
        assertThat(absenceRepository.findByEmployeeIdAndAbsenceDate(employeeId, absenceDate)).isNull()
    }

    @Test
    fun getAllEntitiesByYear_returnsOnlyAbsencesOfThatYear() {
        // Given
        absenceRepository.save(Absence(absenceDate = LocalDate.of(2025, 12, 31), employeeId = 1))
        absenceRepository.save(Absence(absenceDate = LocalDate.of(2026, 1, 1), employeeId = 1))
        absenceRepository.save(Absence(absenceDate = LocalDate.of(2026, 6, 1), employeeId = 2))

        // When
        val result = absenceService.getAllEntitiesByYear(2026)

        // Then
        assertThat(result.map { it.absenceDate })
            .containsExactlyInAnyOrder(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1))
    }

    @Test
    fun getAllByEmployeeId_returnsResponseWithAllDates() {
        // Given
        absenceRepository.save(Absence(absenceDate = LocalDate.of(2026, 1, 1), employeeId = 5))
        absenceRepository.save(Absence(absenceDate = LocalDate.of(2026, 1, 2), employeeId = 6))

        // When
        val result = absenceService.getAllByEmployeeId(5)

        // Then
        assertThat(result.employeeId).isEqualTo(5)
        assertThat(result.absenceDates).containsExactly(LocalDate.of(2026, 1, 1))
    }
}

