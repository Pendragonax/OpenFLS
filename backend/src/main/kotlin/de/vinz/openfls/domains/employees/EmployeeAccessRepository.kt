package de.vinz.openfls.domains.employees

import de.vinz.openfls.domains.employees.entities.EmployeeAccess
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface EmployeeAccessRepository : CrudRepository<EmployeeAccess, Long> {

    @Query("SELECT u FROM EmployeeAccess u WHERE u.username = :username")
    fun getEmployeeByUsername(@Param("username") username: String): EmployeeAccess?

    @Modifying
    @Query("UPDATE EmployeeAccess u SET u.password = :password WHERE u.id = :id")
    fun changePassword(@Param("id") id: Long,
                       @Param("password") password: String): Int

    @Modifying
    @Query("UPDATE EmployeeAccess u SET u.role = :role WHERE u.id = :id")
    fun changeRole(@Param("id") id: Long, @Param("role") role: Int): Int
}