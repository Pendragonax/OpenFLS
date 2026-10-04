package de.vinz.openfls.domains.sponsors.dto

sealed class SponsorDeleteResult {
    data class Success(val response: SponsorWithUnprofessionalsResponse) : SponsorDeleteResult()
    data object NotFound : SponsorDeleteResult()
}
