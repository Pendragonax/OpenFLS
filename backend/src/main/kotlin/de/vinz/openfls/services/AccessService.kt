package de.vinz.openfls.services

import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.contingents.services.ContingentService
import org.springframework.stereotype.Service

@Service
class AccessService(
        private val tokenService: TokenService,
        private val employeeService: EmployeeService,
        private val goalService: GoalService,
        private val contingentService: ContingentService,
        private val assistancePlanService: AssistancePlanService,
        private val permissionService: PermissionService,
        private val clientService: ClientService
) {
    /**
     * Checks if the user is an admin
     * @return true = admin, false = no admin
     */
    fun isAdmin(userToken: String): Boolean {
        return tokenService.getUserInfo(userToken).second
    }

    fun getId(userToken: String): Long {
        return tokenService.getUserInfo(userToken).first
    }

    /**
     * Checks if the user is affiliated to the institution.
     * @return true = affiliated or admin, false = not affiliated or admin
     */
    fun isAffiliated(userToken: String, institutionId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            // ADMIN
            if (userInfo.second)
                return true

            isAffiliated(userInfo.first, institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * Checks if the user is the leader of the institution.
     * @return true = leader or admin, false = no leader or admin
     */
    fun isLeader(userToken: String, institutionId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            // ADMIN
            if (userInfo.second)
                return true

            isLeader(userInfo.first, institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canWriteEntries(userToken: String, institutionId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            // ADMIN
            if (userInfo.second)
                return true

            canWriteEntries(userInfo.first, institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canReadEntries(userToken: String, institutionId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            // ADMIN
            if (userInfo.second)
                return true

            canReadEntries(userInfo.first, institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyClient(userToken: String, clientId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            // ADMIN
            if (userInfo.second)
                return true

            val clientInstitutionId = clientService.getById(clientId)?.institution?.id ?: 0

            isAffiliated(userInfo.first, clientInstitutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyGoal(userToken: String, goalId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            // ADMIN
            if (userInfo.second)
                return true

            val institutionId = assistancePlanService
                .getById(goalService.getById(goalId)?.institution?.id ?: 0)
                ?.institution?.id ?: 0

            isAffiliated(userInfo.first, institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyAssistancePlan(userToken: String, assistancePlanId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            // ADMIN
            if (userInfo.second)
                return true

            val institutionId = assistancePlanService.getById(assistancePlanId)?.institution?.id ?: 0

            isAffiliated(userInfo.first, institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyContingent(userToken: String, contingentId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            // ADMIN
            if (userInfo.second)
                return true

            val institutionId = contingentService.getById(contingentId)?.institution?.id ?: 0

            isLeader(userInfo.first, institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    fun canModifyEmployee(userToken: String, employeeId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            userInfo.second
        } catch (ex: Exception) {
            false
        }
    }

    fun canReadEmployeeStats(userToken: String, employeeId: Long): Boolean {
        return try {
            val userInfo = tokenService.getUserInfo(userToken)

            // ADMIN
            if (userInfo.second)
                return true

            val leadingInstitutions = this.getLeadingInstitutionIds(userInfo.first)
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