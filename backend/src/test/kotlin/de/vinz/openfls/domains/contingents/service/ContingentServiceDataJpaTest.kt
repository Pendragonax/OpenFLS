package de.vinz.openfls.domains.contingents.service

import de.vinz.openfls.domains.contingents.entity.Contingent
import de.vinz.openfls.domains.contingents.repository.ContingentRepository
import de.vinz.openfls.domains.contingents.dto.ContingentCreateRequest
import de.vinz.openfls.domains.contingents.dto.ContingentCreateResult
import de.vinz.openfls.domains.contingents.dto.ContingentUpdateRequest
import de.vinz.openfls.domains.contingents.dto.ContingentUpdateResult
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.institutions.service.InstitutionService
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
    fun create_endBeforeStart_returnsInvalidRange() {
        // Given
        val dto = ContingentCreateRequest(
            start = LocalDate.of(2026, 2, 10),
            end = LocalDate.of(2026, 2, 1)
        )

        // When
        val result = contingentService.create(dto)

        // Then
        assertThat(result).isEqualTo(ContingentCreateResult.InvalidRange("end before start"))
    }

    @Test
    fun create_validDto_persistsEntity() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val dto = ContingentCreateRequest(
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 12, 31),
            weeklyServiceHours = 20.0,
            employeeId = employee.id!!,
            institutionId = institution.id!!
        )
        whenever(employeeService.getById(dto.employeeId)).thenReturn(employee)
        whenever(institutionService.getEntityById(dto.institutionId)).thenReturn(institution)

        // When
        val result = contingentService.create(dto) as ContingentCreateResult.Success

        // Then
        val saved = contingentRepository.findById(result.response.id)
        assertThat(saved).isPresent
        assertThat(saved.get().weeklyServiceHours).isEqualTo(20.0)
    }

    @Test
    fun update_missingContingent_returnsNotFound() {
        // Given
        val dto = ContingentUpdateRequest(
            id = 9999,
            start = LocalDate.of(2026, 1, 1),
            weeklyServiceHours = 10.0
        )

        // When
        val result = contingentService.update(dto)

        // Then
        assertThat(result).isEqualTo(ContingentUpdateResult.NotFound)
    }

    @Test
    fun update_endBeforeStart_returnsInvalidRange() {
        // Given
        val existing = contingentRepository.save(Contingent(
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 1, 10),
            weeklyServiceHours = 10.0
        ))
        val dto = ContingentUpdateRequest(
            id = existing.id,
            start = LocalDate.of(2026, 2, 10),
            end = LocalDate.of(2026, 2, 1),
            weeklyServiceHours = 12.0
        )

        // When
        val result = contingentService.update(dto)

        // Then
        assertThat(result).isEqualTo(ContingentUpdateResult.InvalidRange("end before start"))
    }

    @Test
    fun update_validDto_updatesEntity() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Erika", lastname = "Musterfrau"))
        val institution = institutionRepository.save(Institution(name = "UpdateInst", email = "u@b.c", phonenumber = "2"))
        val existing = contingentRepository.save(Contingent(
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 1, 10),
            weeklyServiceHours = 10.0,
            employee = employee,
            institution = institution
        ))
        val dto = ContingentUpdateRequest(
            id = existing.id,
            start = LocalDate.of(2026, 1, 1),
            end = LocalDate.of(2026, 1, 31),
            weeklyServiceHours = 12.0,
            employeeId = employee.id!!,
            institutionId = institution.id!!
        )
        whenever(employeeService.getById(dto.employeeId)).thenReturn(employee)
        whenever(institutionService.getEntityById(dto.institutionId)).thenReturn(institution)

        // When
        val result = contingentService.update(dto) as ContingentUpdateResult.Success

        // Then
        val saved = contingentRepository.findById(result.response.id)
        assertThat(saved).isPresent
        assertThat(saved.get().weeklyServiceHours).isEqualTo(12.0)
        assertThat(saved.get().employee?.id).isEqualTo(employee.id)
        assertThat(saved.get().institution?.id).isEqualTo(institution.id)
    }
}
