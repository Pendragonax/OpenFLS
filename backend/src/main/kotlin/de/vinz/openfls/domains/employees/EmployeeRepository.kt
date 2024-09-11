package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.employees.projections.EmployeeSoloProjection
import org.springframework.data.repository.CrudRepository

interface EmployeeRepository : CrudRepository<Employee, Long> {
    fun findAllProjectionsBy(): List<EmployeeSoloProjection>
}