package de.vinz.openfls.domains.services.dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.categories.dtos.CategoryDto
import de.vinz.openfls.domains.goals.dtos.GoalDto
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime
import jakarta.validation.constraints.Size

class ServiceXLDto {
    var id: Long = 0

    var start: LocalDateTime = LocalDateTime.now()

    var end: LocalDateTime = LocalDateTime.now()

    @Size(max = 64)
    var title: String = ""

    @Size(max = 1024)
    var content: String = ""

    var unfinished: Boolean = false

    var groupService: Boolean = false

    var minutes: Int = 0

    @field:NotNull
    var employeeId: Long = 0

    @field:NotNull
    var clientId: Long = 0

    @field:NotNull
    var institutionId: Long = 0

    @field:NotNull
    var assistancePlanId: Long = 0

    @field:NotNull
    var hourTypeId: Long = 0

    @JsonIgnoreProperties(value = ["services", "hours", "hibernateLazyInitializer"])
    var goals: MutableSet<GoalDto> = mutableSetOf()

    var categorys: MutableSet<CategoryDto> = mutableSetOf()
}