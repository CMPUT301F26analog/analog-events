package com.example.birchbark.model

/**
 * US 01.12.01 and US 02.02.01
 *
 * Concrete, stateless codec for campsite QR functionality.
 * Codec is independent of the scanner, Android and Firestore. The URI
 * identifies a campsite, not a user, and grants no access permissions.
 *
 * @Note See #[RW-15], #[RW-16], #[RW-17], #[RW-18] in References.md
 * @Note See #[RW+91] in LLM.md
 */
class CampsiteQrCodec {
    private val prefix = "birchbark://campsite/"
    private val idPattern = Regex("[A-Za-z0-9_-]{1,1500}")

    /**
     * Encodes the existing Firestore campsite ID into a promotional URI.
     * @param campsiteId document ID assigned by CampsiteRepository.
     * @return canonical BirchBark URI.
     * @throws IllegalArgumentException if the ID is not supported.
     */
    fun encode(campsiteId: String): String {
        require(idPattern.matches(campsiteId)) { "Invalid campsite ID." }
        return prefix + campsiteId
    }

    /**
     * Validates raw QR or deep-link data without performing a database read.
     * @param data untrusted QR from any scanner.
     * @return campsite ID, or null for a malformed/unrelated QR.
     */
    fun decode(data: String): String? =
        if (data.startsWith(prefix)) data.removePrefix(prefix)
            .takeIf { idPattern.matches(it) } else null

    /**
     * @param data raw QR payload.
     * @return true if its format is supported (not a guarantee of existence).
     */
    fun isValid(data: String): Boolean = decode(data) != null
}