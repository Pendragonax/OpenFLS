package de.vinz.openfls.domains.services.dto

import de.vinz.openfls.domains.categories.dto.CategoryResponse
import de.vinz.openfls.domains.goals.dto.GoalResponse
import de.vinz.openfls.domains.services.entity.Service
import java.time.LocalDateTime

data class ServiceWithGoalsAndCategoriesResponse(
    val id: Long,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val title: String,
    val content: String,
    val unfinished: Boolean,
    val groupService: Boolean,
    val archivedService: Boolean,
    val minutes: Int,
    val employeeId: Long,
    val clientId: Long,
    val institutionId: Long,
    val assistancePlanId: Long,
    val hourTypeId: Long,
    val goals: Set<GoalResponse>,
    val categorys: Set<CategoryResponse>
) {
    companion object {
        fun from(service: Service): ServiceWithGoalsAndCategoriesResponse = ServiceWithGoalsAndCategoriesResponse(
            id = service.id,
            start = service.start,
            end = service.end,
            title = service.title,
            content = service.content,
            unfinished = service.unfinished,
            groupService = service.groupService,
            archivedService = service.archivedService,
            minutes = service.minutes,
            employeeId = service.employee?.id ?: 0,
            clientId = service.client?.id ?: 0,
            institutionId = service.institution?.id ?: 0,
            assistancePlanId = service.assistancePlan?.id ?: 0,
            hourTypeId = service.hourType?.id ?: 0,
            goals = service.goals.map { GoalResponse.from(it) }.toSet(),
            categorys = service.categorys.map { CategoryResponse.from(it) }.toSet()
        )
    }
}
