package de.vinz.openfls.domains.clients.repository

import de.vinz.openfls.domains.clients.dto.ClientFavoriteRowDto
import de.vinz.openfls.domains.clients.entity.Client
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param

/** Read model of the client dashboard: the favourite clients of an employee. */
interface ClientDashboardRepository : Repository<Client, Long> {

    @Query(
        """
        SELECT new de.vinz.openfls.domains.clients.dto.ClientFavoriteRowDto(
            c.id, c.firstName, c.lastName, c.archived, i.id, i.name)
        FROM Employee e
        JOIN e.clientFavorites c
        JOIN c.institution i
        WHERE e.id = :employeeId
        ORDER BY c.lastName ASC, c.firstName ASC
        """
    )
    fun findFavoriteRowDtosByEmployeeId(@Param("employeeId") employeeId: Long): List<ClientFavoriteRowDto>

    @Query(
        """
        SELECT c.id
        FROM Employee e
        JOIN e.clientFavorites c
        WHERE e.id = :employeeId
        """
    )
    fun findFavoriteClientIdsByEmployeeId(@Param("employeeId") employeeId: Long): List<Long>
}
