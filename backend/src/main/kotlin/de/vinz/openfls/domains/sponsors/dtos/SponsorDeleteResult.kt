package de.vinz.openfls.domains.sponsors.dtos

sealed class SponsorDeleteResult {
    data class Success(val response: SponsorWithUnprofessionalsResponse) : SponsorDeleteResult()
    data object NotFound : SponsorDeleteResult()
}
