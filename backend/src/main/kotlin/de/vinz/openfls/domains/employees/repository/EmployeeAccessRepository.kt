package de.vinz.openfls.domains.employees.repository

import de.vinz.openfls.domains.employees.entity.EmployeeAccess
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface EmployeeAccessRepository : CrudRepository<EmployeeAccess, Long> {

    @Query("SELECT u FROM EmployeeAccess u WHERE u.username = :username")
    fun findByUsername(@Param("username") username: String): EmployeeAccess?

    @Modifying
    @Query("UPDATE EmployeeAccess u SET u.password = :password WHERE u.id = :id")
    fun changePassword(@Param("id") id: Long,
                       @Param("password") password: String): Int
}
