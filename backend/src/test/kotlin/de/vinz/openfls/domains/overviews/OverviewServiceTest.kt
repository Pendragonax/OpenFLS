package de.vinz.openfls.domains.overviews

import de.vinz.openfls.domains.assistancePlans.repositories.AssistancePlanRepository
import de.vinz.openfls.domains.clients.ClientRepository
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.services.ServiceRepository
import de.vinz.openfls.exceptions.IllegalTimeException
import de.vinz.openfls.exceptions.UserNotAllowedException
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.modelmapper.ModelMapper

class OverviewServiceTest {

    private lateinit var overviewService: OverviewService
    private lateinit var accessService: AccessService
    private lateinit var serviceRepository: ServiceRepository
    private lateinit var assistancePlanRepository: AssistancePlanRepository
    private lateinit var clientRepository: ClientRepository
    private lateinit var modelMapper: ModelMapper

    @BeforeEach
    fun setup() {
        accessService = mock()
        serviceRepository = mock()
        assistancePlanRepository = mock()
        clientRepository = mock()
        modelMapper = mock()

        overviewService = OverviewService(
            accessService, serviceRepository, assistancePlanRepository, clientRepository, modelMapper
        )
    }

    @Test
    fun checkYearMonth_whenValidYearAndMonth_thenNoException() {
        assertThatCode { overviewService.checkYearMonth(2023, 5) }
            .doesNotThrowAnyException()
    }

    @Test
    fun checkYearMonth_whenInvalidMonth_thenThrowIllegalTimeException() {
        assertThrows(IllegalTimeException::class.java) {
            overviewService.checkYearMonth(2023, 13)
        }
        assertThrows(IllegalTimeException::class.java) {
            overviewService.checkYearMonth(2023, 0)
        }
        assertThrows(IllegalTimeException::class.java) {
            overviewService.checkYearMonth(-1, 13)
        }
    }

    @Test
    fun checkAccess_whenUserIsAdmin_thenNoException() {
        `when`(accessService.isAdmin()).thenReturn(true)

        assertThatCode { overviewService.checkAccess(null) }
            .doesNotThrowAnyException()
    }

    @Test
    fun checkAccess_whenUserIsNotAdminAndNoAccess_thenThrowUserNotAllowedException() {
        `when`(accessService.isAdmin()).thenReturn(false)
        `when`(accessService.canReadEntries(1L)).thenReturn(false)

        assertThrows(UserNotAllowedException::class.java) {
            overviewService.checkAccess(1L)
        }
    }

    @Test
    fun checkAccess_whenUserIsNotAdminAndNoAccess_thenNoException() {
        `when`(accessService.isAdmin()).thenReturn(false)
        `when`(accessService.canReadEntries(1L)).thenReturn(true)

        assertThatCode { overviewService.checkAccess(1L) }
            .doesNotThrowAnyException()
    }
}