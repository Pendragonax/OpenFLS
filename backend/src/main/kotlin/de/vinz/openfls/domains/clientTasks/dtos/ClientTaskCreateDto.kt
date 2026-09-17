package de.vinz.openfls.domains.clientTasks.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class ClientTaskCreateDto(
    @field:NotNull
    val clientId: Long = 0,

    @field:NotBlank
    @field:Size(max = 128)
    val title: String = "",

    @field:Size(max = 1024)
    val description: String = "",

    @field:NotNull
    val dueDate: LocalDate = LocalDate.now()
)
