package de.vinz.openfls.domains.assistancePlans.repositories

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.employees.EmployeeRepository
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.goals.entities.Goal
import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.hourTypes.HourTypeRepository
import de.vinz.openfls.domains.institutions.Institution
import de.vinz.openfls.domains.institutions.InstitutionRepository
import de.vinz.openfls.domains.services.Service
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.domains.sponsors.Sponsor
import de.vinz.openfls.domains.sponsors.SponsorRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate
import java.time.LocalDateTime

@DataJpaTest
class AssistancePlanPreviewRepositoryDataJpaTest {

    @Autowired
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Autowired
    lateinit var serviceRepository: ServiceRepository

    @Autowired
    lateinit var employeeRepository: EmployeeRepository

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @Autowired
    lateinit var sponsorRepository: SponsorRepository

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    @Test
    fun previewAndRawMinuteQueries_returnRawDataWithoutComputedHourLogic() {
        val base = createBaseData()

        val planWithPlanHours = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                client = base.client,
                sponsor = base.sponsor,
                institution = base.institution,
                hours = mutableSetOf(
                    AssistancePlanHour(
                        weeklyMinutes = 120,
                        hourType = base.hourType
                    )
                )
            ).also { plan ->
                plan.hours.forEach { it.assistancePlan = plan }
            }
        )

        val planWithGoalHours = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2025, 1, 1),
                end = LocalDate.of(2025, 12, 31),
                client = base.client,
                sponsor = base.sponsor,
                institution = base.institution
            )
        )
        val goal = Goal(title = "Goal", assistancePlan = planWithGoalHours)
        goal.hours.add(GoalHour(weeklyMinutes = 210, hourType = base.hourType, goal = goal))
        planWithGoalHours.goals.add(goal)
        assistancePlanRepository.save(planWithGoalHours)

        base.employee.assistancePlanFavorites.add(planWithGoalHours)
        employeeRepository.save(base.employee)

        val previewResult = assistancePlanRepository.findPreviewProjectionsByClientId(base.client.id)
        val planHourMinutesResult = assistancePlanRepository
            .findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(planWithPlanHours.id, planWithGoalHours.id))
        val goalHourMinutesResult = assistancePlanRepository
            .findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(planWithPlanHours.id, planWithGoalHours.id))
        val favoriteIdsResult = assistancePlanRepository.findFavoriteAssistancePlanIdsByEmployeeId(base.employee.id!!)

        assertThat(previewResult).hasSize(2)
        assertThat(previewResult[0].id).isEqualTo(planWithPlanHours.id)
        assertThat(previewResult[1].id).isEqualTo(planWithGoalHours.id)
        assertThat(planHourMinutesResult).hasSize(1)
        assertThat(planHourMinutesResult.first().assistancePlanId).isEqualTo(planWithPlanHours.id)
        assertThat(planHourMinutesResult.first().weeklyMinutes).isEqualTo(120)
        assertThat(goalHourMinutesResult).hasSize(1)
        assertThat(goalHourMinutesResult.first().assistancePlanId).isEqualTo(planWithGoalHours.id)
        assertThat(goalHourMinutesResult.first().weeklyMinutes).isEqualTo(210)
        assertThat(favoriteIdsResult).containsExactly(planWithGoalHours.id)
    }

    @Test
    fun findMinutesByAssistancePlanIdsAndStartAndEnd_returnsOnlyMatchingYearWindowRows() {
        val base = createBaseData()
        val now = LocalDate.now()
        val yearStart = LocalDate.of(now.year, 1, 1)
        val yearEnd = LocalDate.of(now.year, 12, 31)

        val assistancePlan = assistancePlanRepository.save(
            AssistancePlan(
                start = yearStart,
                end = yearEnd,
                client = base.client,
                sponsor = base.sponsor,
                institution = base.institution
            )
        )

        serviceRepository.save(
            Service(
                start = LocalDateTime.of(now.year, 2, 1, 9, 0),
                end = LocalDateTime.of(now.year, 2, 1, 10, 0),
                minutes = 120,
                client = base.client,
                employee = base.employee,
                institution = base.institution,
                hourType = base.hourType,
                assistancePlan = assistancePlan
            )
        )
        serviceRepository.save(
            Service(
                start = LocalDateTime.of(now.year - 1, 12, 31, 9, 0),
                end = LocalDateTime.of(now.year - 1, 12, 31, 10, 0),
                minutes = 60,
                client = base.client,
                employee = base.employee,
                institution = base.institution,
                hourType = base.hourType,
                assistancePlan = assistancePlan
            )
        )

        val result = serviceRepository.findMinutesByAssistancePlanIdsAndStartAndEnd(
            listOf(assistancePlan.id),
            yearStart,
            yearEnd
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().assistancePlanId).isEqualTo(assistancePlan.id)
        assertThat(result.first().minutes).isEqualTo(120)
    }

    private fun createBaseData(): BaseData {
        val institution = institutionRepository.save(
            Institution(name = "Institution", email = "institution@test.de", phonenumber = "123")
        )
        val template = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template", description = "desc", withoutClient = false)
        )
        val client = clientRepository.save(
            Client(
                firstName = "Max",
                lastName = "Mustermann",
                categoryTemplate = template,
                institution = institution
            )
        )
        val sponsor = sponsorRepository.save(Sponsor(name = "Sponsor", payOverhang = true, payExact = false))
        val hourType = hourTypeRepository.save(HourType(title = "Standard", price = 10.0))
        val employee = employeeRepository.save(
            Employee(
                firstname = "Erika",
                lastname = "Musterfrau",
                email = "employee@test.de"
            )
        )

        return BaseData(
            institution = institution,
            client = client,
            sponsor = sponsor,
            hourType = hourType,
            employee = employee
        )
    }

    private data class BaseData(
        val institution: Institution,
        val client: Client,
        val sponsor: Sponsor,
        val hourType: HourType,
        val employee: Employee
    )
}
