package de.vinz.openfls.domains.employees.dto

import de.vinz.openfls.domains.employees.entity.Unprofessional
import java.time.LocalDate

data class UnprofessionalResponse(
    val employeeId: Long = 0,
    val sponsorId: Long = 0,
    val end: LocalDate? = null
) {
    companion object {
        fun from(unprofessional: Unprofessional): UnprofessionalResponse {
            return UnprofessionalResponse(
                employeeId = unprofessional.id?.employeeId ?: unprofessional.employee?.id ?: 0,
                sponsorId = unprofessional.id?.sponsorId ?: unprofessional.sponsor?.id ?: 0,
                end = unprofessional.end
            )
        }
    }
}
