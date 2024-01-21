package de.vinz.openfls.domains.evaluations.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.entities.Employee
import de.vinz.openfls.entities.Goal
import org.jetbrains.annotations.NotNull
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "evaluations")
class Evaluation (
        @Id
        @GeneratedValue(strategy= GenerationType.IDENTITY)
        var id: Long = 0,

        @field:NotNull
        var date: LocalDate = LocalDate.now(),

        @Column(length = 1024)
        var content: String = "",

        var approved: Boolean = false,

        @field:NotNull
        var createdAt: LocalDateTime = LocalDateTime.now(),

        @JsonIgnore
        @ManyToOne(
                cascade = [CascadeType.REFRESH],
                fetch = FetchType.LAZY)
        @JoinColumn(name = "createdEvaluationsId")
        var createdBy: Employee,

        @field:NotNull
        var updatedAt: LocalDateTime = LocalDateTime.now(),

        @JsonIgnore
        @ManyToOne(
                cascade = [CascadeType.REFRESH],
                fetch = FetchType.LAZY)
        @JoinColumn(name = "updatedEvaluationsId")
        var updatedBy: Employee,

        @JsonIgnore
        @ManyToOne(
                cascade = [CascadeType.REFRESH],
                fetch = FetchType.LAZY)
        @JoinColumn(name = "goal_id")
        var goal: Goal
)