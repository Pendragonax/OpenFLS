package de.vinz.openfls.domains.categories.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.vinz.openfls.domains.categories.dtos.CategoryTemplateDto
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@Entity
@Table(name = "category_templates")
class CategoryTemplate(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @Column(length = 64)
        var title: String = "",

        @Column(length = 1024)
        var description: String = "",

        var withoutClient: Boolean = false,

        @JsonIgnoreProperties(value = ["services", "categoryTemplate", "hibernateLazyInitializer"])
        @OneToMany(
                mappedBy = "categoryTemplate",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var categories: MutableSet<Category> = mutableSetOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CategoryTemplate) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    companion object {
        fun from(categoryTemplateDto: CategoryTemplateDto): CategoryTemplate {
            return CategoryTemplate(
                    id = categoryTemplateDto.id,
                    title = categoryTemplateDto.title,
                    description = categoryTemplateDto.description,
                    withoutClient = categoryTemplateDto.withoutClient,
                    categories = categoryTemplateDto.categories.map { Category.from(it) }.toMutableSet()
            )
        }

        fun soloFrom(categoryTemplateDto: CategoryTemplateDto): CategoryTemplate {
            return CategoryTemplate(
                    id = categoryTemplateDto.id,
                    title = categoryTemplateDto.title,
                    description = categoryTemplateDto.description,
                    withoutClient = categoryTemplateDto.withoutClient
            )
        }
    }
}