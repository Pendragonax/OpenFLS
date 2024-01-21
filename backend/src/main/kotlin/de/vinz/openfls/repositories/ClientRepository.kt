package de.vinz.openfls.repositories

import de.vinz.openfls.entities.Client
import org.springframework.data.repository.CrudRepository

interface ClientRepository : CrudRepository<Client, Long>