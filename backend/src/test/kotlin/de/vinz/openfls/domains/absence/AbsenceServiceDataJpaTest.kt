package de.vinz.openfls.domains.absence

import de.vinz.openfls.domains.permissions.AccessService
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
        val result = absenceService.create(absenceDate)

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
        absenceRepository.save(Absence(id = 0, absenceDate = absenceDate, employeeId = employeeId))

        // When
        val result = absenceService.create(absenceDate)

        // Then
        val all = absenceRepository.findAllByEmployeeId(employeeId)
        assertThat(all).hasSize(1)
        assertThat(result.absenceDates).containsExactly(absenceDate)
    }
}
