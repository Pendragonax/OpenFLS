package de.vinz.openfls.services

import de.vinz.openfls.repositories.AssistancePlanRepository
import de.vinz.openfls.repositories.ClientRepository
import de.vinz.openfls.repositories.ServiceRepository
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.modelmapper.ModelMapper


@ExtendWith(MockitoExtension::class)
class OverviewServiceTest {
    private var permissionService: PermissionService = mockk()

    private var serviceRepository: ServiceRepository = mockk()

    private var assistancePlanRepository: AssistancePlanRepository = mockk()

    private var clientRepository: ClientRepository = mockk()


    @Test
    fun test() {
        assertThat(true).isTrue();
    }
}