package de.vinz.openfls.domains.employees.projections

interface EmployeeSoloProjection {
    val id: Long
    val firstname: String
    val lastname: String
    val email: String
    val phonenumber: String
    val description: String
}