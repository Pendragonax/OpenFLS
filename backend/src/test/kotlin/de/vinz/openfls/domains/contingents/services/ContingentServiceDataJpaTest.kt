package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.domains.contingents.ContingentRepository
import de.vinz.openfls.domains.contingents.dtos.ContingentDto
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.institutions.InstitutionService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate

@DataJpaTest
@Import(ContingentService::class, TestBeans::class)
class ContingentServiceDataJpaTest {

    @Autowired
    lateinit var contingentService: ContingentService

    @Autowired
    lateinit var contingentRepository: ContingentRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @MockitoBean
    lateinit var employeeService: EmployeeService

    @MockitoBean
    lateinit var institutionService: InstitutionService

    @MockitoBean
    lateinit var accessService: AccessService

    @Test
    fun create_endBeforeStart_throwsException() {
        // Given
        val dto = ContingentDto().apply {
            start = LocalDate.of(2026, 2, 10)
            end = LocalDate.of(2026, 2, 1)
        }

        // When / Then
        assertThatThrownBy { contingentService.create(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun create_validDto_persistsEntity() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val dto = ContingentDto().apply {
            start = LocalDate.of(2026, 1, 1)
            end = LocalDate.of(2026, 12, 31)
            weeklyServiceHours = 20.0
            employeeId = employee.id!!
            institutionId = institution.id!!
        }
        whenever(employeeService.getById(dto.employeeId, true)).thenReturn(employee)
        whenever(institutionService.getEntityById(dto.institutionId)).thenReturn(institution)

        // When
        val result = contingentService.create(dto)

        // Then
        val saved = contingentRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().weeklyServiceHours).isEqualTo(20.0)
    }

    @Test
    fun update_missingContingent_throwsException() {
        // Given
        val dto = ContingentDto().apply {
            id = 9999
            start = LocalDate.of(2026, 1, 1)
            weeklyServiceHours = 10.0
        }

        // When / Then
        assertThatThrownBy { contingentService.update(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_endBeforeStart_throwsException() {
        // Given
        val existing = contingentRepository.save(de.vinz.openfls.domains.contingents.Contingent(
            id = 0,
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 1, 10),
            weeklyServiceHours = 10.0
        ))
        val dto = ContingentDto().apply {
            id = existing.id
            start = LocalDate.of(2026, 2, 10)
            end = LocalDate.of(2026, 2, 1)
            weeklyServiceHours = 12.0
        }

        // When / Then
        assertThatThrownBy { contingentService.update(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_validDto_updatesEntity() {
        // Given
        val existing = contingentRepository.save(de.vinz.openfls.domains.contingents.Contingent(
            id = 0,
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 1, 10),
            weeklyServiceHours = 10.0
        ))
        val dto = ContingentDto().apply {
            id = existing.id
            start = LocalDate.of(2026, 1, 1)
            end = LocalDate.of(2026, 1, 31)
            weeklyServiceHours = 12.0
        }

        // When
        val result = contingentService.update(dto)

        // Then
        val saved = contingentRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().weeklyServiceHours).isEqualTo(12.0)
    }
}
