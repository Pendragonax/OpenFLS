package de.vinz.openfls.controller

import de.vinz.openfls.dtos.ServiceDto
import de.vinz.openfls.dtos.ServiceFilterDto
import de.vinz.openfls.dtos.ServiceXLDto
import de.vinz.openfls.model.Service
import de.vinz.openfls.services.*
import org.modelmapper.ModelMapper
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.validation.Valid

@RestController
@RequestMapping("/services")
class ServiceController(
    private val serviceService: ServiceService,
    private val employeeService: EmployeeService,
    private val accessService: AccessService,
    private val permissionService: PermissionService,
    private val assistancePlanService: AssistancePlanService,
    private val modelMapper: ModelMapper,
    private val helperService: HelperService,
    private val converter: ConverterService
) {
    @PostMapping
    fun create(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @Valid @RequestBody valueDto: ServiceDto): Any {
        return try {
            if (!accessService.canWriteEntries(token, valueDto.institutionId))
                throw IllegalArgumentException("No permission to write entries to this institution")

            val entity = modelMapper.map(valueDto, Service::class.java)

            entity.employee.unprofessionals = null
            val savedEntity = serviceService.create(entity)

            helperService.printLog(this::class.simpleName, "create [id=${savedEntity.id}]", false)

            ResponseEntity.ok(modelMapper.map(savedEntity, ServiceDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "create - ${ex.message}", true)

            ResponseEntity(
                employeeService.getById(valueDto.employeeId),
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @PutMapping("{id}")
    fun update(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long,
               @Valid @RequestBody valueDto: ServiceDto): Any {
        return try {
            if (!accessService.canWriteEntries(token, valueDto.institutionId))
                throw IllegalArgumentException("No permission to update this service")
            if (!serviceService.existsById(id))
                throw IllegalArgumentException("service not found")
            if (!accessService.isAdmin(token) &&
                serviceService.getById(id)?.employee?.id != accessService.getId(token))
                throw IllegalArgumentException("Your not the owner of this service or the admin")
            if (serviceService.getById(id)
                ?.start
                ?.toLocalDate()
                ?.isBefore(LocalDate.now().minusDays(14)) == true) {
                throw IllegalArgumentException("14 days edit period is over")
            }

            val entity = modelMapper.map(valueDto, Service::class.java)

            val savedEntity = serviceService.update(entity)

            helperService.printLog(this::class.simpleName, "update [id=${savedEntity.id}]", false)

            ResponseEntity.ok(modelMapper.map(savedEntity, ServiceDto::class.java))
        } catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "update - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @DeleteMapping("{id}")
    fun delete(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
               @PathVariable id: Long): Any {
        return try {
            val service = serviceService.getById(id);

            println((service?.employee?.id == accessService.getId(token)))
            println("start - ${service?.start} | end - ${LocalDate.now().minusDays(14)}")
            println(service?.start?.toLocalDate()?.isAfter(LocalDate.now().minusDays(14)))

            if (!accessService.isAdmin(token) &&
                (service?.employee?.id != accessService.getId(token) ||
                        service.start.toLocalDate().isBefore(LocalDate.now().minusDays(14))))
                throw IllegalArgumentException("No permission to delete this service")
            if (!serviceService.existsById(id))
                throw IllegalArgumentException("service not found")

            val entity = serviceService.getById(id)

            serviceService.delete(id)

            helperService.printLog(this::class.simpleName, "delete [id=${id}]", false)

            ResponseEntity.ok(entity)
        }catch (ex: Exception) {
            helperService.printLog(this::class.simpleName, "delete - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping
    fun getAll(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String): Any {
        return try {
            if (!accessService.isAdmin(token))
                throw IllegalArgumentException("No permission to get all services")

            val dtos = serviceService.getAll()
                .map { modelMapper.map(it, ServiceDto::class.java) }

            helperService.printLog(this::class.simpleName, "getAll", false)

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "getAll - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("{id}")
    fun getById(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                @PathVariable id: Long): Any {
        return try {
            if (id <= 0)
                throw IllegalArgumentException("id is <= 0")
            if (!serviceService.existsById(id))
                throw IllegalArgumentException("service not found")

            val dto = modelMapper.map(serviceService.getById(id), ServiceDto::class.java)

            if (!accessService.isAdmin(token) &&
                !accessService.canReadEntries(token, dto.institutionId))
                throw IllegalArgumentException("Your not the allowed to read this entry")

            helperService.printLog(this::class.simpleName, "getById", false)

            ResponseEntity.ok(dto)
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "getById - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("assistance_plan/{id}")
    fun getByAssistancePlan(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                            @PathVariable id: Long): Any {
        return try {
            if (id <= 0)
                throw IllegalArgumentException("id is <= 0")
            if (!assistancePlanService.existsById(id))
                throw IllegalArgumentException("assistance plan not found")
            if (!accessService.canModifyAssistancePlan(token, id))
                throw IllegalArgumentException("no permission to load the services of this assistance plan")

            val dtos = serviceService.getByAssistancePlan(id).map {
                modelMapper.map(it, ServiceXLDto::class.java)
            }

            helperService.printLog(this::class.simpleName, "getByAssistancePlan", false)

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "getByAssistancePlan - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}/{date}")
    fun getByEmployeeAndDate(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                             @PathVariable id: Long,
                             @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate): Any {
        return try {
            val leadingInstitutionIds = permissionService
                .getLeadingInstitutionIdsByEmployee(accessService.getId(token))
            val userId = accessService.getId(token)
            val isAdmin = accessService.isAdmin(token)

            val dtos = serviceService.getByEmployeeAndDate(id, date)
                .map { modelMapper.map(it, ServiceDto::class.java) }
                .filter { isAdmin || it.employeeId == userId || leadingInstitutionIds.contains(it.institutionId) }
                .sortedBy { it.start }

            helperService.printLog(this::class.simpleName, "getByEmployeeAndDate", false)

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "getByEmployeeAndDate - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}/{start}/{end}")
    fun getByEmployeeAndStartAndEnd(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                    @PathVariable id: Long,
                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val leadingInstitutionIds = permissionService
                    .getLeadingInstitutionIdsByEmployee(accessService.getId(token))
            val userId = accessService.getId(token)
            val isAdmin = accessService.isAdmin(token)

            val dtos = serviceService.getByEmployeeAndStartAndEnd(id, start, end)
                    .map { modelMapper.map(it, ServiceDto::class.java) }
                    .filter { isAdmin || it.employeeId == userId || leadingInstitutionIds.contains(it.institutionId) }
                    .sortedBy { it.start }

            helperService.printLog(this::class.simpleName, "getByEmployeeAndStartAndEnd", false)

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "getByEmployeeAndStartAndEnd - ${ex.message}", true)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("client/{id}/{date}")
    fun getByClientAndDate(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                             @PathVariable id: Long,
                             @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate): Any {
        return try {
            val affiliatedInstitutionIds = permissionService
                .getReadableInstitutionIdsByEmployee(accessService.getId(token))
            val userId = accessService.getId(token)
            val isAdmin = accessService.isAdmin(token)

            val dtos = serviceService.getByClientAndDate(id, date)
                .map { modelMapper.map(it, ServiceDto::class.java) }
                .filter { isAdmin || it.employeeId == userId || affiliatedInstitutionIds.contains(it.institutionId) }

            helperService.printLog(this::class.simpleName, "getByClientAndDate", false)

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "getByClientAndDate - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("client/{id}/{start}/{end}")
    fun getByClientAndStartAndEnd(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                                  @PathVariable id: Long,
                                  @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                                  @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            val affiliatedInstitutionIds = permissionService
                    .getReadableInstitutionIdsByEmployee(accessService.getId(token))
            val userId = accessService.getId(token)
            val isAdmin = accessService.isAdmin(token)

            val dtos = serviceService.getByClientAndStartAndEnd(id, start, end)
                    .map { modelMapper.map(it, ServiceDto::class.java) }
                    .filter { isAdmin || it.employeeId == userId || affiliatedInstitutionIds.contains(it.institutionId) }

            helperService.printLog(this::class.simpleName, "getByClientAndStartAndEnd", false)

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "getByClientAndStartAndEnd - ${ex.message}", true)

            ResponseEntity(
                    ex.message,
                    HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("employee/{id}")
    fun getByEmployeeAndFilter(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                               @PathVariable id: Long,
                               @Valid @RequestBody valueDto: ServiceFilterDto): Any {
        return try {
            if (valueDto.date == null)
                throw IllegalArgumentException("No date")

            val dtos = serviceService.getByEmployeeAndFilter(id, valueDto)
                .map { modelMapper.map(it, ServiceDto::class.java) }

            helperService.printLog(this::class.simpleName, "getByEmployeeAndFilter", false)

            ResponseEntity.ok(dtos)
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "getByEmployeeAndFilter - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("times/{id}/{start}/{end}")
    fun getTimesByEmployee(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                           @PathVariable id: Long,
                           @Valid @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") start: LocalDate,
                           @Valid @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") end: LocalDate): Any {
        return try {
            if (accessService.getId(token) != id &&
                !accessService.isAdmin(token) &&
                !accessService.canReadEmployeeStats(token, id))
                throw IllegalArgumentException("No permission to get the times of this employee")

            val entities = serviceService.getByEmployeeAndStartEndDate(id, start, end)
            val dto = this.converter.convertServiceDTOsToServiceTimeDto(entities).apply {
                periodDays = start.until(end).days + 1
            }

            helperService.printLog(this::class.simpleName, "getTimesByEmployee", false)

            ResponseEntity.ok(dto)
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "getTimesByEmployee - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/employee/{id}")
    fun countByEmployee(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                        @PathVariable id: Long): Any {
        return try {
            helperService.printLog(this::class.simpleName, "countByEmployee", false)
            ResponseEntity.ok(serviceService.countByEmployee(id))
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "countByEmployee - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/client/{id}")
    fun countByClient(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                      @PathVariable id: Long): Any {
        return try {
            helperService.printLog(this::class.simpleName, "countByClient", false)
            ResponseEntity.ok(serviceService.countByClient(id))
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "countByClient - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/assistance_plan/{id}")
    fun countByAssistancePlan(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                              @PathVariable id: Long): Any {
        return try {
            helperService.printLog(this::class.simpleName, "countByAssistancePlan", false)
            ResponseEntity.ok(serviceService.countByAssistancePlan(id))
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "countByAssistancePlan - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }

    @GetMapping("count/goal/{id}")
    fun countByGoal(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String,
                    @PathVariable id: Long): Any {
        return try {
            helperService.printLog(this::class.simpleName, "countByGoal", false)
            ResponseEntity.ok(serviceService.countByGoal(id))
        } catch(ex: Exception) {
            helperService.printLog(this::class.simpleName, "countByGoal - ${ex.message}", true)

            ResponseEntity(
                ex.message,
                HttpStatus.BAD_REQUEST
            )
        }
    }
}