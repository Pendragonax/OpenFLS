package de.vinz.openfls.domains.permissions

import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.clients.ClientService
import de.vinz.openfls.domains.contingents.services.ContingentService
import de.vinz.openfls.domains.goals.services.GoalService
import de.vinz.openfls.services.UserService
import org.springframework.stereotype.Service

@Service
class AccessService(
        private val userService: UserService,
        private val goalService: GoalService,
        private val contingentService: ContingentService,
        private val assistancePlanService: AssistancePlanService,
        private val permissionService: PermissionService,
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
            if (isAdmin())
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

            val clientInstitutionId = clientService.getById(clientId)?.institution?.id ?: 0

            isAffiliated(getId(), clientInstitutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyGoal(goalId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            val institutionId = assistancePlanService
                .getById(goalService.getById(goalId)?.institution?.id ?: 0)
                ?.institution?.id ?: 0

            isAffiliated(getId(), institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyAssistancePlan(assistancePlanId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            val institutionId = assistancePlanService.getById(assistancePlanId)?.institution?.id ?: 0

            isAffiliated(getId(), institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyContingent(contingentId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            val institutionId = contingentService.getById(contingentId)?.institution?.id ?: 0

            isLeader(getId(), institutionId)
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

    fun canReadEmployeeStats(employeeId: Long): Boolean {
        return try {
            // ADMIN
            if (isAdmin())
                return true

            val leadingInstitutions = this.getLeadingInstitutionIds(getId())
            val affiliatedInstitutions = this.getAffiliatedInstitutionIds(employeeId)

            leadingInstitutions.any { affiliatedInstitutions.contains(it) }
        } catch (ex: Exception) {
            false
        }
    }

    private fun isAffiliated(userId: Long, institutionId: Long): Boolean {
        return try {
            val institutions = this.getAffiliatedInstitutionIds(userId)

            institutions.contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    private fun isLeader(userId: Long, institutionId: Long): Boolean {
        return try {
            val institutions = this.getLeadingInstitutionIds(userId)

            institutions.contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    private fun canWriteEntries(userId: Long, institutionId: Long): Boolean {
        return try {
            val institutions = this.getWriteRightsInstitutionIds(userId)

            institutions.contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    private fun canReadEntries(userId: Long, institutionId: Long): Boolean {
        return try {
            val institutions = this.getReadRightsInstitutionIds(userId)

            institutions.contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    private fun getAffiliatedInstitutionIds(id: Long): List<Long> {
        return permissionService
            .getPermissionByEmployee(id)
            .filter { it.affiliated }
            .map { it.institution?.id ?: 0 }
    }

    private fun getLeadingInstitutionIds(id: Long): List<Long> {
        return permissionService
            .getPermissionByEmployee(id)
            .filter { it.changeInstitution }
            .map { it.institution?.id ?: 0 }
    }

    private fun getWriteRightsInstitutionIds(id: Long): List<Long> {
        return permissionService
            .getPermissionByEmployee(id)
            .filter { it.writeEntries }
            .map { it.institution?.id ?: 0 }
    }

    private fun getReadRightsInstitutionIds(id: Long): List<Long> {
        return permissionService
            .getPermissionByEmployee(id)
            .filter { it.readEntries }
            .map { it.institution?.id ?: 0 }
    }
}