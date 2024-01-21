package de.vinz.openfls.dtos

class GoalResponseDto {

    var id: Long = 0

    var title: String = ""

    var description: String = ""

    var assistancePlanId: Long = 0

    var institutionId: Long? = null

    var institutionName: String? = null

    var hours: MutableSet<GoalHourResponseDto> = mutableSetOf()
}