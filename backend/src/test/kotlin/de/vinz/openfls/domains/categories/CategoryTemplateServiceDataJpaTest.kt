package de.vinz.openfls.domains.categories

import de.vinz.openfls.domains.categories.dtos.CategoryDto
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateDto
import de.vinz.openfls.domains.categories.entities.Category
import de.vinz.openfls.domains.categories.entities.CategoryTemplate
import de.vinz.openfls.domains.categories.exceptions.InvalidCategoryTemplateDtoException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(CategoryTemplateService::class)
class CategoryTemplateServiceDataJpaTest {

    @Autowired
    lateinit var categoryTemplateService: CategoryTemplateService

    @Autowired
    lateinit var categoryTemplateRepository: de.vinz.openfls.domains.categories.repositories.CategoryTemplateRepository

    @Autowired
    lateinit var categoryRepository: de.vinz.openfls.domains.categories.repositories.CategoryRepository

    @Test
    fun create_withCategories_persistsTemplateAndCategories() {
        // Given
        val dto = CategoryTemplateDto(
            title = "Template A",
            description = "Desc",
            withoutClient = false,
            categories = listOf(
                CategoryDto(title = "Cat 1", shortcut = "C1", description = "D1", faceToFace = true),
                CategoryDto(title = "Cat 2", shortcut = "C2", description = "D2", faceToFace = false)
            )
        )

        // When
        val result = categoryTemplateService.create(dto)

        // Then
        val saved = categoryTemplateRepository.findById(result.id)
        assertThat(saved).isPresent
        assertThat(saved.get().categories).isNotEmpty
        assertThat(saved.get().categories.map { it.title }).contains("Cat 1")
    }

    @Test
    fun update_unknownTemplate_throwsException() {
        // Given
        val dto = CategoryTemplateDto(
            id = 9999,
            title = "Missing",
            description = "Desc",
            withoutClient = false,
            categories = emptyList()
        )

        // When / Then
        assertThatThrownBy { categoryTemplateService.update(dto) }
            .isInstanceOf(InvalidCategoryTemplateDtoException::class.java)
    }

    @Test
    fun update_existingTemplate_deletesRemovedCategories() {
        // Given
        val template = categoryTemplateRepository.save(
            CategoryTemplate(title = "Template B", description = "Desc", withoutClient = false)
        )
        val category1 = categoryRepository.save(
            Category(title = "Cat 1", shortcut = "C1", description = "D1", faceToFace = true, categoryTemplate = template)
        )
        val category2 = categoryRepository.save(
            Category(title = "Cat 2", shortcut = "C2", description = "D2", faceToFace = true, categoryTemplate = template)
        )
        val removedCategoryId = category2.id

        val dto = CategoryTemplateDto(
            id = template.id,
            title = "Template B",
            description = "Desc",
            withoutClient = false,
            categories = listOf(
                CategoryDto(
                    id = category1.id,
                    title = "Cat 1",
                    shortcut = "C1",
                    description = "D1",
                    faceToFace = true,
                    categoryTemplateId = template.id
                )
            )
        )

        // When
        categoryTemplateService.update(dto)

        // Then
        val remaining = categoryRepository.findAll().toList()
        assertThat(remaining.map { it.id }).contains(category1.id)
    }
}
