package de.vinz.openfls.domains.hourCorridors

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.hourCorridors.dtos.CreateHourCorridorDto
import de.vinz.openfls.domains.hourCorridors.dtos.UpdateHourCorridorDto
import de.vinz.openfls.domains.hourTypes.HourType
import de.vinz.openfls.domains.hourTypes.HourTypeRepository
import de.vinz.openfls.domains.hourTypes.HourTypeService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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

    @MockitoBean
    lateinit var hourTypeService: HourTypeService

    @Test
    fun create_validDto_persistsEntity() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val dto = CreateHourCorridorDto(
            title = "5 bis 10",
            weeklyMinutesFrom = 300,
            weeklyMinutesTill = 600,
            hourTypeId = hourType.id
        )
        whenever(hourTypeService.getById(hourType.id)).thenReturn(hourType)

        // When
        val result = hourCorridorService.create(dto)

        // Then
        val saved = hourCorridorRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().title).isEqualTo("5 bis 10")
        assertThat(saved.get().weeklyMinutesFrom).isEqualTo(300)
        assertThat(saved.get().weeklyMinutesTill).isEqualTo(600)
        assertThat(saved.get().hourType?.id).isEqualTo(hourType.id)
        val history = hourCorridorService.getAuditHistory(result.id)
        assertThat(history).hasSize(1)
        assertThat(history[0].action).isEqualTo(HourCorridorAuditAction.CREATE)
    }

    @Test
    fun create_invalidRange_throwsException() {
        // Given
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val dto = CreateHourCorridorDto(
            title = "Ungültig",
            weeklyMinutesFrom = 600,
            weeklyMinutesTill = 300,
            hourTypeId = hourType.id
        )

        // When / Then
        assertThatThrownBy { hourCorridorService.create(dto) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("till before from")
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
        val dto = UpdateHourCorridorDto(
            id = existing.id,
            title = "Neu",
            weeklyMinutesFrom = 360,
            weeklyMinutesTill = 720,
            hourTypeId = secondHourType.id
        )
        whenever(hourTypeService.getById(secondHourType.id)).thenReturn(secondHourType)

        // When
        val result = hourCorridorService.update(dto)

        // Then
        val saved = hourCorridorRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().title).isEqualTo("Neu")
        assertThat(saved.get().weeklyMinutesFrom).isEqualTo(360)
        assertThat(saved.get().weeklyMinutesTill).isEqualTo(720)
        assertThat(saved.get().hourType?.id).isEqualTo(secondHourType.id)
        assertThat(hourCorridorService.getAuditHistory(result.id)).extracting<String> { it.action.name }
            .containsExactly("UPDATE")
        val updateAudit = hourCorridorService.getAuditHistory(result.id)[0]
        assertThat(updateAudit.beforeTitle).isEqualTo("Alt")
        assertThat(updateAudit.afterTitle).isEqualTo("Neu")
        assertThat(updateAudit.beforeWeeklyMinutesFrom).isEqualTo(240)
        assertThat(updateAudit.afterWeeklyMinutesFrom).isEqualTo(360)
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
        val count = hourCorridorService.countByAssistancePlan(corridor.id)

        // Then
        assertThat(count).isEqualTo(1)
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
    fun delete_referencedCorridor_throwsException() {
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

        // When / Then
        assertThatThrownBy { hourCorridorService.delete(corridor.id) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("hour corridor is used by 1 assistance plans")
    }

    @Test
    fun delete_unusedCorridor_keepsAuditHistory() {
        val hourType = hourTypeRepository.save(HourType(title = "Fachleistungsstunde", price = 12.5))
        val corridor = hourCorridorRepository.save(
            HourCorridor(title = "5 bis 10", weeklyMinutesFrom = 300, weeklyMinutesTill = 600, hourType = hourType)
        )

        hourCorridorService.delete(corridor.id)

        assertThat(hourCorridorRepository.findById(corridor.id)).isEmpty
        val history = hourCorridorService.getAuditHistory(corridor.id)
        assertThat(history).hasSize(1)
        assertThat(history[0].action).isEqualTo(HourCorridorAuditAction.DELETE)
        assertThat(history[0].beforeTitle).isEqualTo("5 bis 10")
        assertThat(history[0].afterTitle).isNull()
    }
}
