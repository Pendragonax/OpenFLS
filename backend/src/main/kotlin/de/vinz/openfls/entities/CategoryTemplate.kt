package de.vinz.openfls.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "category_templates")
class CategoryTemplate(
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    var id: Long? = null,

    @field:NotEmpty(message = "title is needed")
    @Column(length = 64)
    var title: String,

    @Column(length = 1024)
    var description: String,

    var withoutClient: Boolean,

    @JsonIgnoreProperties(value = ["services", "categoryTemplate", "hibernateLazyInitializer"])
    @OneToMany(
        mappedBy = "categoryTemplate",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY)
    var categories: MutableSet<Category>?
)