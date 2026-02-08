package de.vinz.openfls.domains.services

import de.vinz.openfls.domains.assistancePlans.services.AssistancePlanService
import de.vinz.openfls.domains.employees.services.EmployeeService
import de.vinz.openfls.domains.permissions.AccessService
import de.vinz.openfls.domains.permissions.PermissionService
import de.vinz.openfls.domains.services.dtos.ServiceDto
import de.vinz.openfls.domains.services.dtos.ServiceFilterDto
import de.vinz.openfls.domains.services.exceptions.ServicePermissionDeniedException
import de.vinz.openfls.domains.contingents.services.ContingentCalendarService
import de.vinz.openfls.domains.services.dtos.ClientAndDateRequestDTO
import de.vinz.openfls.domains.services.dtos.ClientAndDateResponseDTO
import de.vinz.openfls.domains.services.services.ServiceService
import de.vinz.openfls.logback.PerformanceLogbackFilter
import jakarta.validation.Valid
import org.apache.coyote.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/services")
class ServiceController(
    private val serviceService: ServiceService,
    private val contingentCalendarService: ContingentCalendarService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService,
    private val permissionService: PermissionService,
    private val assistancePlanService: AssistancePlanService
) {

    private val logger: Logger = LoggerFactory.getLogger(ServiceController::class.java)

    @Value("\${logging.performance}")
    private val logPerformance: Boolean = false

    @PostMapping
    fun create(@Valid @RequestBody valueDto: ServiceDto): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.canWriteEntries(valueDto.institutionId))
                throw IllegalArgumentException("No permission to write entries to this institution")

            val dto = serviceService.create(valueDto)

            if (logPerformance) {
                logger.info(String.format("%s create took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    employeeService.getById(valueDto.employeeId).apply {
                        this?.access?.password = ""
                    },
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @PutMapping("{id}")
    fun update(@PathVariable id: Long,
               @Valid @RequestBody valueDto: ServiceDto): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.canWriteEntries(valueDto.institutionId))
                throw IllegalArgumentException("No permission to update this service")
            if (!serviceService.existsById(id))
                throw IllegalArgumentException("service not found")
            if (!accessService.isAdmin() &&
                    serviceService.getById(id)?.employee?.id != accessService.getId())
                throw IllegalArgumentException("Your not the owner of this service or the admin")

            val dto = serviceService.update(valueDto)

            if (logPerformance) {
                logger.info(String.format("%s update took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val service = serviceService.getById(id)

            if (!accessService.isAdmin() &&
                    (service?.employee?.id != accessService.getId() ||
                            service.start.toLocalDate().isBefore(LocalDate.now().minusDays(14))))
                throw IllegalArgumentException("No permission to delete this service")
            if (!serviceService.existsById(id))
                throw IllegalArgumentException("service not found")

            val dto = serviceService.getDtoById(id)
            serviceService.delete(id)

            if (logPerformance) {
                logger.info(String.format("%s delete took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping
    fun getAll(): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (!accessService.isAdmin())
                throw IllegalArgumentException("No permission to get all services")

            val dtos = serviceService.getAllDtos()

            if (logPerformance) {
                logger.info(String.format("%s getAll took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (id <= 0)
                throw IllegalArgumentException("id is <= 0")
            if (!serviceService.existsById(id))
                throw IllegalArgumentException("service not found")

            val dto = serviceService.getDtoById(id)

            if (dto != null && !accessService.isAdmin() &&
                    !accessService.canReadEntries(dto.institutionId))
                throw IllegalArgumentException("Your not the allowed to read this entry")


            if (logPerformance) {
                logger.info(String.format("%s getById took %d ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("assistance_plan/{id}")
    fun getByAssistancePlan(@PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (id <= 0)
                throw IllegalArgumentException("id is <= 0")
            if (!assistancePlanService.existsById(id))
                throw IllegalArgumentException("assistance plan not found")
            if (!accessService.canModifyAssistancePlan(id))
                throw IllegalArgumentException("no permission to load the services of this assistance plan")

            val dtos = serviceService.getXLDtosByAssistancePlan(id)

            if (logPerformance) {
                logger.info(String.format("%s getByAssistancePlan took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("assistance_plan/{id}/illegal")
    fun getIllegalByAssistancePlan(@PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (id <= 0)
                throw IllegalArgumentException("id is <= 0")
            if (!assistancePlanService.existsById(id))
                throw IllegalArgumentException("assistance plan not found")
            if (!accessService.canModifyAssistancePlan(id))
                throw IllegalArgumentException("no permission to load the services of this assistance plan")

            val dtos = serviceService.getIllegalByAssistancePlan(id)

            if (logPerformance) {
                logger.info(String.format("%s getIllegalByAssistancePlan took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("assistance_plan/{id}/not_between/{start}/{end}")
    fun getByAssistancePlanAndNotBetweenStartAndEnd(@PathVariable id: Long,
                                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (id <= 0)
                throw IllegalArgumentException("id is <= 0")
            if (!assistancePlanService.existsById(id))
                throw IllegalArgumentException("assistance plan not found")
            if (!accessService.canModifyAssistancePlan(id))
                throw IllegalArgumentException("no permission to load the services of this assistance plan")

            val dtos = serviceService.getByAssistancePlanAndNotBetweenStartAndEnd(id, start, end)

            if (logPerformance) {
                logger.info(String.format("%s getByAssistancePlanAndStartAndEnd took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}/illegal")
    fun getIllegalByEmployee(@PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.isAdmin() && !accessService.canReadEmployee(id) && accessService.getId() != id) {
                throw IllegalArgumentException("No permission to load the illegal services of this employee")
            }

            val dtos = serviceService.getIllegalByEmployee(id)

            if (logPerformance) {
                logger.info(String.format("%s getIllegalByEmployee took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}/{date}")
    fun getByEmployeeAndDate(@PathVariable id: Long,
                             @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val leadingInstitutionIds = permissionService
                    .getLeadingInstitutionIdsByEmployee(accessService.getId())
            val userId = accessService.getId()
            val isAdmin = accessService.isAdmin()

            val dtos = serviceService.getDtosByEmployeeAndDate(id, date)
                    .filter { isAdmin || it.employeeId == userId || leadingInstitutionIds.contains(it.institutionId) }
                    .sortedBy { it.start }

            if (logPerformance) {
                logger.info(String.format("%s getByEmployeeAndDate took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}/{start}/{end}")
    fun getByEmployeeAndStartAndEnd(@PathVariable id: Long,
                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val leadingInstitutionIds = permissionService
                    .getLeadingInstitutionIdsByEmployee(accessService.getId())
            val userId = accessService.getId()
            val isAdmin = accessService.isAdmin()

            val dtos = serviceService.getByEmployeeAndStartAndEnd(id, start, end)
                    .filter { isAdmin || it.employee.id == userId || leadingInstitutionIds.contains(it.institution.id) }
                    .sortedBy { it.start }

            if (logPerformance) {
                logger.info(String.format("%s getByEmployeeAndStartAndEnd took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("institution/{id}/{start}/{end}")
    fun getByInstitutionIdAndStartAndEnd(@PathVariable id: Long,
                                         @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                         @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.canReadEntries(id)) {
                throw IllegalArgumentException("no permission to load the services of this institution")
            }

            val dtos = serviceService.getDtosByInstitutionIdAndStartAndEnd(id, start, end)

            if (logPerformance) {
                logger.info(String.format("%s getByInstitutionIdAndStartAndEnd took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("institution/{id}/{date}")
    fun getByInstitutionIdAndDate(@PathVariable id: Long,
                                  @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.canReadEntries(id)) {
                throw IllegalArgumentException("no permission to load the services of this institution")
            }

            val dtos = serviceService.getDtosByInstitutionIdAndDate(id, date)

            if (logPerformance) {
                logger.info(String.format("%s getByInstitutionIdAndDate took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("institution/{id}/illegal")
    fun getIllegalByInstitutionId(@PathVariable id: Long): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.canReadEntries(id)) {
                throw IllegalArgumentException("no permission to load the services of this institution")
            }

            val dtos = serviceService.getIllegalByInstitutionId(id)

            if (logPerformance) {
                logger.info(String.format("%s getIllegalByInstitutionId took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("client/{id}/{date}")
    fun getByClientAndDate(@PathVariable id: Long,
                           @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val affiliatedInstitutionIds = permissionService
                    .getReadableInstitutionIdsByEmployee(accessService.getId())
            val userId = accessService.getId()
            val isAdmin = accessService.isAdmin()

            val dtos = serviceService.getDtosByClientAndDate(id, date)
                    .filter { isAdmin || it.employeeId == userId || affiliatedInstitutionIds.contains(it.institutionId) }

            if (logPerformance) {
                logger.info(String.format("%s getByClientAndDate took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("client/{id}/{start}/{end}")
    fun getByClientAndStartAndEnd(@PathVariable id: Long,
                                  @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                  @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            val affiliatedInstitutionIds = permissionService
                    .getReadableInstitutionIdsByEmployee(accessService.getId())
            val userId = accessService.getId()
            val isAdmin = accessService.isAdmin()

            val dtos = serviceService.getDtosByClientAndStartAndEnd(id, start, end)
                    .filter { isAdmin || it.employeeId == userId || affiliatedInstitutionIds.contains(it.institutionId) }

            if (logPerformance) {
                logger.info(String.format("%s getByClientAndStartAndEnd took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("institution/{institutionId}/client/{clientId}/{start}/{end}")
    fun getByInstitutionIdAndClientIdAndStartAndEnd(@PathVariable institutionId: Long,
                                                    @PathVariable clientId: Long,
                                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.canReadEntries(institutionId)) {
                throw IllegalArgumentException("no permission to load the services of this institution")
            }

            val readableInstitutions = accessService.getReadRightsInstitutionIds();
            val dtos = serviceService.getProjections(institutionId, clientId, start, end, readableInstitutions)

            if (logPerformance) {
                logger.info(String.format("%s getByInstitutionIdAndClientIdAndStartAndEnd took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("institution/{institutionId}/employee/{employeeId}/client/{clientId}/{start}/{end}")
    fun getByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd(@PathVariable institutionId: Long,
                                                                 @PathVariable employeeId: Long,
                                                                 @PathVariable clientId: Long,
                                                                 @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                                                 @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()

            if (!accessService.canReadEntries(institutionId)) {
                throw ServicePermissionDeniedException()
            }

            if (employeeId > 0 &&
                    accessService.getId() != employeeId &&
                    !accessService.isAdmin() &&
                    !accessService.canReadEmployee(employeeId))
                throw ServicePermissionDeniedException()

            val readableInstitutions = accessService.getReadRightsInstitutionIds();
            val dtos = serviceService.getProjections(institutionId, employeeId, clientId, start, end, readableInstitutions)

            if (logPerformance) {
                logger.info(String.format("%s getByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: ServicePermissionDeniedException) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.FORBIDDEN
            )
        } catch (ex: Exception) {
            logger.error(ex.message, ex)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}")
    fun getByEmployeeAndFilter(@PathVariable id: Long,
                               @Valid @RequestBody valueDto: ServiceFilterDto): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (valueDto.date == null)
                throw IllegalArgumentException("No date")

            val dtos = serviceService.getDtosByEmployeeAndFilter(id, valueDto)

            if (logPerformance) {
                logger.info(String.format("%s getByEmployeeAndFilter took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                        dtos.size))
            }

            ResponseEntity.ok(dtos)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("times/{id}/{start}/{end}")
    fun getTimesByEmployee(@PathVariable id: Long,
                           @Valid @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                           @Valid @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val startMs = System.currentTimeMillis()
            if (accessService.getId() != id &&
                    !accessService.isAdmin() &&
                    !accessService.canReadEmployee(id))
                throw IllegalArgumentException("No permission to get the times of this employee")

            val serviceDtos = serviceService.getDtosByEmployeeAndStartEndDate(id, start, end)
            val dto = ServiceDtoConverter.convertServicesToServiceTimeDto(serviceDtos).apply {
                periodDays = start.until(end).days + 1
            }

            if (logPerformance) {
                logger.info(String.format("%s getTimesByEmployee took %s ms",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs))
            }

            ResponseEntity.ok(dto)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/employee/{id}")
    fun countByEmployee(@PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(serviceService.countByEmployee(id))
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/client/{id}")
    fun countByClient(@PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(serviceService.countByClient(id))
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/assistance_plan/{id}")
    fun countByAssistancePlan(@PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(serviceService.countByAssistancePlan(id))
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/goal/{id}")
    fun countByGoal(@PathVariable id: Long): Any {
        return try {
            ResponseEntity.ok(serviceService.countByGoal(id))
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("client-and-date")
    fun getByClientAndDate(@RequestBody request: ClientAndDateRequestDTO): ResponseEntity<Any> {
        return try {
            val startMs = System.currentTimeMillis()

            val result = serviceService.getFromTillEmployeeNameProjectionByClientAndDate(request.clientId, request.date)
            val response = ClientAndDateResponseDTO.of(request.clientId, result)

            if (logPerformance) {
                logger.info(String.format("%s getByClientAndDate took %s ms and found %d entities",
                        PerformanceLogbackFilter.PERFORMANCE_FILTER_STRING,
                        System.currentTimeMillis() - startMs,
                    result.size))
            }

            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            logger.error(ex.message)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }
}