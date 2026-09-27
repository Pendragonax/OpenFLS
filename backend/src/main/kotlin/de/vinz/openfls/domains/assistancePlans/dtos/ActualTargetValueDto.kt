package de.vinz.openfls.domains.assistancePlans.dtos

import de.vinz.openfls.domains.hourTypes.dto.HourTypeResponse

open class ActualTargetValueDto {
    var target: Double = 0.0

    var actual: Double = 0.0

    var size: Long = 0

    var hourType: HourTypeResponse = HourTypeResponse()
}