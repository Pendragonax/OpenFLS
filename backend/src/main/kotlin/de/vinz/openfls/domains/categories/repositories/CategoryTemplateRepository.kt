package de.vinz.openfls.domains.categories.repositories

import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface CategoryTemplateRepository : CrudRepository<CategoryTemplate, Long> {

    @Query("SELECT u FROM CategoryTemplate u ORDER BY u.title ASC")
    fun findAllByTitle(): List<CategoryTemplate>

    fun findAllByOrderByIdAsc(): List<CategoryTemplate>
}