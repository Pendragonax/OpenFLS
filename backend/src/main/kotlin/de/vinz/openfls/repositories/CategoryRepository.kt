package de.vinz.openfls.repositories

import de.vinz.openfls.entities.Category
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface CategoryRepository : CrudRepository<Category, Long> {

    @Query("SELECT u FROM Category u WHERE u.categoryTemplate.id = :categoryTemplateId")
    fun findByTemplateId(@Param("categoryTemplateId") employeeId: Long): List<Category>

    fun findByOrderByIdAsc(): List<Category>
}