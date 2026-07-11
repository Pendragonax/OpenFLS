package de.vinz.openfls.domains.employees.archive

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.archive.EmployeeArchiveActionType.ARCHIVE
import de.vinz.openfls.domains.employees.archive.EmployeeArchiveActionType.REACTIVATE
import de.vinz.openfls.exceptions.UserNotAllowedException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@DataJpaTest
@Import(EmployeeArchiveService::class)
class EmployeeArchiveServiceDataJpaTest {

    @Autowired
    lateinit var employeeArchiveService: EmployeeArchiveService

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Test
    fun archive_withAdminPermission_persistsHistoryEntry() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val actor = EmployeeArchiveActor(
            employeeId = 8L,
            firstname = "Anna",
            lastname = "Lead",
            isAdmin = true
        )

        // When
        val entry = employeeArchiveService.archive(
            employeeId = employee.id!!,
            actionDate = LocalDate.of(2026, 7, 4),
            reason = "Archived by request",
            remark = "Initial archive",
            actor = actor
        )

        // Then
        val saved = employeeRepository.findById(employee.id!!)
        assertThat(saved).isPresent
        assertThat(saved.get().archived).isTrue()
        assertThat(saved.get().archiveHistoryEntries).hasSize(1)
        assertThat(entry.actionType).isEqualTo(ARCHIVE)
        assertThat(entry.executingEmployeeFirstname).isEqualTo("Anna")
        assertThat(entry.executingEmployeeLastname).isEqualTo("Lead")
    }

    @Test
    fun archive_withoutAdminPermission_throwsUserNotAllowedException() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val actor = EmployeeArchiveActor(
            employeeId = 8L,
            firstname = "Anna",
            lastname = "Employee",
            isAdmin = false
        )

        // When / Then
        assertThatThrownBy {
            employeeArchiveService.archive(
                employeeId = employee.id!!,
                actionDate = LocalDate.of(2026, 7, 4),
                reason = "Archived by request",
                remark = "Initial archive",
                actor = actor
            )
        }.isInstanceOf(UserNotAllowedException::class.java)
    }

    @Test
    fun reactivate_withAdminPermission_restoresArchiveState() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val actor = EmployeeArchiveActor(
            employeeId = 8L,
            firstname = "Anna",
            lastname = "Lead",
            isAdmin = true
        )
        employeeArchiveService.archive(
            employeeId = employee.id!!,
            actionDate = LocalDate.of(2026, 7, 4),
            reason = "Archived by request",
            remark = "Initial archive",
            actor = actor
        )

        // When
        val entry = employeeArchiveService.reactivate(
            employeeId = employee.id!!,
            actionDate = LocalDate.of(2026, 7, 4),
            reason = "Employee active again",
            remark = "Reactivated",
            actor = actor
        )

        // Then
        val saved = employeeRepository.findById(employee.id!!)
        assertThat(saved).isPresent
        assertThat(saved.get().archived).isFalse()
        assertThat(saved.get().archiveHistoryEntries).hasSize(2)
        assertThat(entry.actionType).isEqualTo(REACTIVATE)
        val history = employeeArchiveService.getArchiveHistory(employee.id!!)
        assertThat(history).hasSize(2)
        assertThat(history[0].actionType).isEqualTo(REACTIVATE)
        assertThat(history[1].actionType).isEqualTo(ARCHIVE)
    }

    @Test
    fun reactivate_withoutArchiveState_throwsStateException() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val actor = EmployeeArchiveActor(
            employeeId = 8L,
            firstname = "Anna",
            lastname = "Lead",
            isAdmin = true
        )

        // When / Then
        assertThatThrownBy {
            employeeArchiveService.reactivate(
                employeeId = employee.id!!,
                actionDate = LocalDate.of(2026, 7, 4),
                reason = "Employee active again",
                remark = "Reactivated",
                actor = actor
            )
        }.isInstanceOf(EmployeeArchiveStateException::class.java)
            .hasMessage("employee is not archived")
    }

    @Test
    fun getArchiveHistory_withoutHistory_returnsEmptyList() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))

        // When
        val history = employeeArchiveService.getArchiveHistory(employee.id!!)

        // Then
        assertThat(history).isEmpty()
    }
}
