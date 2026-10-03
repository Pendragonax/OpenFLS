package de.vinz.openfls.domains.services.service

import de.vinz.openfls.architecture.InternalEntityApi
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.service.AssistancePlanService
import de.vinz.openfls.domains.categories.entity.Category
import de.vinz.openfls.domains.categories.service.CategoryTemplateService
import de.vinz.openfls.domains.clients.entity.Client
import de.vinz.openfls.domains.clients.service.ClientService
import de.vinz.openfls.domains.employees.service.EmployeeService
import de.vinz.openfls.domains.goals.entity.Goal
import de.vinz.openfls.domains.goals.service.GoalService
import de.vinz.openfls.domains.hourTypes.entity.HourType
import de.vinz.openfls.domains.hourTypes.service.HourTypeService
import de.vinz.openfls.domains.institutions.entity.Institution
import de.vinz.openfls.domains.institutions.service.InstitutionService
import de.vinz.openfls.domains.permissions.service.AccessService
import de.vinz.openfls.domains.permissions.service.PermissionService
import de.vinz.openfls.domains.services.dto.AssistancePlanServiceMinutesDto
import de.vinz.openfls.domains.services.dto.ClientLatestServiceResponse
import de.vinz.openfls.domains.services.dto.ClientServicesByDateResponse
import de.vinz.openfls.domains.services.dto.ContingentEvaluationServiceDto
import de.vinz.openfls.domains.services.dto.IdReferenceRequest
import de.vinz.openfls.domains.services.dto.ServiceCalendarDto
import de.vinz.openfls.domains.services.dto.ServiceCreateRequest
import de.vinz.openfls.domains.services.dto.ServiceCreateResult
import de.vinz.openfls.domains.services.dto.ServiceDeleteResult
import de.vinz.openfls.domains.services.dto.ServiceDto
import de.vinz.openfls.domains.services.dto.ServiceGetResult
import de.vinz.openfls.domains.services.dto.ServiceListItemResponse
import de.vinz.openfls.domains.services.dto.ServiceUpdateRequest
import de.vinz.openfls.domains.services.dto.ServiceUpdateResult
import de.vinz.openfls.domains.services.dto.ServiceWithGoalsAndCategoriesResponse
import de.vinz.openfls.domains.services.entity.Service
import de.vinz.openfls.domains.services.repository.ServiceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@org.springframework.stereotype.Service
class ServiceService(
    private val serviceRepository: ServiceRepository,
    private val clientService: ClientService,
    private val assistancePlanService: AssistancePlanService,
    private val employeeService: EmployeeService,
    private val hourTypeService: HourTypeService,
    private val institutionService: InstitutionService,
    private val goalService: GoalService,
    private val categoryTemplateService: CategoryTemplateService,
    private val accessService: AccessService,
    private val permissionService: PermissionService
) {

    private enum class MissingReference { CLIENT, ASSISTANCE_PLAN, HOUR_TYPE, INSTITUTION, GOAL, CATEGORY }

    private class ServiceReferences(
        val client: Client,
        val assistancePlan: AssistancePlan,
        val hourType: HourType,
        val institution: Institution,
        val goals: MutableSet<Goal>,
        val categories: MutableSet<Category>
    )

    private sealed interface ReferenceLookup {
        class Found(val references: ServiceReferences) : ReferenceLookup
        class Missing(val reference: MissingReference) : ReferenceLookup
    }

    @Transactional
    fun create(request: ServiceCreateRequest): ServiceCreateResult {
        if (!accessService.canWriteEntries(request.institutionId)) {
            return ServiceCreateResult.Forbidden
        }

        val references = when (val lookup = resolveReferences(
            request.clientId, request.institutionId, request.assistancePlanId, request.hourTypeId,
            request.goals, request.categorys
        )) {
            is ReferenceLookup.Missing -> return lookup.reference.toCreateResult()
            is ReferenceLookup.Found -> lookup.references
        }

        if (isClientArchived(references.client, references.assistancePlan)) {
            return ServiceCreateResult.ClientArchived
        }
        if (request.start >= request.end) {
            return ServiceCreateResult.InvalidTimeRange
        }

        val saved = serviceRepository.save(
            Service(
                start = request.start,
                end = request.end,
                minutes = minutesBetween(request.start, request.end),
                title = request.title,
                content = request.content,
                groupService = request.groupService,
                unfinished = request.unfinished,
                client = references.client,
                employee = employeeService.getEntityById(accessService.getId())
                    ?: throw IllegalStateException("current user not found"),
                institution = references.institution,
                hourType = references.hourType,
                assistancePlan = references.assistancePlan,
                goals = references.goals,
                categorys = references.categories
            )
        )

        return ServiceCreateResult.Success(ServiceWithGoalsAndCategoriesResponse.from(saved))
    }

    @Transactional
    fun update(id: Long, request: ServiceUpdateRequest): ServiceUpdateResult {
        if (!accessService.canWriteEntries(request.institutionId)) {
            return ServiceUpdateResult.Forbidden
        }

        val existing = serviceRepository.findByIdOrNull(id)
            ?: return ServiceUpdateResult.NotFound
        if (!isAdminOrOwner(existing)) {
            return ServiceUpdateResult.Forbidden
        }

        val references = when (val lookup = resolveReferences(
            request.clientId, request.institutionId, request.assistancePlanId, request.hourTypeId,
            request.goals, request.categorys
        )) {
            is ReferenceLookup.Missing -> return lookup.reference.toUpdateResult()
            is ReferenceLookup.Found -> lookup.references
        }

        if (isClientArchived(existing.client, existing.assistancePlan) ||
            isClientArchived(references.client, references.assistancePlan)
        ) {
            return ServiceUpdateResult.ClientArchived
        }
        if (request.start >= request.end) {
            return ServiceUpdateResult.InvalidTimeRange
        }

        existing.apply {
            start = request.start
            end = request.end
            minutes = minutesBetween(request.start, request.end)
            title = request.title
            content = request.content
            groupService = request.groupService
            unfinished = request.unfinished
            client = references.client
            institution = references.institution
            hourType = references.hourType
            assistancePlan = references.assistancePlan
            goals.clear()
            goals.addAll(references.goals)
            categorys.clear()
            categorys.addAll(references.categories)
        }

        return ServiceUpdateResult.Success(ServiceWithGoalsAndCategoriesResponse.from(serviceRepository.save(existing)))
    }

    @Transactional
    fun delete(id: Long): ServiceDeleteResult {
        val existing = serviceRepository.findByIdOrNull(id)
            ?: return ServiceDeleteResult.NotFound
        if (!isAdminOrOwner(existing)) {
            return ServiceDeleteResult.Forbidden
        }
        if (isClientArchived(existing.client, existing.assistancePlan)) {
            return ServiceDeleteResult.ClientArchived
        }

        val response = ServiceWithGoalsAndCategoriesResponse.from(existing)
        serviceRepository.delete(existing)

        return ServiceDeleteResult.Success(response)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): ServiceGetResult {
        val entity = (if (id > 0) serviceRepository.findByIdOrNull(id) else null)
            ?: return ServiceGetResult.NotFound

        if (!accessService.isAdmin() && !accessService.canReadEntries(entity.institution?.id ?: 0)) {
            return ServiceGetResult.Forbidden
        }

        return ServiceGetResult.Success(ServiceWithGoalsAndCategoriesResponse.from(entity))
    }

    @Transactional(readOnly = true)
    fun getServicesByAssistancePlanId(assistancePlanId: Long): List<ServiceWithGoalsAndCategoriesResponse>? {
        if (assistancePlanId <= 0 || !assistancePlanService.existsById(assistancePlanId)) {
            return null
        }

        return serviceRepository.findAllByAssistancePlanId(assistancePlanId)
            .map { ServiceWithGoalsAndCategoriesResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getServicesOutsideAssistancePlanPeriodByEmployeeId(employeeId: Long): List<ServiceListItemResponse> {
        return serviceRepository.findOutsideAssistancePlanPeriodByEmployeeId(employeeId).map(ServiceListItemResponse::from)
    }

    @Transactional(readOnly = true)
    fun getServicesOutsideAssistancePlanPeriodByInstitutionId(institutionId: Long): List<ServiceListItemResponse> {
        return serviceRepository.findOutsideAssistancePlanPeriodByInstitutionId(institutionId).map(ServiceListItemResponse::from)
    }

    @Transactional(readOnly = true)
    fun getServicesByEmployeeIdAndStartAndEnd(
        employeeId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<ServiceListItemResponse> {
        val userId = accessService.getId()
        val isAdmin = accessService.isAdmin()
        val leadingInstitutionIds = permissionService.getLeadingInstitutionIdsByEmployee(userId)

        return serviceRepository.findByEmployeeAndStartAndEnd(employeeId, start, end)
            .map(ServiceListItemResponse::from)
            .filter { isAdmin || it.employee.id == userId || leadingInstitutionIds.contains(it.institution.id) }
            .sortedBy { it.start }
    }

    @Transactional(readOnly = true)
    fun getServicesByInstitutionIdAndEmployeeIdAndClientIdAndStartAndEnd(
        institutionId: Long,
        employeeId: Long,
        clientId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<ServiceListItemResponse> {
        return serviceRepository.findWithRelationsByFilter(
            institutionId,
            accessService.getReadRightsInstitutionIds(),
            employeeId,
            clientId,
            start,
            end
        ).map(ServiceListItemResponse::from)
    }

    @Transactional(readOnly = true)
    fun getClientServicesByDate(clientId: Long, date: LocalDate): ClientServicesByDateResponse {
        val services = serviceRepository
            .findServiceTimeSlotProjectionByClientIdAndStartIsBetween(clientId, date)

        return ClientServicesByDateResponse(
            clientId = clientId,
            services = services.map { service ->
                ClientServicesByDateResponse.ClientServicesByDateEntry(
                    id = service.id,
                    timepoint = "${service.start.toLocalTime()} - ${service.end.toLocalTime()}",
                    employeeName = "${service.employeeFirstname.first()}. ${service.employeeLastname}"
                )
            }
        )
    }

    @Transactional(readOnly = true)
    fun countServicesByEmployeeId(employeeId: Long): Long {
        return serviceRepository.countByEmployeeId(employeeId)
    }

    @Transactional(readOnly = true)
    fun countServicesByClientId(clientId: Long): Long {
        return serviceRepository.countByClientId(clientId)
    }

    @Transactional(readOnly = true)
    fun countServicesByAssistancePlanId(assistancePlanId: Long): Long {
        return serviceRepository.countByAssistancePlanId(assistancePlanId)
    }

    /**
     * Latest entries of a client, already restricted to what the requesting employee
     * may read: own entries always, foreign entries only for readable institutions.
     */
    @Transactional(readOnly = true)
    fun getLatestServicesByClientId(
        clientId: Long,
        employeeId: Long,
        readableInstitutionIds: List<Long>,
        isAdmin: Boolean,
        limit: Int
    ): List<ClientLatestServiceResponse> {
        if (limit <= 0) {
            return emptyList()
        }
        return serviceRepository.findLatestByClientId(
            clientId = clientId,
            employeeId = employeeId,
            readableInstitutionIds = readableInstitutionIds.ifEmpty { listOf(-1L) },
            isAdmin = isAdmin,
            pageable = PageRequest.of(0, limit)
        )
    }

    @Transactional(readOnly = true)
    fun getContingentEvaluationServicesByInstitutionIdAndYear(
        institutionId: Long,
        year: Int
    ): List<ContingentEvaluationServiceDto> {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        return serviceRepository
            .findContingentEvaluationServicesByInstitutionIdAndStartAndEnd(institutionId, start, end)
            .map(ContingentEvaluationServiceDto::from)
    }

    @Transactional(readOnly = true)
    fun getCalendarServicesByEmployeeIdAndStartAndEnd(
        employeeId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<ServiceCalendarDto> {
        return serviceRepository.findServiceCalendarProjection(employeeId, start, end)
            .map(ServiceCalendarDto::from)
    }

    @Transactional(readOnly = true)
    fun getMinutesInPlanPeriodByAssistancePlanIdsAndStartAndEnd(
        assistancePlanIds: List<Long>,
        start: LocalDate,
        end: LocalDate
    ): List<AssistancePlanServiceMinutesDto> {
        return serviceRepository.findMinutesInPlanPeriodByAssistancePlanIdsAndStartAndEnd(assistancePlanIds, start, end)
            .map(AssistancePlanServiceMinutesDto::from)
    }

    @Transactional(readOnly = true)
    fun getMinutesInPlanPeriodByAssistancePlanIdsUntil(
        assistancePlanIds: List<Long>,
        until: LocalDate
    ): List<AssistancePlanServiceMinutesDto> {
        return serviceRepository.findMinutesInPlanPeriodByAssistancePlanIdsUntil(assistancePlanIds, until)
            .map(AssistancePlanServiceMinutesDto::from)
    }

    @Transactional(readOnly = true)
    fun getServicesByAssistancePlanIdAndHourTypeIdAndYearAndMonth(
        assistancePlanId: Long,
        hourTypeId: Long,
        year: Int,
        month: Int
    ): List<ServiceDto> {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        return getServicesByAssistancePlanIdAndHourTypeIdAndStartAndEnd(assistancePlanId, hourTypeId, start, end)
    }

    @Transactional(readOnly = true)
    fun getServicesByAssistancePlanIdAndHourTypeIdAndYear(
        assistancePlanId: Long,
        hourTypeId: Long,
        year: Int
    ): List<ServiceDto> {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        return getServicesByAssistancePlanIdAndHourTypeIdAndStartAndEnd(assistancePlanId, hourTypeId, start, end)
    }

    @Transactional(readOnly = true)
    fun getServicesByAssistancePlanIdAndHourTypeIdAndStartAndEnd(
        assistancePlanId: Long,
        hourTypeId: Long,
        start: LocalDate,
        end: LocalDate
    ): List<ServiceDto> {
        return serviceRepository.findByAssistancePlanIdAndHourTypeIdAndStartAndEnd(
            assistancePlanId, hourTypeId, start, end
        ).map(ServiceDto::from)
    }

    @Transactional(readOnly = true)
    fun getServicesByAssistancePlanIdAndYearAndMonth(
        assistancePlanId: Long,
        year: Int,
        month: Int
    ): List<ServiceDto> {
        val start = LocalDate.of(year, month, 1)
        val end = start.plusMonths(1).minusDays(1)
        return serviceRepository.findByAssistancePlanIdAndStartAndEnd(assistancePlanId, start, end)
            .map(ServiceDto::from)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByAssistancePlanId(assistancePlanId: Long): List<Service> {
        return serviceRepository.findAllByAssistancePlanId(assistancePlanId)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByClientId(clientId: Long): List<Service> {
        return serviceRepository.findByClientIdOrderByStartAsc(clientId)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByAssistancePlanIdAndStartBetween(
        assistancePlanId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Service> {
        return serviceRepository.findServicesByAssistancePlanIdAndStartIsBetween(assistancePlanId, start, end)
    }

    @InternalEntityApi
    @Transactional(readOnly = true)
    fun getAllEntitiesByYearAndMonthAndHourTypeIdAndInstitutionIdAndSponsorId(
        year: Int,
        month: Int?,
        hourTypeId: Long,
        institutionId: Long?,
        sponsorId: Long?
    ): List<Service> {
        return when {
            institutionId != null && sponsorId != null && month != null ->
                serviceRepository.findAllByYearAndMonthAndHourTypeIdAndInstitutionIdAndSponsorId(
                    year = year, month = month, hourTypeId = hourTypeId, institutionId = institutionId, sponsorId = sponsorId
                )

            institutionId != null && sponsorId != null ->
                serviceRepository.findAllByYearAndHourTypeIdAndInstitutionIdAndSponsorId(
                    year = year, hourTypeId = hourTypeId, institutionId = institutionId, sponsorId = sponsorId
                )

            institutionId != null && month != null ->
                serviceRepository.findAllByYearAndMonthAndHourTypeIdAndInstitutionId(
                    year = year, month = month, hourTypeId = hourTypeId, institutionId = institutionId
                )

            sponsorId != null && month != null ->
                serviceRepository.findAllByYearAndMonthAndHourTypeIdAndSponsorId(
                    year = year, month = month, hourTypeId = hourTypeId, sponsorId = sponsorId
                )

            institutionId != null ->
                serviceRepository.findAllByYearAndHourTypeIdAndInstitutionId(
                    year = year, hourTypeId = hourTypeId, institutionId = institutionId
                )

            sponsorId != null ->
                serviceRepository.findAllByYearAndHourTypeIdAndSponsorId(
                    year = year, hourTypeId = hourTypeId, sponsorId = sponsorId
                )

            month != null ->
                serviceRepository.findAllByYearAndMonthAndHourTypeId(
                    year = year, month = month, hourTypeId = hourTypeId
                )

            else ->
                serviceRepository.findAllByYearAndHourTypeId(year = year, hourTypeId = hourTypeId)
        }
    }

    private fun resolveReferences(
        clientId: Long,
        institutionId: Long,
        assistancePlanId: Long,
        hourTypeId: Long,
        goalReferences: List<IdReferenceRequest>,
        categoryReferences: List<IdReferenceRequest>
    ): ReferenceLookup {
        val client = clientService.getEntityById(clientId)
            ?: return ReferenceLookup.Missing(MissingReference.CLIENT)
        val assistancePlan = assistancePlanService.getEntityById(assistancePlanId)
            ?: return ReferenceLookup.Missing(MissingReference.ASSISTANCE_PLAN)
        val hourType = hourTypeService.getEntityById(hourTypeId)
            ?: return ReferenceLookup.Missing(MissingReference.HOUR_TYPE)
        val institution = institutionService.getEntityById(institutionId)
            ?: return ReferenceLookup.Missing(MissingReference.INSTITUTION)

        val goals = goalReferences.map { it.id }.distinct()
            .map { goalService.getEntityById(it) ?: return ReferenceLookup.Missing(MissingReference.GOAL) }
            .toMutableSet()

        val categoryIds = categoryReferences.map { it.id }.distinct()
        val categories = categoryTemplateService.getAllCategoryEntitiesByIds(categoryIds)
        if (categories.size != categoryIds.size) {
            return ReferenceLookup.Missing(MissingReference.CATEGORY)
        }

        return ReferenceLookup.Found(
            ServiceReferences(client, assistancePlan, hourType, institution, goals, categories.toMutableSet())
        )
    }

    private fun MissingReference.toCreateResult(): ServiceCreateResult = when (this) {
        MissingReference.CLIENT -> ServiceCreateResult.ClientNotFound
        MissingReference.ASSISTANCE_PLAN -> ServiceCreateResult.AssistancePlanNotFound
        MissingReference.HOUR_TYPE -> ServiceCreateResult.HourTypeNotFound
        MissingReference.INSTITUTION -> ServiceCreateResult.InstitutionNotFound
        MissingReference.GOAL -> ServiceCreateResult.GoalNotFound
        MissingReference.CATEGORY -> ServiceCreateResult.CategoryNotFound
    }

    private fun MissingReference.toUpdateResult(): ServiceUpdateResult = when (this) {
        MissingReference.CLIENT -> ServiceUpdateResult.ClientNotFound
        MissingReference.ASSISTANCE_PLAN -> ServiceUpdateResult.AssistancePlanNotFound
        MissingReference.HOUR_TYPE -> ServiceUpdateResult.HourTypeNotFound
        MissingReference.INSTITUTION -> ServiceUpdateResult.InstitutionNotFound
        MissingReference.GOAL -> ServiceUpdateResult.GoalNotFound
        MissingReference.CATEGORY -> ServiceUpdateResult.CategoryNotFound
    }

    private fun isAdminOrOwner(service: Service): Boolean {
        return accessService.isAdmin() || service.employee?.id == accessService.getId()
    }

    private fun isClientArchived(client: Client?, assistancePlan: AssistancePlan?): Boolean {
        return client?.archived == true || assistancePlan?.client?.archived == true
    }

    private fun minutesBetween(start: LocalDateTime, end: LocalDateTime): Int {
        return Duration.between(start, end).toMinutes().toInt()
    }
}
