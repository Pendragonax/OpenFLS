package de.vinz.openfls.dtos

import jakarta.validation.constraints.NotEmpty

class CategoryDto {
    var id: Long = 0

    @field:NotEmpty
    var title: String = ""

    @field:NotEmpty
    var shortcut: String = ""

    var description: String = ""

    var faceToFace: Boolean = true

    var categoryTemplateId: Long = 0
}