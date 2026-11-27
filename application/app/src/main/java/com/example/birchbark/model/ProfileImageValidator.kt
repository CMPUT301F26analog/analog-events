package com.example.birchbark.model

/**
 * Checks a profile image before it's saved. Call this before saving.
 * Written as one small function like [ReviewValidator] because there are only a few checks.
 */
object ProfileImageValidator {

    /**
     * The biggest image allowed, in bytes. A 256 pixel square JPEG is usually
     * 15 to 40 KB, so this leaves plenty of room, and stays far under Firestore's
     * 1 MB limit for a whole document. See [TBD 5] in LLM.md
     */
    const val MAX_BYTES = 200_000

    /**
     * Checks an image and who it's for.
     *
     * @param fid the entrant's FID, i.e. their document ID in the "users" collection.
     * @param image the JPEG bytes to save.
     * @return the message of every check that failed, so all the problems show at
     *   once. An empty list means the image is safe to save.
     */
    fun validate(fid: String, image: ByteArray): List<String> {
        val errors = mutableListOf<String>()

        if (fid.isBlank()) {
            errors.add("Profile is required")
        }
        if (image.isEmpty()) {
            errors.add("Choose an image")
        }
        if (image.size > MAX_BYTES) {
            errors.add("Image is too large")
        }

        return errors
    }
}
