package de.vinz.openfls.domains.sponsors.dtos

sealed class SponsorUpdateResult {
    data class Success(val response: SponsorResponse) : SponsorUpdateResult()
    data object NotFound : SponsorUpdateResult()
}
