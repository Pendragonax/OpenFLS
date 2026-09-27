package de.vinz.openfls.domains.sponsors.dto

sealed class SponsorUpdateResult {
    data class Success(val response: SponsorResponse) : SponsorUpdateResult()
    data object NotFound : SponsorUpdateResult()
}
