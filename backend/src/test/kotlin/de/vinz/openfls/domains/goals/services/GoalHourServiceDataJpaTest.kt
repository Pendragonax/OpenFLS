package de.vinz.openfls.domains.goals.services

import de.vinz.openfls.domains.goals.entities.GoalHour
import de.vinz.openfls.domains.goals.repositories.GoalHourRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(GoalHourService::class)
class GoalHourServiceDataJpaTest {

    @Autowired
    lateinit var goalHourService: GoalHourService

    @Autowired
    lateinit var goalHourRepository: GoalHourRepository

    @Test
    fun create_validEntity_persistsEntry() {
        // Given
        val entity = GoalHour(weeklyHours = 5.0)

        // When
        val result = goalHourService.create(entity)

        // Then
        val saved = goalHourRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().weeklyHours).isEqualTo(5.0)
    }

    @Test
    fun update_missingEntity_throwsException() {
        // Given
        val entity = GoalHour(id = 9999, weeklyHours = 5.0)

        // When / Then
        assertThatThrownBy { goalHourService.update(entity) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun update_existingEntity_updatesValue() {
        // Given
        val existing = goalHourRepository.save(GoalHour(weeklyHours = 2.0))
        val updated = GoalHour(id = existing.id, weeklyHours = 7.0)

        // When
        val result = goalHourService.update(updated)

        // Then
        val saved = goalHourRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().weeklyHours).isEqualTo(7.0)
    }
}
