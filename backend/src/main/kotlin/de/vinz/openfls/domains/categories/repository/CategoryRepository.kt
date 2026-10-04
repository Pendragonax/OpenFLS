package de.vinz.openfls.domains.categories.repository

import de.vinz.openfls.domains.categories.entity.Category
import org.springframework.data.repository.CrudRepository

interface CategoryRepository : CrudRepository<Category, Long> {}