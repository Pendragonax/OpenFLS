package de.vinz.openfls.domains.clients

import de.vinz.openfls.domains.clients.dtos.ClientInstitutionDto
import de.vinz.openfls.domains.clients.dtos.ClientSoloDto
import de.vinz.openfls.domains.clients.projections.ClientSoloProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository


interface ClientRepository : CrudRepository<Client, Long> {
    @Query("SELECT new de.vinz.openfls.domains.clients.dtos.ClientInstitutionDto(c.id, c.firstName, c.lastName, c.phoneNumber, c.email, i.id, i.name, i.email, i.phonenumber) FROM Client c JOIN c.institution i")
    fun findAllClientSimpleDto(): List<ClientInstitutionDto>

    fun findAllClientSoloProjectionBy(): List<ClientSoloProjection>
}