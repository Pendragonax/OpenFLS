package de.vinz.openfls.domains.clientTasks

import de.vinz.openfls.domains.clientTasks.dtos.ClientTaskCountDto
import org.springframework.data.jpa.repository.Query
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface ClientTaskRepository : CrudRepository<ClientTask, Long> {

    @Query(
        """
        SELECT t
        FROM ClientTask t
        LEFT JOIN FETCH t.createdBy
        LEFT JOIN FETCH t.completedBy
        WHERE t.client.id = :clientId
        ORDER BY t.done ASC, t.dueDate ASC, t.id ASC
        """
    )
    fun findAllByClientId(@Param("clientId") clientId: Long): List<ClientTask>

    fun findAllByClientIdAndDoneOrderByDueDateAscIdAsc(clientId: Long, done: Boolean): List<ClientTask>

    fun findAllByClientIdAndDoneOrderByCompletedAtDescIdDesc(
        clientId: Long,
        done: Boolean,
        pageable: Pageable
    ): Page<ClientTask>


    @Query(
        """
        SELECT count(t)
        FROM ClientTask t
        WHERE t.client.id = :clientId AND t.done = false
        """
    )
    fun countOpenByClientId(@Param("clientId") clientId: Long): Long

    @Query(
        """
        SELECT new de.vinz.openfls.domains.clientTasks.dtos.ClientTaskCountDto(
            t.client.id,
            count(t),
            sum(case when t.dueDate < :today then 1L else 0L end))
        FROM ClientTask t
        WHERE t.client.id IN :clientIds AND t.done = false
        GROUP BY t.client.id
        """
    )
    fun findOpenTaskCountsByClientIds(
        @Param("clientIds") clientIds: List<Long>,
        @Param("today") today: LocalDate
    ): List<ClientTaskCountDto>
}
