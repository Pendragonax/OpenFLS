package de.vinz.openfls.domains.contingents.services

import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.contingents.ContingentRepository
import de.vinz.openfls.domains.contingents.projections.ContingentProjection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.*

@ExtendWith(MockitoExtension::class)
class ContingentServiceTest {

    @Mock
    lateinit var contingentRepository: ContingentRepository

    private lateinit var contingentService: ContingentService

    @BeforeEach
    fun setUp() {
        val workdaysReal = 251L
        val workdaysAssumption = 195L
        contingentService = ContingentService(contingentRepository, workdaysReal, workdaysAssumption)
    }

    @Test
    fun testCreate_WhenValidContingent_ThenReturnSavedContingent() {
        // Given
        val contingent = mock(Contingent::class.java)
        whenever(contingentRepository.save(contingent)).thenReturn(contingent)

        // When
        val result = contingentService.create(contingent)

        // Then
        assertThat(result).isEqualTo(contingent)
        verify(contingentRepository, times(1)).save(contingent)
    }

    @Test
    fun testUpdate_WhenContingentExists_ThenReturnUpdatedContingent() {
        // Given
        val contingent = mock(Contingent::class.java)
        whenever(contingent.id).thenReturn(1L)
        whenever(contingentRepository.existsById(1L)).thenReturn(true)
        whenever(contingentRepository.save(contingent)).thenReturn(contingent)

        // When
        val result = contingentService.update(contingent)

        // Then
        assertThat(result).isEqualTo(contingent)
        verify(contingentRepository, times(1)).save(contingent)
    }

