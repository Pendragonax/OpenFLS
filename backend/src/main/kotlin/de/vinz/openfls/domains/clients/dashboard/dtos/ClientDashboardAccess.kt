package de.vinz.openfls.domains.clients.dashboard.dtos

/**
 * Visibility state of a single dashboard section. `DENIED` is returned instead of
 * silently empty data so the frontend can tell the user that the section exists
 * but is not readable with their permissions.
 */
enum class ClientDashboardAccess {
    GRANTED,
    DENIED
}
