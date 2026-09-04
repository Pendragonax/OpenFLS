package de.vinz.openfls.domains.absence.dtos

data class YearAbsenceDto(
    val year: Int,
    val employeeAbsences: List<EmployeeAbsenceResponseDto>
) {
    companion object {
        fun of(year: Int, employeeAbsences: List<EmployeeAbsenceResponseDto>): YearAbsenceDto {
            return YearAbsenceDto(
                year = year,
                employeeAbsences = employeeAbsences
            )
        }
    }
}