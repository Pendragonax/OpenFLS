package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

@Entity
@Table(name = "category_templates")
class CategoryTemplate(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @field:NotEmpty(message = "The title is required.")
        @Column(length = 64)
        var title: String = "",

        @field:Size(max = 1024, message = "The description must not exceed 1024 characters.")
        @Column(length = 1024)
        var description: String = "",

        var withoutClient: Boolean = false,

        @JsonIgnoreProperties(value = ["services", "categoryTemplate", "hibernateLazyInitializer"])
        @OneToMany(
                mappedBy = "categoryTemplate",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY)
        var categories: MutableSet<Category> = mutableSetOf()
)