    @Test
    fun testUpdate_WhenContingentDoesNotExist_ThenThrowException() {
        // Given
        val contingent = mock(Contingent::class.java)
        whenever(contingent.id).thenReturn(1L)
        whenever(contingentRepository.existsById(1L)).thenReturn(false)

        // When & Then
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            contingentService.update(contingent)
        }.also {
            assertThat(it).hasMessage("id not found")
        }
        verify(contingentRepository, times(0)).save(contingent)
    }

    @Test
    fun testDelete_WhenValidId_ThenPerformDeletion() {
        // Given
        val id = 1L
        doNothing().`when`(contingentRepository).deleteById(id)

        // When
        contingentService.delete(id)

        // Then
        verify(contingentRepository, times(1)).deleteById(id)
    }

    @Test
    fun testGetById_WhenValidId_ThenReturnContingent() {
        // Given
        val id = 1L
        val contingent: Contingent = mock(Contingent::class.java)
        whenever(contingentRepository.findById(id)).thenReturn(Optional.of(contingent))

        // When
        val result = contingentService.getById(id)

        // Then
        assertThat(result).isEqualTo(contingent)
    }

    @Test
    fun testGetById_WhenInvalidId_ThenReturnNull() {
        // Given
        val id = 1L
        whenever(contingentRepository.findById(id)).thenReturn(Optional.empty())

        // When
        val result = contingentService.getById(id)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun testGetContingentHoursByYearAndMonth_WhenInYearMonth_ThenReturnCorrectHours() {
        // Given
        val contingent = mock(ContingentProjection::class.java)
        whenever(contingent.start).thenReturn(LocalDate.of(2024, 1, 1))
        whenever(contingent.end).thenReturn(LocalDate.of(2024, 12, 31))
        whenever(contingent.weeklyServiceHours).thenReturn(40.0)

        // When
        val hours = contingentService.getContingentHoursByYearAndMonth(2024, 1, contingent)

        // Then
        assertThat(hours).isGreaterThan(0.0)
        assertThat(hours).isEqualTo(177.09)
    }

    @Test
    fun testIsContingentInYearMonth_WhenContingentInYearMonth_ThenReturnTrue() {
        // Given
        val contingent = mock(ContingentProjection::class.java)
        whenever(contingent.start).thenReturn(LocalDate.of(2024, 1, 1))
        whenever(contingent.end).thenReturn(LocalDate.of(2024, 12, 31))

        // When
        val result = contingentService.isContingentInYearMonth(2024, 1, contingent)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun testIsContingentInYearMonth_WhenContingentNotInYearMonth_ThenReturnFalse() {
        // Given
        val contingent = mock(ContingentProjection::class.java)
        whenever(contingent.start).thenReturn(LocalDate.of(2023, 1, 1))
        whenever(contingent.end).thenReturn(LocalDate.of(2023, 12, 31))

        // When
        val result = contingentService.isContingentInYearMonth(2024, 1, contingent)

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun testGetContingentHoursByYear_WhenValidContingents_ThenReturnHours() {
        // Given
        val contingent = mock(ContingentProjection::class.java)
        whenever(contingent.start).thenReturn(LocalDate.of(2024, 1, 1))
        whenever(contingent.end).thenReturn(LocalDate.of(2024, 3, 15))
        whenever(contingent.weeklyServiceHours).thenReturn(7.0)
        val contingent2 = mock(ContingentProjection::class.java)
        whenever(contingent2.start).thenReturn(LocalDate.of(2024, 3, 16))
        whenever(contingent2.end).thenReturn(LocalDate.of(2024, 12, 31))
        whenever(contingent2.weeklyServiceHours).thenReturn(7.0)

        val contingents = listOf(contingent, contingent2)

        // When
        val hours = contingentService.getContingentHoursByYear(2024, contingents)

        // Then
        assertThat(hours).isNotEmpty
        assertThat(hours[0]).isEqualTo(284.21)
        assertThat(hours[1]).isEqualTo(31.00)
        assertThat(hours[2]).isEqualTo(29.00)
        assertThat(hours[3]).isEqualTo(31.00)
        assertThat(hours[4]).isEqualTo(30.00)
        assertThat(hours[5]).isEqualTo(31.00)
        assertThat(hours[6]).isEqualTo(30.00)
        assertThat(hours[7]).isEqualTo(31.00)
        assertThat(hours[8]).isEqualTo(31.00)
        assertThat(hours[9]).isEqualTo(30.00)
        assertThat(hours[10]).isEqualTo(31.00)
        assertThat(hours[11]).isEqualTo(30.00)
        assertThat(hours[12]).isEqualTo(31.00)
    }

    @Test
    fun testGetContingentHoursByYear_WhenValidContingentPartiallyInYear_ThenReturnHours() {
        // Given
        val contingent = mock(ContingentProjection::class.java)
        whenever(contingent.start).thenReturn(LocalDate.of(2024, 1, 1))
        whenever(contingent.end).thenReturn(LocalDate.of(2024, 2, 29))
        whenever(contingent.weeklyServiceHours).thenReturn(7.0)

        // When
        val hours = contingentService.getContingentHoursByYear(2024, contingent)

        // Then
        assertThat(hours).isNotEmpty
        assertThat(hours[0]).isEqualTo(46.35)
        assertThat(hours[1]).isEqualTo(31.00)
        assertThat(hours[2]).isEqualTo(29.00)
    }

    @Test
    fun testGetContingentHoursByYear_WhenValidContingentFullYear_ThenReturnHours() {
        // Given
        val contingent = mock(ContingentProjection::class.java)
        whenever(contingent.start).thenReturn(LocalDate.of(2024, 1, 1))
        whenever(contingent.weeklyServiceHours).thenReturn(7.0)

        // When
        val hours = contingentService.getContingentHoursByYear(2024, contingent)

        // Then
        assertThat(hours).isNotEmpty
        assertThat(hours[0]).isEqualTo(273.0)
        assertThat(hours[1]).isEqualTo(31.00)
        assertThat(hours[2]).isEqualTo(29.00)
        assertThat(hours[3]).isEqualTo(31.00)
        assertThat(hours[4]).isEqualTo(30.00)
        assertThat(hours[5]).isEqualTo(31.00)
        assertThat(hours[6]).isEqualTo(30.00)
        assertThat(hours[7]).isEqualTo(31.00)
        assertThat(hours[8]).isEqualTo(31.00)
        assertThat(hours[9]).isEqualTo(30.00)
        assertThat(hours[10]).isEqualTo(31.00)
        assertThat(hours[11]).isEqualTo(30.00)
        assertThat(hours[12]).isEqualTo(31.00)
    }

    @Test
    fun testGetContingentHoursByYear_WhenValidContingentFullTimeYear_ThenReturnHours() {
        // Given
        val contingent = mock(ContingentProjection::class.java)
        whenever(contingent.start).thenReturn(LocalDate.of(2024, 1, 1))
        whenever(contingent.weeklyServiceHours).thenReturn(30.27)

        // When
        val hours = contingentService.getContingentHoursByYear(2024, contingent)

        // Then
        assertThat(hours).isNotEmpty
        assertThat(hours[0]).isEqualTo(1180.32)
        assertThat(hours[1]).isEqualTo(134.03)
        assertThat(hours[12]).isEqualTo(134.03)
    }

    @Test
    fun testExistsById_WhenContingentExists_ThenReturnTrue() {
        // Given
        val id = 1L
        whenever(contingentRepository.existsById(id)).thenReturn(true)

        // When
        val result = contingentService.existsById(id)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun testExistsById_WhenContingentDoesNotExist_ThenReturnFalse() {
        // Given
        val id = 1L
        whenever(contingentRepository.existsById(id)).thenReturn(false)

        // When
        val result = contingentService.existsById(id)

        // Then
        assertThat(result).isFalse()
    }
}
