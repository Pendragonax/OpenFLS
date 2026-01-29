package de.vinz.openfls.domains.absence.dtos

data class YearAbsenceDTO(
    val year: Int,
    val employeeAbsences: List<EmployeeAbsenceResponseDTO>
) {
    companion object {
        fun of(year: Int, employeeAbsences: List<EmployeeAbsenceResponseDTO>): YearAbsenceDTO {
            return YearAbsenceDTO(
                year = year,
                employeeAbsences = employeeAbsences
            )
        }
    }
}