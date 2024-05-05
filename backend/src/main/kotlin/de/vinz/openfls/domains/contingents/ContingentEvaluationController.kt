package de.vinz.openfls.domains.contingents

import de.vinz.openfls.domains.contingents.services.ContingentEvaluationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/contingents/evaluations")
class ContingentEvaluationController(
        private val contingentEvaluationService: ContingentEvaluationService
) {

    @GetMapping("institution/{institutionId}/{year}")
    fun getByInstitution(@PathVariable institutionId: Long, @PathVariable year: Int): Any {
        val contingentEvaluation =
                contingentEvaluationService.getContingentEvaluationByYearAndInstitution(year, institutionId)
        return ResponseEntity.ok(contingentEvaluation)
    }
}