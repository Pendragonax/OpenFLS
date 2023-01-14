package de.vinz.openfls.repositories

import de.vinz.openfls.model.Permission
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface PermissionRepository : CrudRepository<Permission, Long> {

    @Query("SELECT u FROM Permission u WHERE u.id.employeeId = :employeeId AND u.id.institutionId = :institutionId")
    fun findByIds(@Param("employeeId") employeeId: Long, @Param("institutionId") institutionId: Long): Permission?

    @Query("SELECT u FROM Permission u WHERE u.id.employeeId = :employeeId")
    fun findByEmployeeId(@Param("employeeId") employeeId: Long): Iterable<Permission>

    @Query("SELECT u FROM Permission u WHERE u.id.institutionId = :institutionId")
    fun findByInstitutionId(@Param("institutionId") institutionId: Long): Iterable<Permission>
}