package de.vinz.openfls.services

import de.vinz.openfls.repositories.ServiceRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ServiceServiceTest {
    @Mock
    lateinit var serviceRepository: ServiceRepository

    @Test
    fun getByEmployeeAndStartEndDate() {
    }
}