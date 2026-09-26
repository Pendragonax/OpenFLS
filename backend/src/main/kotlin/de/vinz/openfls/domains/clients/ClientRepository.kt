package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dtos.ClientFavoriteRowDto
import de.vinz.openfls.domains.clients.dtos.ClientInstitutionDto
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param


interface ClientRepository : CrudRepository<Client, Long> {
    @Query("SELECT new de.vinz.openfls.domains.clients.dtos.ClientInstitutionDto(c.id, c.firstName, c.lastName, c.phoneNumber, c.email, i.id, i.name, i.email, i.phonenumber) FROM Client c JOIN c.institution i")
    fun findAllClientSimpleDto(): List<ClientInstitutionDto>

    @Query(
        """
        SELECT new de.vinz.openfls.domains.clients.dtos.ClientFavoriteRowDto(
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