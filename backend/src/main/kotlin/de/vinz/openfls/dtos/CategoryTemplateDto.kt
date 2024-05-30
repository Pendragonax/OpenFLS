package de.vinz.openfls.dtos

import jakarta.validation.constraints.NotEmpty

class CategoryTemplateDto {
    var id: Long = 0

    @field:NotEmpty
    var title: String = ""

    var description: String = ""

    var withoutClient: Boolean = false

    var categories: Array<CategoryDto> = emptyArray()
}