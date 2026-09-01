package de.vinz.openfls.domains.clientTasks.dtos

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class CompleteClientTaskDto(
    @field:Size(max = 1024)
    val comment: String = "",

    @field:NotNull
    val completedOn: LocalDate = LocalDate.now()
)
