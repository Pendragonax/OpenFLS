package de.vinz.openfls.services

interface GenericService<T> {
    fun create(value: T): T
    fun update(value: T): T
    fun delete(id: Long)
    fun getAll(): List<T>
    fun getById(id: Long): T?
    fun existsById(id: Long): Boolean
}