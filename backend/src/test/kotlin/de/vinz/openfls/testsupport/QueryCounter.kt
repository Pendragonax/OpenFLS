package de.vinz.openfls.testsupport

import jakarta.persistence.EntityManagerFactory
import org.hibernate.SessionFactory

/**
 * Counts the SQL statements Hibernate prepares while a block runs. Used to prove that list
 * operations load their relations with a constant number of queries instead of one query per row.
 */
class QueryCounter(entityManagerFactory: EntityManagerFactory) {

    private val statistics = entityManagerFactory.unwrap(SessionFactory::class.java).statistics

    fun count(block: () -> Unit): Long {
        val before = statistics.prepareStatementCount
        block()
        return statistics.prepareStatementCount - before
    }
}
