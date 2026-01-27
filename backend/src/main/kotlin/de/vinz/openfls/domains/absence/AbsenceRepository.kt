package de.vinz.openfls.domains.absence

import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class AbsenceRepository {

    private val db = HashMap<Long, Absence>()
    private val random = java.util.Random()

    fun save(absence: Absence): Absence {
        if (db.entries.any { it.value.employeeId == absence.employeeId && it.value.absenceDate == absence.absenceDate }) {
            return db.entries.first { it.value.employeeId == absence.employeeId && it.value.absenceDate == absence.absenceDate }.value
        }

        var newId = random.nextLong()
        while(db.containsKey(newId) || newId == 0L) {
            newId = random.nextLong()
        }
        absence.id = newId
        db[absence.id] = absence
        return absence
    }

    fun remove(id: Long) {
        if (!db.containsKey(id)) {
            return
        }

        db.remove(id)
    }

    fun findByEmployeeIdAndAbsenceDate(employeeId: Long, absenceDate: LocalDate): Absence? {
        return db.entries.firstOrNull { it.value.employeeId == employeeId && it.value.absenceDate == absenceDate }?.value
    }

    fun findAllByEmployeeId(employeeId: Long): List<Absence> {
        return db.entries.filter { it.value.employeeId == employeeId }.map { it.value }
    }

}