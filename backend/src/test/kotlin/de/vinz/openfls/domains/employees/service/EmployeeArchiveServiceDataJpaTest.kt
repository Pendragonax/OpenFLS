package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.domains.employees.dto.EmployeeArchiveResult
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.employees.entity.EmployeeArchiveActionType.ARCHIVE
import de.vinz.openfls.domains.employees.entity.EmployeeArchiveActionType.REACTIVATE
import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.permissions.service.PermissionService
import de.vinz.openfls.domains.sponsors.service.SponsorService
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate

@DataJpaTest
@Import(
    EmployeeArchiveService::class,
    EmployeeService::class,
    EmployeeAccessService::class,
    UnprofessionalService::class,
    SponsorService::class,
    PermissionService::class,
    TestBeans::class
)
class EmployeeArchiveServiceDataJpaTest {

    @Autowired
    lateinit var employeeArchiveService: EmployeeArchiveService

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @MockitoBean
    lateinit var accessService: AccessService

    private val actionDate = LocalDate.of(2026, 7, 4)

    @BeforeEach
    fun setUp() {
        val actor = employeeRepository.save(Employee(firstname = "Anna", lastname = "Lead"))
        whenever(accessService.getId()).thenReturn(actor.id)
    }

    @Test
    fun archive_activeEmployee_persistsHistoryEntryWithTheExecutingEmployee() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))

        // When
        val result = employeeArchiveService.archive(employee.id!!, actionDate, "Archived by request", "Initial archive")

        // Then
        val entry = (result as EmployeeArchiveResult.Success).response
        val saved = employeeRepository.findById(employee.id!!).get()
        assertThat(saved.archived).isTrue()
        assertThat(saved.archiveHistoryEntries).hasSize(1)
        assertThat(entry.actionType).isEqualTo(ARCHIVE)
        assertThat(entry.executingEmployeeFirstname).isEqualTo("Anna")
        assertThat(entry.executingEmployeeLastname).isEqualTo("Lead")
    }

    @Test
    fun archive_alreadyArchivedEmployee_returnsAlreadyArchived() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann", archived = true))

        // When
        val result = employeeArchiveService.archive(employee.id!!, actionDate, "Archived by request", "Initial archive")

        // Then
        assertThat(result).isEqualTo(EmployeeArchiveResult.AlreadyArchived)
        assertThat(employeeRepository.findById(employee.id!!).get().archiveHistoryEntries).isEmpty()
    }

    @Test
    fun archive_unknownEmployee_returnsNotFound() {
        assertThat(employeeArchiveService.archive(9999, actionDate, "Reason", "Remark"))
            .isEqualTo(EmployeeArchiveResult.NotFound)
    }

    @Test
    fun archive_executingEmployeeDoesNotExist_returnsActorNotFound() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        whenever(accessService.getId()).thenReturn(0L)

        // When
        val result = employeeArchiveService.archive(employee.id!!, actionDate, "Reason", "Remark")

        // Then
        assertThat(result).isEqualTo(EmployeeArchiveResult.ActorNotFound)
        assertThat(employeeRepository.findById(employee.id!!).get().archived).isFalse()
    }

    @Test
    fun reactivate_archivedEmployee_restoresActiveStateAndListsNewestFirst() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        employeeArchiveService.archive(employee.id!!, actionDate, "Archived by request", "Initial archive")

        // When
        val result = employeeArchiveService.reactivate(employee.id!!, actionDate, "Employee active again", "Reactivated")

        // Then
        val entry = (result as EmployeeArchiveResult.Success).response
        val saved = employeeRepository.findById(employee.id!!).get()
        assertThat(saved.archived).isFalse()
        assertThat(saved.archiveHistoryEntries).hasSize(2)
        assertThat(entry.actionType).isEqualTo(REACTIVATE)
        val history = employeeArchiveService.getHistory(employee.id!!)!!
        assertThat(history).hasSize(2)
        assertThat(history[0].actionType).isEqualTo(REACTIVATE)
        assertThat(history[1].actionType).isEqualTo(ARCHIVE)
    }

    @Test
    fun reactivate_activeEmployee_returnsNotArchived() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))

        // When
        val result = employeeArchiveService.reactivate(employee.id!!, actionDate, "Employee active again", "Reactivated")

        // Then
        assertThat(result).isEqualTo(EmployeeArchiveResult.NotArchived)
    }

    @Test
    fun getArchiveHistory_withoutHistory_returnsEmptyList() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))

        // When / Then
        assertThat(employeeArchiveService.getHistory(employee.id!!)).isEmpty()
    }

    @Test
    fun getArchiveHistory_unknownEmployee_returnsNull() {
        assertThat(employeeArchiveService.getHistory(9999)).isNull()
    }
}
