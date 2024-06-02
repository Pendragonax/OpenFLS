package de.vinz.openfls.domains.evaluations.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.entities.Employee
import de.vinz.openfls.entities.Goal
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "evaluations")
class Evaluation(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0,

        @Column(nullable = false)
        var date: LocalDate = LocalDate.now(),

        @Column(length = 1024)
        var content: String = "",

        var approved: Boolean = false,

        @Column(nullable = false)
        var createdAt: LocalDateTime = LocalDateTime.now(),

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.REFRESH], fetch = FetchType.LAZY)
        @JoinColumn(name = "createdEvaluationsId")
        var createdBy: Employee? = null,

        @Column(nullable = false)
        var updatedAt: LocalDateTime = LocalDateTime.now(),

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.REFRESH], fetch = FetchType.LAZY)
        @JoinColumn(name = "updatedEvaluationsId")
        var updatedBy: Employee? = null,

        @JsonIgnore
        @ManyToOne(cascade = [CascadeType.REFRESH], fetch = FetchType.LAZY)
        @JoinColumn(name = "goal_id")
        var goal: Goal? = null
)