package de.vinz.openfls.domains.categories

import jakarta.persistence.EntityManagerFactory
import de.vinz.openfls.testsupport.QueryCounter
import de.vinz.openfls.domains.categories.dto.CategoryCreateRequest
import de.vinz.openfls.domains.categories.dto.CategoryTemplateCreateRequest
import de.vinz.openfls.domains.categories.dto.CategoryTemplateUpdateRequest
import de.vinz.openfls.domains.categories.dto.CategoryTemplateUpdateResult
import de.vinz.openfls.domains.categories.dto.CategoryUpdateRequest
import de.vinz.openfls.domains.categories.entity.Category
import de.vinz.openfls.domains.categories.entity.CategoryTemplate
import de.vinz.openfls.domains.categories.repository.CategoryRepository
import de.vinz.openfls.domains.categories.repository.CategoryTemplateRepository
import de.vinz.openfls.domains.categories.service.CategoryTemplateService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(CategoryTemplateService::class)
class CategoryTemplateServiceDataJpaTest {

    @Autowired
    lateinit var categoryTemplateService: CategoryTemplateService

    @Autowired
    lateinit var categoryTemplateRepository: CategoryTemplateRepository

    @Autowired
    lateinit var categoryRepository: CategoryRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    @Test
    fun create_withCategories_persistsTemplateAndCategories() {
        // Given
        val request = CategoryTemplateCreateRequest(
            title = "Template A",
            description = "Desc",
            withoutClient = false,
            categories = listOf(
                CategoryCreateRequest(title = "Cat 1", shortcut = "C1", description = "D1", faceToFace = true),
                CategoryCreateRequest(title = "Cat 2", shortcut = "C2", description = "D2", faceToFace = false)
            )
        )

        // When
        val result = categoryTemplateService.create(request)

        // Then
        entityManager.flush()
        entityManager.clear()
        val saved = categoryTemplateRepository.findById(result.id).get()
        assertThat(saved.categories.map { it.title }).containsExactlyInAnyOrder("Cat 1", "Cat 2")
        assertThat(result.categories.map { it.title }).containsExactly("Cat 1", "Cat 2")
        assertThat(result.categories).allMatch { it.id > 0 && it.categoryTemplateId == result.id }
    }

    @Test
    fun update_unknownTemplate_returnsNotFound() {
        // Given
        val request = CategoryTemplateUpdateRequest(id = 9999, title = "Missing")

        // When
        val result = categoryTemplateService.update(request)

        // Then
        assertThat(result).isEqualTo(CategoryTemplateUpdateResult.NotFound)
    }

    @Test
    fun update_existingTemplate_updatesDeletesAndAddsCategories() {
        // Given
        val template = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template B", description = "Desc", withoutClient = false)
        )
        val keep = categoryRepository.save(
            Category(title = "Keep", shortcut = "K", description = "D", faceToFace = true, categoryTemplate = template)
        )
        val remove = categoryRepository.save(
            Category(title = "Remove", shortcut = "R", description = "D", faceToFace = true, categoryTemplate = template)
        )
        entityManager.flush()
        entityManager.clear()

        val request = CategoryTemplateUpdateRequest(
            id = template.id,
            title = "Template B2",
            description = "Desc2",
            withoutClient = true,
            categories = listOf(
                CategoryUpdateRequest(id = keep.id, title = "Keep renamed", shortcut = "K2", description = "D2", faceToFace = false),
                CategoryUpdateRequest(title = "Added", shortcut = "A")
            )
        )

        // When
        val result = categoryTemplateService.update(request) as CategoryTemplateUpdateResult.Success

