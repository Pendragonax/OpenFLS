package de.vinz.openfls.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "categories")
class Category(
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    var id: Long? = null,

    @field:NotEmpty(message = "title is needed")
    @Column(length = 124)
    var title: String,

    @field:NotEmpty(message = "shortcut is needed")
    @Column(length = 8)
    var shortcut: String,

    @Column(length = 1024)
    var description: String,

    var faceToFace: Boolean,

    @ManyToOne(
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "category_template_id")
    var categoryTemplate: CategoryTemplate,

    @ManyToMany(
        fetch = FetchType.EAGER,
        mappedBy = "categorys")
    var services: MutableSet<Service> = mutableSetOf()
)