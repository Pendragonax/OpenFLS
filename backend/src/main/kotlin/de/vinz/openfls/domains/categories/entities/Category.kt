package de.vinz.openfls.domains.categories.entities

import de.vinz.openfls.domains.categories.dtos.CategoryDto
import de.vinz.openfls.domains.services.Service
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@Entity
@Table(name = "categories")
data class Category(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @field:NotEmpty(message = "Title is required.")
        @Column(length = 124)
        var title: String = "",

        @field:NotEmpty(message = "Shortcut is required.")
        @Column(length = 8)
        var shortcut: String = "",

        @field:Size(max = 1024, message = "Description must not exceed 1024 characters.")
        @Column(length = 1024)
        var description: String = "",

        var faceToFace: Boolean = false,

        @ManyToOne(cascade = [CascadeType.PERSIST], fetch = FetchType.LAZY)
        @JoinColumn(name = "category_template_id")
        var categoryTemplate: CategoryTemplate? = null,

        @ManyToMany(fetch = FetchType.LAZY, mappedBy = "categorys")
        var services: MutableSet<Service> = mutableSetOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Category) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    companion object {
        fun from(categoryDto: CategoryDto): Category {
            return Category(
                    id = categoryDto.id,
                    title = categoryDto.title,
                    shortcut = categoryDto.shortcut,
                    description = categoryDto.description,
                    faceToFace = categoryDto.faceToFace,
                    categoryTemplate = CategoryTemplate(id = categoryDto.categoryTemplateId)
            )
        }
    }
}