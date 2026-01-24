package de.vinz.openfls.domains.institutions

import de.vinz.openfls.domains.employees.entities.EmployeeInstitutionRightsKey
import de.vinz.openfls.domains.institutions.dtos.CreateInstitutionDTO
import de.vinz.openfls.domains.institutions.dtos.ResponseByIDInstitutionDTO
import de.vinz.openfls.domains.institutions.dtos.UpdateInstitutionDTO
import de.vinz.openfls.domains.institutions.projections.InstitutionSoloProjection
import de.vinz.openfls.domains.permissions.Permission
import de.vinz.openfls.domains.permissions.PermissionDto
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.modelmapper.ModelMapper
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class InstitutionServiceTest {

    @Mock
    lateinit var institutionRepository: InstitutionRepository

    @Mock
    lateinit var modelMapper: ModelMapper

    private lateinit var institutionService: InstitutionService

    @BeforeEach
    fun setUp() {
        institutionService = InstitutionService(institutionRepository, modelMapper)
    }

    @Test
    fun create_validDto_returnsCreatedDto() {
        // Given
        val permissions = listOf(
            permissionDto(1L, 10L, read = true, write = false, change = false, affiliated = true),
            permissionDto(2L, 10L, read = true, write = true, change = true, affiliated = false)
        )
        val dto = CreateInstitutionDTO(
            name = "Alpha",
            email = "alpha@example.com",
            phonenumber = "12345",
            permissions = permissions
        )
        val savedEntity = Institution.of(dto).apply { id = 5L }
        whenever(institutionRepository.save(any())).thenReturn(savedEntity)

        // When
        val result = institutionService.create(dto)

        // Then
        val captor = argumentCaptor<Institution>()
        verify(institutionRepository).save(captor.capture())
        assertThat(captor.firstValue.name).isEqualTo("Alpha")
        assertThat(captor.firstValue.permissions).hasSize(2)
        assertThat(result.name).isEqualTo("Alpha")
        assertThat(result.email).isEqualTo("alpha@example.com")
        assertThat(result.phonenumber).isEqualTo("12345")
        assertThat(result.permissions).hasSize(2)
    }

    @Test
    fun update_existingEntity_updatesFieldsAndPermissions() {
        // Given
        val existingPermissions = mutableSetOf(
            Permission(
                id = EmployeeInstitutionRightsKey(employeeId = 1L, institutionId = 10L),
                readEntries = false,
                writeEntries = false,
                changeInstitution = false,
                affiliated = false
            ),
            Permission(
                id = EmployeeInstitutionRightsKey(employeeId = 2L, institutionId = 10L),
                readEntries = true,
                writeEntries = false,
                changeInstitution = false,
                affiliated = false
            )
        )
        val entity = Institution(
            id = 10L,
            name = "Old",
            email = "old@example.com",
            phonenumber = "111",
            permissions = existingPermissions
        )
        val dto = UpdateInstitutionDTO(
            id = 10L,
            name = "New",
            email = "new@example.com",
            phonenumber = "222",
            permissions = listOf(
                permissionDto(1L, 10L, read = true, write = true, change = true, affiliated = true),
                permissionDto(3L, 10L, read = false, write = true, change = false, affiliated = false)
            )
        )
        whenever(institutionRepository.findById(10L)).thenReturn(Optional.of(entity))
        whenever(institutionRepository.save(any())).thenAnswer { it.arguments[0] as Institution }

        // When
        val result = institutionService.update(dto)

        // Then
        assertThat(result.name).isEqualTo("New")
        assertThat(result.email).isEqualTo("new@example.com")
        assertThat(result.phonenumber).isEqualTo("222")
        assertThat(entity.permissions).hasSize(2)
        assertThat(entity.permissions?.map { it.id.employeeId }).containsExactlyInAnyOrder(1L, 3L)
        val updatedPermission = entity.permissions?.first { it.id.employeeId == 1L }
        assertThat(updatedPermission?.readEntries).isTrue()
        assertThat(updatedPermission?.writeEntries).isTrue()
        assertThat(updatedPermission?.changeInstitution).isTrue()
        assertThat(updatedPermission?.affiliated).isTrue()
    }

    @Test
    fun update_missingEntity_throwsIllegalArgumentException() {
        // Given
        val dto = UpdateInstitutionDTO(id = 999L)
        whenever(institutionRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val throwable = assertThatThrownBy { institutionService.update(dto) }

        // Then
        throwable
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Institution with id 999 not found")
    }

    @Test
    fun delete_existingId_deletesEntity() {
        // Given
        val id = 5L

        // When
        institutionService.delete(id)

        // Then
        verify(institutionRepository).deleteById(id)
    }

    @Test
    fun getAllSoloDTOs_whenProjectionsExist_returnsSortedMappedDTOs() {
        // Given
        val projection1 = mockSoloProjection(id = 1L, name = "beta", email = "b@x", phonenumber = "1")
        val projection2 = mockSoloProjection(id = 2L, name = "Alpha", email = "a@x", phonenumber = "2")
        whenever(institutionRepository.findInstitutionSoloProjectionOrderedByName()).thenReturn(
            listOf(projection1, projection2)
        )

        // When
        val result = institutionService.getAllSoloDTOs()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Alpha")
        assertThat(result[1].name).isEqualTo("beta")
    }

    @Test
    fun getAllDTOs_whenEntitiesExist_returnsSortedDTOs() {
        // Given
        val entity1 = Institution(id = 1L, name = "beta", email = "b@x", phonenumber = "1")
        val entity2 = Institution(id = 2L, name = "Alpha", email = "a@x", phonenumber = "2")
        whenever(institutionRepository.findAll()).thenReturn(listOf(entity1, entity2))
        // When
        val result = institutionService.getAllDTOs()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Alpha")
        assertThat(result[1].name).isEqualTo("beta")
    }

    @Test
    fun getAllEntities_whenEntitiesExist_returnsSortedEntities() {
        // Given
        val entity1 = Institution(id = 1L, name = "Beta")
        val entity2 = Institution(id = 2L, name = "Alpha")
        whenever(institutionRepository.findAll()).thenReturn(listOf(entity1, entity2))

        // When
        val result = institutionService.getAllEntities()

        // Then
        assertThat(result).containsExactly(entity2, entity1)
    }

    @Test
    fun getDTOById_whenEntityExists_returnsMappedDto() {
        // Given
        val entity = Institution(id = 7L, name = "Alpha")
        val dto = ResponseByIDInstitutionDTO(id = 7L, name = "Alpha")
        whenever(institutionRepository.findById(7L)).thenReturn(Optional.of(entity))
        whenever(modelMapper.map(eq(entity), eq(ResponseByIDInstitutionDTO::class.java))).thenReturn(dto)

        // When
        val result = institutionService.getDTOById(7L)

        // Then
        assertThat(result?.id).isEqualTo(7L)
        assertThat(result?.name).isEqualTo("Alpha")
    }

    @Test
    fun getDTOById_whenEntityMissing_returnsNull() {
        // Given
        whenever(institutionRepository.findById(7L)).thenReturn(Optional.empty())

        // When
        val result = institutionService.getDTOById(7L)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun getEntityById_whenEntityExists_returnsEntity() {
        // Given
        val entity = Institution(id = 7L, name = "Alpha")
        whenever(institutionRepository.findById(7L)).thenReturn(Optional.of(entity))

        // When
        val result = institutionService.getEntityById(7L)

        // Then
        assertThat(result).isEqualTo(entity)
    }

    @Test
    fun getEntityById_whenEntityMissing_returnsNull() {
        // Given
        whenever(institutionRepository.findById(7L)).thenReturn(Optional.empty())

        // When
        val result = institutionService.getEntityById(7L)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun existsById_whenEntityExists_returnsTrue() {
        // Given
        whenever(institutionRepository.existsById(4L)).thenReturn(true)

        // When
        val result = institutionService.existsById(4L)

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun existsById_whenEntityMissing_returnsFalse() {
        // Given
        whenever(institutionRepository.existsById(4L)).thenReturn(false)

        // When
        val result = institutionService.existsById(4L)

        // Then
        assertThat(result).isFalse()
    }

    private fun permissionDto(
        employeeId: Long,
        institutionId: Long,
        read: Boolean,
        write: Boolean,
        change: Boolean,
        affiliated: Boolean
    ): PermissionDto {
        return PermissionDto().apply {
            this.employeeId = employeeId
            this.institutionId = institutionId
            this.readEntries = read
            this.writeEntries = write
            this.changeInstitution = change
            this.affiliated = affiliated
        }
    }

    private fun mockSoloProjection(
        id: Long,
        name: String,
        email: String = "mail@example.com",
        phonenumber: String = "123"
    ): InstitutionSoloProjection {
        return object : InstitutionSoloProjection {
            override val id: Long = id
            override val name: String = name
            override val email: String = email
            override val phonenumber: String = phonenumber
        }
    }
}
