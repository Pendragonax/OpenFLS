package de.vinz.openfls.domains.assistancePlans.repository

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHour
import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.clients.repository.ClientRepository
import de.vinz.openfls.domains.employees.repository.EmployeeRepository
import de.vinz.openfls.domains.employees.entity.Employee
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.entity.GoalHour
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourTypes.repository.HourTypeRepository
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.services.entity.Service
import de.vinz.openfls.domains.services.repository.ServiceRepository
import de.vinz.openfls.domains.sponsors.entity.Sponsor
import de.vinz.openfls.domains.sponsors.repository.SponsorRepository
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
    lateinit var assistancePlanPreviewRepository: AssistancePlanPreviewRepository

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

        val previewResult = assistancePlanPreviewRepository.findPreviewProjectionsByClientId(base.client.id)
        val planHourMinutesResult = assistancePlanPreviewRepository
            .findWeeklyMinutesFromAssistancePlanHoursByAssistancePlanIds(listOf(planWithPlanHours.id, planWithGoalHours.id))
        val goalHourMinutesResult = assistancePlanPreviewRepository
            .findWeeklyMinutesFromGoalHoursByAssistancePlanIds(listOf(planWithPlanHours.id, planWithGoalHours.id))
        val favoriteIdsResult = assistancePlanPreviewRepository.findFavoriteAssistancePlanIdsByEmployeeId(base.employee.id!!)
        val existingResult = assistancePlanPreviewRepository.findExistingProjectionsByClientId(base.client.id)

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
        assertThat(existingResult).hasSize(2)
        assertThat(existingResult.first().sponsorName).isEqualTo("Sponsor")
    }

    @Test
    fun findMinutesInPlanPeriodByAssistancePlanIdsAndStartAndEnd_returnsOnlyMatchingYearWindowRows() {
        val base = createBaseData()
        val now = LocalDate.of(2026, 3, 10)
        val yearStart = LocalDate.of(now.year, 1, 1)
        val yearEnd = LocalDate.of(now.year, 12, 31)

        val assistancePlan = assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(now.year, 3, 1),
                end = LocalDate.of(now.year, 9, 30),
                client = base.client,
                sponsor = base.sponsor,
                institution = base.institution
            )
        )

        serviceRepository.save(
            Service(
                start = LocalDateTime.of(now.year, 5, 1, 9, 0),
                end = LocalDateTime.of(now.year, 5, 1, 10, 0),
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
                start = LocalDateTime.of(now.year, 1, 31, 9, 0),
                end = LocalDateTime.of(now.year, 1, 31, 10, 0),
                minutes = 60,
                client = base.client,
                employee = base.employee,
                institution = base.institution,
                hourType = base.hourType,
                assistancePlan = assistancePlan
            )
        )
        serviceRepository.save(
            Service(
                start = LocalDateTime.of(now.year, 10, 1, 9, 0),
                end = LocalDateTime.of(now.year, 10, 1, 10, 0),
                minutes = 45,
                client = base.client,
                employee = base.employee,
                institution = base.institution,
                hourType = base.hourType,
                assistancePlan = assistancePlan
            )
        )

        val result = serviceRepository.findMinutesInPlanPeriodByAssistancePlanIdsAndStartAndEnd(
            listOf(assistancePlan.id),
            yearStart,
            yearEnd
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().assistancePlanId).isEqualTo(assistancePlan.id)
        assertThat(result.first().minutes).isEqualTo(120)
    }

    @Test
    fun findMinutesInPlanPeriodByAssistancePlanIdsUntil_returnsRowsFromEachPlanStart() {
        val base = createBaseData()
        val now = LocalDate.of(2026, 3, 10)
        val assistancePlan = assistancePlanRepository.save(
            AssistancePlan(
                start = now.minusDays(10),
                end = now.plusDays(10),
                client = base.client,
                sponsor = base.sponsor,
                institution = base.institution
            )
        )

        serviceRepository.save(
            Service(
                start = now.minusDays(5).atStartOfDay(),
                end = now.minusDays(5).atStartOfDay().plusHours(1),
                minutes = 60,
                client = base.client,
                employee = base.employee,
                institution = base.institution,
                hourType = base.hourType,
                assistancePlan = assistancePlan
            )
        )
        serviceRepository.save(
            Service(
                start = now.minusDays(11).atStartOfDay(),
                end = now.minusDays(11).atStartOfDay().plusHours(1),
                minutes = 120,
                client = base.client,
                employee = base.employee,
                institution = base.institution,
                hourType = base.hourType,
                assistancePlan = assistancePlan
            )
        )

        val result = serviceRepository.findMinutesInPlanPeriodByAssistancePlanIdsUntil(
            listOf(assistancePlan.id),
            now
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().assistancePlanId).isEqualTo(assistancePlan.id)
        assertThat(result.first().minutes).isEqualTo(60)
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
