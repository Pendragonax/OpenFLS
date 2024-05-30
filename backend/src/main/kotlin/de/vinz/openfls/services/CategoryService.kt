package de.vinz.openfls.services

import de.vinz.openfls.entities.Category
import de.vinz.openfls.repositories.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import jakarta.transaction.Transactional

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository
) : GenericService<Category> {

    @Transactional
    override fun create(value: Category): Category {
        return categoryRepository.save(value)
    }

    @Transactional
    override fun update(value: Category): Category {
        if (!categoryRepository.existsById(value.id ?: 0))
            throw IllegalArgumentException("id not found")

        return categoryRepository.save(value)
    }

    @Transactional
    override fun delete(id: Long) {
        return categoryRepository.deleteById(id)
    }

    override fun getAll(): List<Category> {
        return categoryRepository.findAll().toList()
    }

    override fun getById(id: Long): Category? {
        return categoryRepository.findByIdOrNull(id)
    }

    override fun existsById(id: Long): Boolean {
        return categoryRepository.existsById(id)
    }

    fun getAllByTemplateId(id: Long): List<Category> {
        return categoryRepository.findByTemplateId(id)
    }
}