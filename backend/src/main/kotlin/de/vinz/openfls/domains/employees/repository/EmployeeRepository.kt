package de.vinz.openfls.domains.employees.repository

import de.vinz.openfls.domains.employees.entity.Employee
import org.springframework.data.repository.CrudRepository

interface EmployeeRepository : CrudRepository<Employee, Long>
