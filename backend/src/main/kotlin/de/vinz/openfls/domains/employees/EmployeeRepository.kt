package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.entities.Employee
import org.springframework.data.repository.CrudRepository

interface EmployeeRepository : CrudRepository<Employee, Long>