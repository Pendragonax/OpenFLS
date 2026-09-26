package de.vinz.openfls.domains.categories.repositories

import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import org.springframework.data.repository.CrudRepository

interface CategoryTemplateRepository : CrudRepository<CategoryTemplate, Long>
