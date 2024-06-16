package de.vinz.openfls.domains.authentication.models

enum class EUserRoles(val id: Int) {
    ADMIN(1),
    LEAD(2),
    USER(3);

    companion object {
        fun fromId(id: Int): EUserRoles {
            val role = entries.find { it.id == id }
            return role ?: USER
        }
    }
}