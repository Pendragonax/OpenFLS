package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.domains.contingents.projections.ContingentProjection
import de.vinz.openfls.projections.EmployeeSoloProjection
import de.vinz.openfls.projections.InstitutionSoloProjection
import de.vinz.openfls.services.ServiceService
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class ContingentEvaluationServiceTest {
    @MockK
    lateinit var contingentService: ContingentService
    @MockK
    lateinit var serviceService: ServiceService

    @Before
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun testMock() {
        val employee = createRandomEmployee()
        val institution = createRandomInstitution()
        val contingent = createContingent(
                LocalDate.of(2023, 1, 11),
                LocalDate.of(2023, 1, 17),
                1.0,
                employee,
                institution)
        val contingentEvaluationService = ContingentEvaluationService(contingentService, serviceService)

        val result = contingentEvaluationService.getEmployeeContingentEvaluations(2023, listOf(contingent), listOf())

        assertThat(result.first().contingentHours.sum()).isEqualTo(1.0)
    }

    @Test
    fun testMock2() {
        val employee = createRandomEmployee()
        val institution = createRandomInstitution()
        val contingent = createContingent(
                LocalDate.of(2022, 1, 31),
                LocalDate.of(2023, 1, 7),
                1.0,
                employee,
                institution)
        val contingentEvaluationService = ContingentEvaluationService(contingentService, serviceService)

        val result = contingentEvaluationService.getEmployeeContingentEvaluations(2023, listOf(contingent), listOf())

        assertThat(result.first().contingentHours.sum()).isEqualTo(1.0)
    }

    private fun createRandomContingents(employees: List<EmployeeSoloProjection>, institutions: List<InstitutionSoloProjection>, size: Int): List<ContingentProjection> {
        return List(size) { createRandomContingent(employees, institutions) }
    }

    private fun createRandomContingents(size: Int): List<ContingentProjection> {
        val employees = createRandomEmployees(3)
        val institutions = createRandomInstitutions(3)

        return List(size) { createRandomContingent(employees, institutions) }
    }

    private fun createRandomContingent(employees: List<EmployeeSoloProjection>, institutions: List<InstitutionSoloProjection>): ContingentProjection {
        val start = LocalDate.now().minusDays(Random.nextLong(0, 365))
        val end = start.plusDays(Random.nextLong(0, 365))

        return object : ContingentProjection {
            override val id = Random.nextLong(1, 10000)
            override val start = start
            override val end = if (Random.nextBoolean()) end else null
            override val weeklyServiceHours = Random.nextDouble(1.0, 40.0)
            override val employee = employees[Random.nextInt(employees.size)]
            override val institution = institutions[Random.nextInt(employees.size)]
        }
    }

    private fun createContingent(start: LocalDate, end: LocalDate?, weeklyServiceHours: Double, employee: EmployeeSoloProjection, institution: InstitutionSoloProjection): ContingentProjection {
        return object : ContingentProjection {
            override val id = Random.nextLong(1, 10000)
            override val start = start
            override val end = end
            override val weeklyServiceHours = weeklyServiceHours
            override val employee = employee
            override val institution = institution
        }
    }

    private fun createRandomEmployees(size: Int): List<EmployeeSoloProjection> {
        return List(size) { createRandomEmployee() }
    }

    private fun createRandomEmployee(): EmployeeSoloProjection {
        return object : EmployeeSoloProjection {
            override val id = Random.nextLong(1, 10000)
            override val firstname = "Firstname${Random.nextInt(1, 100)}"
            override val lastname = "Lastname${Random.nextInt(1, 100)}"
            override val email = "email${Random.nextInt(1, 100)}@example.com"
            override val phonenumber = "123456${Random.nextInt(1000, 9999)}"
            override val description = "Description${Random.nextInt(1, 100)}"
        }
    }

    private fun createRandomInstitutions(size: Int): List<InstitutionSoloProjection> {
        return List(size) { createRandomInstitution() }
    }

    private fun createRandomInstitution(): InstitutionSoloProjection {
        return object : InstitutionSoloProjection {
            override val id = Random.nextLong(1, 10000)
            override val name = "Name${Random.nextInt(1, 100)}"
            override val email = "email${Random.nextInt(1, 100)}@example.com"
            override val phonenumber = "123456${Random.nextInt(1000, 9999)}"
        }
    }

}