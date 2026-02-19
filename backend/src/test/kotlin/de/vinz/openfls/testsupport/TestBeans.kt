package de.vinz.openfls.testsupport

import de.vinz.openfls.domains.assistancePlans.AssistancePlan
import de.vinz.openfls.domains.assistancePlans.AssistancePlanHour
import de.vinz.openfls.domains.assistancePlans.dtos.AssistancePlanHourDto
import de.vinz.openfls.domains.hourTypes.HourType
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
        addConverter(object : AbstractConverter<AssistancePlanHourDto, AssistancePlanHour>() {
            override fun convert(source: AssistancePlanHourDto): AssistancePlanHour {
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
