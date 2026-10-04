package de.vinz.openfls.domains.hourCorridors.repository

import de.vinz.openfls.domains.hourCorridors.entity.HourCorridor
import de.vinz.openfls.domains.hourCorridors.projection.HourCorridorAssistancePlanProjection
import de.vinz.openfls.domains.hourCorridors.projection.HourCorridorUsageProjection
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface HourCorridorRepository : CrudRepository<HourCorridor, Long> {

    @Query("SELECT COUNT(ap) FROM HourCorridor hc JOIN hc.assistancePlans ap WHERE hc.id = :hourCorridorId")
    fun countAssistancePlansByHourCorridorId(@Param("hourCorridorId") hourCorridorId: Long): Long

    @Query(
        """
        SELECT hc.id as hourCorridorId,
               COUNT(ap) as assistancePlanCount
        FROM HourCorridor hc
        JOIN hc.assistancePlans ap
        WHERE hc.id IN :hourCorridorIds
        GROUP BY hc.id
        """
    )
    fun countAssistancePlansByHourCorridorIds(
        @Param("hourCorridorIds") hourCorridorIds: List<Long>
    ): List<HourCorridorUsageProjection>

    @Query(
        """
        SELECT ap.id as id, ap.start as start, ap.end as end,
               c.firstName as clientFirstName, c.lastName as clientLastName
        FROM HourCorridor hc
        JOIN hc.assistancePlans ap
        JOIN ap.client c
        WHERE hc.id = :hourCorridorId
        ORDER BY ap.start DESC, ap.id DESC
        """
    )
    fun findAssistancePlansByHourCorridorId(
        @Param("hourCorridorId") hourCorridorId: Long
    ): List<HourCorridorAssistancePlanProjection>
}
