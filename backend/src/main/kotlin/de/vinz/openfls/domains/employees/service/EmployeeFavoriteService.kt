package de.vinz.openfls.domains.employees.service

import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.employees.dto.EmployeeFavoriteResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Favourite clients and assistance plans of an employee. Lives apart from [EmployeeService],
 * because it needs the client and assistance plan services, which in turn rely on the employee service.
 */
@Service
class EmployeeFavoriteService(
    private val employeeService: EmployeeService,
    private val assistancePlanService: AssistancePlanService,
    private val clientService: ClientService
) {

    @Transactional
    fun addAssistancePlanFavorite(employeeId: Long, assistancePlanId: Long): EmployeeFavoriteResult {
        val employee = employeeService.getEntityById(employeeId) ?: return EmployeeFavoriteResult.EmployeeNotFound
        val assistancePlan = assistancePlanService.getEntityById(assistancePlanId)
            ?: return EmployeeFavoriteResult.AssistancePlanNotFound

        if (employee.assistancePlanFavorites.none { it.id == assistancePlanId }) {
            employee.assistancePlanFavorites.add(assistancePlan)
            employeeService.saveEntity(employee)
        }

        return EmployeeFavoriteResult.Success
    }

    @Transactional
    fun deleteAssistancePlanFavorite(employeeId: Long, assistancePlanId: Long): EmployeeFavoriteResult {
        val employee = employeeService.getEntityById(employeeId) ?: return EmployeeFavoriteResult.EmployeeNotFound

        if (employee.assistancePlanFavorites.removeIf { it.id == assistancePlanId }) {
            employeeService.saveEntity(employee)
        }

        return EmployeeFavoriteResult.Success
    }

    @Transactional
    fun addClientFavorite(employeeId: Long, clientId: Long): EmployeeFavoriteResult {
        val employee = employeeService.getEntityById(employeeId) ?: return EmployeeFavoriteResult.EmployeeNotFound
        val client = clientService.getEntityById(clientId) ?: return EmployeeFavoriteResult.ClientNotFound

        if (employee.clientFavorites.none { it.id == clientId }) {
            employee.clientFavorites.add(client)
            employeeService.saveEntity(employee)
        }

        return EmployeeFavoriteResult.Success
    }

    @Transactional
    fun deleteClientFavorite(employeeId: Long, clientId: Long): EmployeeFavoriteResult {
        val employee = employeeService.getEntityById(employeeId) ?: return EmployeeFavoriteResult.EmployeeNotFound

        if (employee.clientFavorites.removeIf { it.id == clientId }) {
            employeeService.saveEntity(employee)
        }

        return EmployeeFavoriteResult.Success
    }

    /**
     * Removes a client from the favourites of every employee. Used when the client is
     * archived or deleted, so nobody keeps a dead entry on their home view.
     */
    @Transactional
    fun deleteClientFavoritesByClientId(clientId: Long): Int {
        var removedFavorites = 0
        employeeService.getAllEntities().forEach { employee ->
            if (employee.clientFavorites.removeIf { it.id == clientId }) {
                removedFavorites++
                employeeService.saveEntity(employee)
            }
        }

        return removedFavorites
    }

    @Transactional
    fun deleteAssistancePlanFavoritesByClientId(clientId: Long): Int {
        var removedFavorites = 0
        employeeService.getAllEntities().forEach { employee ->
            val before = employee.assistancePlanFavorites.size
            employee.assistancePlanFavorites.removeIf { it.client?.id == clientId }
            val removedForEmployee = before - employee.assistancePlanFavorites.size
            if (removedForEmployee > 0) {
                removedFavorites += removedForEmployee
                employeeService.saveEntity(employee)
            }
        }

        return removedFavorites
    }
}
