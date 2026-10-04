package de.vinz.openfls.domains.services.dto

/**
 * Verweis auf eine bestehende Entität. Weitere vom Frontend mitgesendete Felder werden ignoriert.
 */
data class IdReferenceRequest(
    val id: Long = 0
)
