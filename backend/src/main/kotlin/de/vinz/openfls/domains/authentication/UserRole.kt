package de.vinz.openfls.domains.authentication

enum class UserRole(val id: Int) {
    ADMIN(1),
    LEAD(2),
    USER(3);

    companion object {
        fun fromId(id: Int): UserRole {
            val role = entries.find { it.id == id }
            return role ?: USER
        }
    }
}
