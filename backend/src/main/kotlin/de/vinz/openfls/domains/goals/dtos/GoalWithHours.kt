package de.vinz.openfls.domains.goals.dtos

class GoalWithHours {
    var id: Long = 0

    var title: String = ""

    var description: String = ""

    var assistancePlanId: Long = 0

    var institutionId: Long? = null

    var hours: MutableSet<GoalHourDto> = mutableSetOf()
}
