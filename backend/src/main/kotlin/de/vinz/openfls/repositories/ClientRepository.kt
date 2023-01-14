package de.vinz.openfls.repositories

import de.vinz.openfls.model.Client
import org.springframework.data.repository.CrudRepository

interface ClientRepository : CrudRepository<Client, Long>