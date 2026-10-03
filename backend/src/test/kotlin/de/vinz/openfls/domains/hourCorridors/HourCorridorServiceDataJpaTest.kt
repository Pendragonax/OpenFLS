package de.vinz.openfls.domains.hourCorridors

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.repository.AssistancePlanRepository
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorCreateRequest
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorCreateResult
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorDeleteResult
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorUpdateRequest
import de.vinz.openfls.domains.hourCorridors.dto.HourCorridorUpdateResult
import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import de.vinz.openfls.domains.clients.Client
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.repository.InstitutionRepository
import de.vinz.openfls.domains.hourTypes.repository.HourTypeRepository
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourCorridors.service.HourCorridorService
import de.vinz.openfls.domains.hourCorridors.repository.HourCorridorRepository
import de.vinz.openfls.domains.hourCorridors.repository.HourCorridorAuditLogRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import de.vinz.openfls.TimeConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDate

@DataJpaTest
@Import(HourCorridorService::class, TimeConfiguration::class)
class HourCorridorServiceDataJpaTest {

    @Autowired
    lateinit var hourCorridorService: HourCorridorService

    @Autowired
    lateinit var hourCorridorRepository: HourCorridorRepository

    @Autowired
    lateinit var auditLogRepository: HourCorridorAuditLogRepository

    @Autowired
    lateinit var hourTypeRepository: HourTypeRepository

    @Autowired
    lateinit var assistancePlanRepository: AssistancePlanRepository

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @Autowired
    lateinit var institutionRepository: InstitutionRepository

    @MockitoBean
    lateinit var hourTypeService: HourTypeService

    @Test
    fun create_validDto_persistsEntity() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val dto = HourCorridorCreateRequest(
            title = "5 bis 10",
            weeklyMinutesFrom = 300,
            weeklyMinutesTill = 600,
            hourTypeId = hourType.id
        )
        whenever(hourTypeService.getEntityById(hourType.id)).thenReturn(hourType)

        // When
        val result = hourCorridorService.create(dto) as HourCorridorCreateResult.Success

