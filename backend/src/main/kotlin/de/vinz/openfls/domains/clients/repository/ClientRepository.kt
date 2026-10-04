package de.vinz.openfls.domains.clients.repository

import de.vinz.openfls.domains.clients.entity.Client
import org.springframework.data.repository.CrudRepository

interface ClientRepository : CrudRepository<Client, Long>
