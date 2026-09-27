package de.vinz.openfls.domains.permissions.repository

import de.vinz.openfls.domains.employees.entities.EmployeeInstitutionRightsKey
import de.vinz.openfls.domains.permissions.entity.Permission
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface PermissionRepository : CrudRepository<Permission, EmployeeInstitutionRightsKey> {

    @Query("SELECT u FROM Permission u WHERE u.id.employeeId = :employeeId")
    fun findByEmployeeId(@Param("employeeId") employeeId: Long): Iterable<Permission>
}
