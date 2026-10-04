package de.vinz.openfls.architecture

/**
 * Kennzeichnet Service-Methoden, die JPA-Entities herausgeben oder entgegennehmen.
 * Sie sind ausschließlich für andere Services gedacht und dürfen nie von einem
 * RestController aufgerufen werden (durch ArchTests abgesichert).
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class InternalEntityApi
