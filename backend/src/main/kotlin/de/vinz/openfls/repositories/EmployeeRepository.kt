package de.vinz.openfls.repositories

import de.vinz.openfls.entities.Employee
import org.springframework.data.repository.CrudRepository

interface EmployeeRepository : CrudRepository<Employee, Long>