        // Then
        entityManager.flush()
        entityManager.clear()
        assertThat(categoryRepository.existsById(remove.id)).isFalse()
        val saved = categoryTemplateRepository.findById(template.id).get()
        assertThat(saved.title).isEqualTo("Template B2")
        assertThat(saved.withoutClient).isTrue()
        assertThat(saved.categories.map { it.title }).containsExactlyInAnyOrder("Keep renamed", "Added")
        assertThat(saved.categories.first { it.id == keep.id }.faceToFace).isFalse()
        assertThat(result.response.categories.map { it.title }).containsExactly("Added", "Keep renamed")
        assertThat(result.response.categories).allMatch { it.id > 0 }
    }

    @Test
    fun update_categoryOfAnotherTemplate_returnsCategoryNotInTemplateWithoutMutating() {
        // Given
        val template = categoryTemplateRepository.save(CategoryTemplate(title = "Template C"))
        val otherTemplate = categoryTemplateRepository.save(CategoryTemplate(title = "Template D"))
        val foreign = categoryRepository.save(
            Category(title = "Foreign", shortcut = "F", categoryTemplate = otherTemplate)
        )
        entityManager.flush()
        entityManager.clear()

        val request = CategoryTemplateUpdateRequest(
            id = template.id,
            title = "Template C",
            categories = listOf(CategoryUpdateRequest(id = foreign.id, title = "Foreign", shortcut = "F"))
        )

        // When
        val result = categoryTemplateService.update(request)

        // Then
        assertThat(result).isInstanceOf(CategoryTemplateUpdateResult.CategoryNotInTemplate::class.java)
        entityManager.flush()
        entityManager.clear()
        assertThat(categoryRepository.existsById(foreign.id)).isTrue()
        assertThat(categoryRepository.findById(foreign.id).get().categoryTemplate?.id).isEqualTo(otherTemplate.id)
    }

    @Test
    fun delete_existingTemplate_removesTemplateAndCategories() {
        // Given
        val template = categoryTemplateService.create(
            CategoryTemplateCreateRequest(
                title = "Delete",
                categories = listOf(CategoryCreateRequest(title = "Cat", shortcut = "C"))
            )
        )
        entityManager.flush()
        entityManager.clear()

        // When
        categoryTemplateService.delete(template.id)
        entityManager.flush()

        // Then
        assertThat(categoryTemplateRepository.existsById(template.id)).isFalse()
        assertThat(categoryRepository.findAll().toList()).isEmpty()
    }

    @Test
    fun getAll_returnsTemplatesSortedByTitleWithCategories() {
        // Given
        categoryTemplateService.create(CategoryTemplateCreateRequest(title = "B"))
        categoryTemplateService.create(
            CategoryTemplateCreateRequest(title = "A", categories = listOf(CategoryCreateRequest(title = "Cat", shortcut = "C")))
        )
        entityManager.flush()
        entityManager.clear()

        // When
        val result = categoryTemplateService.getAll()

        // Then
        assertThat(result.map { it.title }).containsExactly("A", "B")
        assertThat(result.first().categories.map { it.title }).containsExactly("Cat")
    }

    @Test
    fun getById_missingTemplate_returnsNull() {
        assertThat(categoryTemplateService.getById(9999)).isNull()
    }

    @Test
    fun getEntityById_existingTemplate_returnsEntity() {
        // Given
        val template = categoryTemplateRepository.save(CategoryTemplate(title = "Entity"))

        // When / Then
        assertThat(categoryTemplateService.getEntityById(template.id)).isEqualTo(template)
    }

    @Test
    fun getAll_loadsTheCategoriesWithAConstantNumberOfQueries() {
        // Given
        repeat(3) { createTemplate(it) }
        entityManager.flush()
        entityManager.clear()
        val queryCounter = QueryCounter(entityManagerFactory)
        val queriesForFewTemplates = queryCounter.count { categoryTemplateService.getAll() }

        repeat(17) { createTemplate(it + 3) }
        entityManager.flush()
        entityManager.clear()

        // When
        val queriesForManyTemplates = queryCounter.count { categoryTemplateService.getAll() }

        // Then
        assertThat(queriesForManyTemplates).isEqualTo(queriesForFewTemplates)
    }

    private fun createTemplate(index: Int) {
        categoryTemplateService.create(
            CategoryTemplateCreateRequest(
                title = "Template $index",
                description = "Desc",
                withoutClient = false,
                categories = listOf(
                    CategoryCreateRequest(title = "Cat A", shortcut = "A", description = "", faceToFace = true),
                    CategoryCreateRequest(title = "Cat B", shortcut = "B", description = "", faceToFace = false)
                )
            )
        )
    }
}
