package de.vinz.openfls.testsupport

import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.entity.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.dto.AssistancePlanHourResponse
import de.vinz.openfls.domains.hourTypes.entity.HourType
import org.modelmapper.AbstractConverter
import org.modelmapper.ModelMapper
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@TestConfiguration
class TestBeans {
    @Bean
    fun modelMapper(): ModelMapper = ModelMapper().apply {
        addConverter(object : AbstractConverter<AssistancePlanHourResponse, AssistancePlanHour>() {
            override fun convert(source: AssistancePlanHourResponse): AssistancePlanHour {
                return AssistancePlanHour().apply {
                    id = source.id
                    weeklyMinutes = source.weeklyMinutes
                    hourType = HourType(id = source.hourTypeId)
                    assistancePlan = AssistancePlan(id = source.assistancePlanId)
                }
            }
        })
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
