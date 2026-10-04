package de.vinz.openfls.domains.categories.entity

import jakarta.persistence.*

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
        return id.hashCode()
    }

}
