package de.vinz.openfls.services

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

    private fun isAffiliated(userId: Long, institutionId: Long): Boolean {
        return try {
            val affiliatedInstitutions = permissionService
                .getPermissionByEmployee(userId)
                .filter { it.affiliated }
                .map { it.institution?.id ?: 0 }

            affiliatedInstitutions.forEach { println(it) }

            affiliatedInstitutions.contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    private fun isLeader(userId: Long, institutionId: Long): Boolean {
        return try {
            val leadingInstitutions = permissionService
                .getPermissionByEmployee(userId)
                .filter { it.changeInstitution }
                .map { it.institution?.id ?: 0 }

            leadingInstitutions.contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    private fun canWriteEntries(userId: Long, institutionId: Long): Boolean {
        return try {
            val institutions = permissionService
                .getPermissionByEmployee(userId)
                .filter { it.writeEntries }
                .map { it.institution?.id ?: 0 }

            institutions.contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }

    private fun canReadEntries(userId: Long, institutionId: Long): Boolean {
        return try {
            val institutions = permissionService
                .getPermissionByEmployee(userId)
                .filter { it.readEntries }
                .map { it.institution?.id ?: 0 }

            institutions.contains(institutionId)
        } catch (ex: Exception) {
            false
        }
    }
}