package de.vinz.openfls.domains.evaluations

import com.fasterxml.jackson.annotation.JsonIgnore
import de.vinz.openfls.domains.contingents.Contingent
import de.vinz.openfls.domains.employees.entities.Employee
import de.vinz.openfls.domains.goals.entities.Goal
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
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Evaluation) return false
                return id == other.id
        }

        override fun hashCode(): Int {
                return id.hashCode()
        }
}