        // Then
        val saved = hourCorridorRepository.findById(result.response.id)
        assertThat(saved).isPresent
        assertThat(saved.get().title).isEqualTo("5 bis 10")
        assertThat(saved.get().weeklyMinutesFrom).isEqualTo(300)
        assertThat(saved.get().weeklyMinutesTill).isEqualTo(600)
        assertThat(saved.get().hourType?.id).isEqualTo(hourType.id)
        val history = hourCorridorService.getAuditHistoryByHourCorridorId(result.response.id)
        assertThat(history).hasSize(1)
        assertThat(history[0].action).isEqualTo(HourCorridorAuditAction.CREATE)
    }

    @Test
    fun create_invalidRange_returnsInvalidRange() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val dto = HourCorridorCreateRequest(
            title = "Ungültig",
            weeklyMinutesFrom = 600,
            weeklyMinutesTill = 300,
            hourTypeId = hourType.id
        )

        // When
        val result = hourCorridorService.create(dto)

        // Then
        assertThat(result).isEqualTo(HourCorridorCreateResult.InvalidRange("till before from"))
    }

    @Test
    fun create_unknownHourType_returnsHourTypeNotFound() {
        // Given
        val dto = HourCorridorCreateRequest(
            title = "5 bis 10",
            weeklyMinutesFrom = 300,
            weeklyMinutesTill = 600,
            hourTypeId = 42
        )
        whenever(hourTypeService.getEntityById(42)).thenReturn(null)

        // When
        val result = hourCorridorService.create(dto)

        // Then
        assertThat(result).isEqualTo(HourCorridorCreateResult.HourTypeNotFound("hour type with id 42 not found"))
    }

    @Test
    fun update_existingDto_updatesEntity() {
        // Given
        val firstHourType = hourTypeRepository.save(HourType(title = "Alt", price = 10.0))
        val secondHourType = hourTypeRepository.save(HourType(title = "Neu", price = 11.0))
        val existing = hourCorridorRepository.save(
            HourCorridor(
                title = "Alt",
                weeklyMinutesFrom = 240,
                weeklyMinutesTill = 480,
                hourType = firstHourType
            )
        )
        val dto = HourCorridorUpdateRequest(
            id = existing.id,
            title = "Neu",
            weeklyMinutesFrom = 360,
            weeklyMinutesTill = 720,
            hourTypeId = secondHourType.id
        )
        whenever(hourTypeService.getEntityById(secondHourType.id)).thenReturn(secondHourType)

        // When
        val result = hourCorridorService.update(dto) as HourCorridorUpdateResult.Success

        // Then
        val saved = hourCorridorRepository.findById(result.response.id)
        assertThat(saved).isPresent
        assertThat(saved.get().title).isEqualTo("Neu")
        assertThat(saved.get().weeklyMinutesFrom).isEqualTo(360)
        assertThat(saved.get().weeklyMinutesTill).isEqualTo(720)
        assertThat(saved.get().hourType?.id).isEqualTo(secondHourType.id)
        assertThat(hourCorridorService.getAuditHistoryByHourCorridorId(result.response.id)).extracting<String> { it.action.name }
            .containsExactly("UPDATE")
        val updateAudit = hourCorridorService.getAuditHistoryByHourCorridorId(result.response.id)[0]
        assertThat(updateAudit.beforeTitle).isEqualTo("Alt")
        assertThat(updateAudit.afterTitle).isEqualTo("Neu")
        assertThat(updateAudit.beforeWeeklyMinutesFrom).isEqualTo(240)
        assertThat(updateAudit.afterWeeklyMinutesFrom).isEqualTo(360)
    }

    @Test
    fun update_unknownCorridor_returnsNotFound() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val dto = HourCorridorUpdateRequest(
            id = 999, title = "Neu", weeklyMinutesFrom = 360, weeklyMinutesTill = 720, hourTypeId = hourType.id
        )

        // When
        val result = hourCorridorService.update(dto)

        // Then
        assertThat(result).isEqualTo(HourCorridorUpdateResult.NotFound)
    }

    @Test
    fun update_invalidRange_returnsInvalidRange() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val existing = hourCorridorRepository.save(
            HourCorridor(title = "Alt", weeklyMinutesFrom = 240, weeklyMinutesTill = 480, hourType = hourType)
        )
        val dto = HourCorridorUpdateRequest(
            id = existing.id, title = "Alt", weeklyMinutesFrom = 480, weeklyMinutesTill = 240, hourTypeId = hourType.id
        )

        // When
        val result = hourCorridorService.update(dto)

        // Then
        assertThat(result).isEqualTo(HourCorridorUpdateResult.InvalidRange("till before from"))
    }

    @Test
    fun update_unknownHourType_returnsHourTypeNotFound() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val existing = hourCorridorRepository.save(
            HourCorridor(title = "Alt", weeklyMinutesFrom = 240, weeklyMinutesTill = 480, hourType = hourType)
        )
        val dto = HourCorridorUpdateRequest(
            id = existing.id, title = "Alt", weeklyMinutesFrom = 240, weeklyMinutesTill = 480, hourTypeId = 42
        )
        whenever(hourTypeService.getEntityById(42)).thenReturn(null)

        // When
        val result = hourCorridorService.update(dto)

        // Then
        assertThat(result).isEqualTo(HourCorridorUpdateResult.HourTypeNotFound("hour type with id 42 not found"))
    }

    @Test
    fun countByAssistancePlan_whenPlansReferenceCorridor_returnsCount() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val corridor = hourCorridorRepository.save(
            HourCorridor(
                title = "5 bis 10",
                weeklyMinutesFrom = 300,
                weeklyMinutesTill = 600,
                hourType = hourType
            )
        )
        assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                hourCorridor = corridor
            )
        )

        // When
        val count = hourCorridorService.countAssistancePlansByHourCorridorId(corridor.id)

        // Then
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun getAssistancePlans_whenPlansReferenceCorridor_returnsPlansWithClientNamesNewestFirst() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val corridor = hourCorridorRepository.save(
            HourCorridor(title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourType = hourType)
        )
        val otherCorridor = hourCorridorRepository.save(
            HourCorridor(title = "10 bis 15", weeklyMinutesFrom = 600, weeklyMinutesTill = 900, hourType = hourType)
        )
        val institution = institutionRepository.save(Institution(name = "Inst", email = "a@b.c", phonenumber = "1"))
        val categoryTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template", description = "", withoutClient = false))
        val client = clientRepository.save(
            Client(firstName = "Max", lastName = "Mustermann", categoryTemplate = categoryTemplate, institution = institution)
        )
        val older = assistancePlanRepository.save(
            AssistancePlan(start = LocalDate.of(2025, 1, 1), end = LocalDate.of(2025, 12, 31), client = client, hourCorridor = corridor)
        )
        val newer = assistancePlanRepository.save(
            AssistancePlan(start = LocalDate.of(2026, 1, 1), end = LocalDate.of(2026, 12, 31), client = client, hourCorridor = corridor)
        )
        assistancePlanRepository.save(
            AssistancePlan(start = LocalDate.of(2026, 1, 1), end = LocalDate.of(2026, 12, 31), client = client, hourCorridor = otherCorridor)
        )

        // When
        val result = hourCorridorService.getAssistancePlansByHourCorridorId(corridor.id)

        // Then
        assertThat(result).extracting<Long> { it.id }.containsExactly(newer.id, older.id)
        assertThat(result[0].clientFirstName).isEqualTo("Max")
        assertThat(result[0].clientLastName).isEqualTo("Mustermann")
    }

    @Test
    fun getAll_multipleCorridors_returnsGroupedAssistancePlanCounts() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val firstCorridor = hourCorridorRepository.save(
            HourCorridor(title = "A", weeklyMinutesFrom = 60, weeklyMinutesTill = 120, hourType = hourType)
        )
        val secondCorridor = hourCorridorRepository.save(
            HourCorridor(title = "B", weeklyMinutesFrom = 120, weeklyMinutesTill = 180, hourType = hourType)
        )
        assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                hourCorridor = firstCorridor
            )
        )
        assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                hourCorridor = firstCorridor
            )
        )

        // When
        val result = hourCorridorService.getAll()

        // Then
        assertThat(result).extracting<Long> { it.id }
            .containsExactly(firstCorridor.id, secondCorridor.id)
        assertThat(result.first().assistancePlanCount).isEqualTo(2)
        assertThat(result.last().assistancePlanCount).isEqualTo(0)
    }

    @Test
    fun delete_referencedCorridor_returnsConflict() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val corridor = hourCorridorRepository.save(
            HourCorridor(
                title = "5 bis 10",
                weeklyMinutesFrom = 300,
                weeklyMinutesTill = 600,
                hourType = hourType
            )
        )
        assistancePlanRepository.save(
            AssistancePlan(
                start = LocalDate.of(2026, 1, 1),
                end = LocalDate.of(2026, 12, 31),
                hourCorridor = corridor
            )
        )

        // When
        val result = hourCorridorService.delete(corridor.id)

        // Then
        assertThat(result).isEqualTo(HourCorridorDeleteResult.Conflict(1))
        assertThat(hourCorridorRepository.findById(corridor.id)).isPresent
    }

    @Test
    fun delete_unknownCorridor_returnsNotFound() {
        // When
        val result = hourCorridorService.delete(999)

        // Then
        assertThat(result).isEqualTo(HourCorridorDeleteResult.NotFound)
    }

    @Test
    fun delete_unusedCorridor_keepsAuditHistory() {
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val corridor = hourCorridorRepository.save(
            HourCorridor(title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourType = hourType)
        )

        hourCorridorService.delete(corridor.id)

        assertThat(hourCorridorRepository.findById(corridor.id)).isEmpty
        val history = hourCorridorService.getAuditHistoryByHourCorridorId(corridor.id)
        assertThat(history).hasSize(1)
        assertThat(history[0].action).isEqualTo(HourCorridorAuditAction.DELETE)
        assertThat(history[0].beforeTitle).isEqualTo("5 bis 10")
        assertThat(history[0].afterTitle).isNull()
    }
}
