package de.vinz.openfls.domains.employees.dtos

import de.vinz.openfls.domains.employees.entities.Unprofessional
import java.time.LocalDate

data class UnprofessionalDto(
    var employeeId: Long = 0,
    var sponsorId: Long = 0,
    var end: LocalDate? = null) {

    companion object {
        fun from(unprofessional: Unprofessional): UnprofessionalDto {
            return UnprofessionalDto(
                    employeeId = unprofessional.employee?.id ?: 0,
                    sponsorId = unprofessional.sponsor?.id ?: 0,
                    end = unprofessional.end)
        }

        fun from(unprofessionalList: List<Unprofessional>): List<UnprofessionalDto> {
            return unprofessionalList.map { from(it) }
        }

        fun from(unprofessionalArray: Array<Unprofessional>): Array<UnprofessionalDto> {
            return  unprofessionalArray.map { from(it) }.toTypedArray()
        }

        fun from(unprofessionalMutableSet: MutableSet<Unprofessional>): MutableSet<UnprofessionalDto> {
            return unprofessionalMutableSet.map { from(it) }.toMutableSet()
        }
    }
}