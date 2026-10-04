package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.domains.employees.dto.EmployeeDeleteResult
import de.vinz.openfls.domains.services.service.ServiceService
import de.vinz.openfls.logging.StructuredLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Deleting an employee also deletes their documented services, so it is only allowed
 * while no service entries exist for them.
 */
@Service
class EmployeeDeletionService(
    private val employeeService: EmployeeService,
    private val serviceService: ServiceService
) {

    @Transactional
    fun delete(id: Long): EmployeeDeleteResult {
        val employee = employeeService.getEmployeeDetailById(id, includeArchived = true)
            ?: return EmployeeDeleteResult.NotFound
        if (serviceService.countServicesByEmployeeId(id) > 0)
            return EmployeeDeleteResult.HasServices

        employeeService.deleteById(id)
        StructuredLog.audit("employee.deleted", "success", "employee", id.toString())

        return EmployeeDeleteResult.Success(employee)
    }
}
