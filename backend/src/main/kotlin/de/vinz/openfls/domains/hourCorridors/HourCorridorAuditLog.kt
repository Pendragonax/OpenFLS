package de.vinz.openfls.domains.hourCorridors

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "hour_corridor_audit_logs")
class HourCorridorAuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "hour_corridor_id", nullable = false)
    var hourCorridorId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    var action: HourCorridorAuditAction = HourCorridorAuditAction.CREATE,

    @Column(name = "changed_at", nullable = false)
    var changedAt: LocalDateTime = LocalDateTime.MIN,

    @Column(nullable = false, length = 128)
    var actor: String = "system",

    @Column(name = "before_title", length = 255)
    var beforeTitle: String? = null,

    @Column(name = "after_title", length = 255)
    var afterTitle: String? = null,

    var beforeWeeklyMinutesFrom: Int? = null,
    var afterWeeklyMinutesFrom: Int? = null,
    var beforeWeeklyMinutesTill: Int? = null,
    var afterWeeklyMinutesTill: Int? = null,
    var beforeHourTypeId: Long? = null,
    var afterHourTypeId: Long? = null
)
