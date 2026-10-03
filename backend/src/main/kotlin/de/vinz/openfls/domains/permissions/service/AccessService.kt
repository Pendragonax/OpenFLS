package de.vinz.openfls.domains.permissions.service

import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.services.UserService
import org.springframework.stereotype.Service

@Service
class AccessService(
        private val userService: UserService,
        private val assistancePlanService: AssistancePlanService,
        private val permissionService: PermissionService,
        private val institutionService: InstitutionService,
        private val clientService: ClientService
) {

    fun isAdmin(): Boolean {
        return userService.isAdmin()
    }

    fun getId(): Long {
        return userService.getUserId()
    }

    fun isAffiliated(institutionId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            isAffiliated(getId(), institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun isLeader(institutionId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            isLeader(getId(), institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun getLeadingInstitutionIds(): List<Long> {
        return permissionService.getLeadingInstitutionIdsByEmployee(getId())
    }

    fun canWriteEntries(institutionId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            canWriteEntries(getId(), institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canReadEntries(institutionId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin() || institutionId <= 0)
                return true

            canReadEntries(getId(), institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyClient(clientId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            val clientInstitutionId = clientService.getEntityById(clientId)?.institution?.id ?: 0

            isAffiliated(getId(), clientInstitutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyAssistancePlan(assistancePlanId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            val institutionId = assistancePlanService.getEntityById(assistancePlanId)?.institution?.id ?: 0

            isAffiliated(getId(), institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyEmployee(employeeId: Long): Boolean {
        return try {
            isAdmin()
        } catch (ex: Exception) {
            false
        }
    }

    fun canReadEmployee(employeeId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            val leadingInstitutions = permissionService.getLeadingInstitutionIdsByEmployee(getId())
            val affiliatedInstitutions = permissionService.getAffiliatedInstitutionIdsByEmployee(employeeId)

            leadingInstitutions.any { affiliatedInstitutions.contains(it) }
        } catch (ex: Exception) {
            false
        }
    }

    private fun isAffiliated(userId: Long, institutionId: Long): Boolean {
        return try {
            permissionService.getAffiliatedInstitutionIdsByEmployee(userId).contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun isLeader(userId: Long, institutionId: Long): Boolean {
        return try {
            permissionService.getLeadingInstitutionIdsByEmployee(userId).contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    private fun canWriteEntries(userId: Long, institutionId: Long): Boolean {
        return try {
            permissionService.getWritableInstitutionIdsByEmployee(userId).contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    private fun canReadEntries(userId: Long, institutionId: Long): Boolean {
        return try {
            permissionService.getReadableInstitutionIdsByEmployee(userId).contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun getWriteRightsInstitutionIds(id: Long): List<Long> {
        return permissionService.getWritableInstitutionIdsByEmployee(id)
    }

    fun getReadRightsInstitutionIds(): List<Long> {
        if (isAdmin()) {
            return institutionService.getAll().map { it.id }
        }

        return permissionService.getReadableInstitutionIdsByEmployee(getId())
    }
}
