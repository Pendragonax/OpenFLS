package de.vinz.openfls.domains.employees.archive

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

class EmployeeArchiveActionRequest {
    @field:NotNull
    var actionDate: LocalDate? = null

    @field:NotBlank
    var reason: String = ""

    @field:NotBlank
    var remark: String = ""
}
