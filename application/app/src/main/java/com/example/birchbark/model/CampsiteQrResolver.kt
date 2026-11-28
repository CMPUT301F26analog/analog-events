package com.example.birchbark.model

/**
 * US 01.12.01
 *
 * Sealed result of validating and resolving a scanned campsite URI (that is, QR code).
 *
 * @Note See #[RW-15], #[RW-16], #[RW-17], #[RW-18] in References.md
 * @Note See #[RW+91] in LLM.md
 **/
sealed interface CampsiteQrResult {
    /** @property campsite the current campsite returned by the data source. */
    data class Found(val campsite: Campsite) : CampsiteQrResult
    /** The QR is not a supported campsite URI. */
    class Invalid : CampsiteQrResult
    /** The current server document does not exist. */
    class Missing : CampsiteQrResult
    /** @property error the underlying lookup failure, not shown directly to users. */
    data class Failed(val error: Exception) : CampsiteQrResult
}

/**
 * Narrow data-source interface for QR campsite lookup.
 * A production implementation must verify the current server document.
 */
fun interface CampsiteQrLookup {
    /**
     * @param campsiteId validated Firestore document ID.
     * @param onResult callback receiving one result on the caller's UI thread in production.
     */
    fun load(campsiteId: String, onResult: (CampsiteQrResult) -> Unit)
}

/**
 * Concrete resolver shared by in-app scans and external deep links.
 * @param lookup injected data source, independent of Firebase/Android.
 * @param codec instance used to interpret the QR payload.
 */
class CampsiteQrResolver(
    private val lookup: CampsiteQrLookup,
    private val codec: CampsiteQrCodec = CampsiteQrCodec()
) {
    /**
     * Validates the payload before touching the database.
     * @param data raw QR or URI text.
     * @param onResult receives the lookup outcome or an immediate invalid result.
     */
    fun resolve(data: String, onResult: (CampsiteQrResult) -> Unit) {
        val id = codec.decode(data)
        if (id == null) onResult(CampsiteQrResult.Invalid())
        else lookup.load(id, onResult)
    }
}

/** ENUM describing the allowed QR destination in the active role. */
enum class CampsiteQrDestination {
    ENTRANT_DETAILS, ADMIN_DETAILS, OWNER_NOTICE, DENIED
}

/**
 * Pure top-level function that maps the active role to a QR destination.
 *
 * @param activeRole role already selected and authorized by application navigation.
 * @return destination permitted for that role, never an automatic role switch.
 */
fun campsiteQrDestinationForRole(activeRole: String): CampsiteQrDestination = when (activeRole) {
    "entrant" -> CampsiteQrDestination.ENTRANT_DETAILS
    "admin" -> CampsiteQrDestination.ADMIN_DETAILS
    "owner" -> CampsiteQrDestination.OWNER_NOTICE
    else -> CampsiteQrDestination.DENIED
}