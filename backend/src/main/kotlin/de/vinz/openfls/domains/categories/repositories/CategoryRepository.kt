package de.vinz.openfls.domains.categories.repositories

import de.vinz.openfls.domains.categories.entities.Category
import org.springframework.data.repository.CrudRepository

interface CategoryRepository : CrudRepository<Category, Long> {}