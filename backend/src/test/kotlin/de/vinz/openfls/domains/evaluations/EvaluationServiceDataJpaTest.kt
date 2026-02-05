package de.vinz.openfls.domains.evaluations

import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.evaluations.dtos.EvaluationRequestDto
import de.vinz.openfls.testsupport.TestBeans
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@DataJpaTest
@Import(EvaluationService::class, TestBeans::class)
class EvaluationServiceDataJpaTest {

    @Autowired
    lateinit var evaluationService: EvaluationService

    @Autowired
    lateinit var evaluationRepository: EvaluationRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Test
    fun create_validDto_persistsEntity() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val dto = EvaluationRequestDto().apply {
            date = LocalDate.of(2026, 2, 1)
            content = "Test"
            approved = true
        }

        // When
        val result = evaluationService.create(dto, employee)

        // Then
        val saved = evaluationRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().content).isEqualTo("Test")
    }

    @Test
    fun update_unknownId_throwsException() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val dto = EvaluationRequestDto().apply {
            id = 9999
            date = LocalDate.of(2026, 2, 1)
            content = "Test"
        }

        // When / Then
        assertThatThrownBy { evaluationService.update(dto, employee) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_existingEntity_updatesFields() {
        // Given
        val employee = employeeRepository.save(Employee(firstname = "Max", lastname = "Mustermann"))
        val existing = evaluationRepository.save(Evaluation(
            date = LocalDate.of(2026, 1, 1),
            content = "Old",
            approved = false,
            createdBy = employee,
            updatedBy = employee
        ))
        val dto = EvaluationRequestDto().apply {
            id = existing.id
            date = LocalDate.of(2026, 2, 2)
            content = "New"
            approved = true
        }

        // When
        val result = evaluationService.update(dto, employee)

        // Then
        val saved = evaluationRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().content).isEqualTo("New")
        assertThat(saved.get().approved).isTrue()
    }
}
