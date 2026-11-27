package com.example.birchbark.model

import com.google.firebase.firestore.Source

/**
 * US 01.12.01
 *
 * Concrete adapter reusing CampsiteRepository rather than adding
 * another Firestore implementation. Only QR reads require the server explicitly.
 *
 * @Note See #[RW-15], #[RW-16], #[RW-17], #[RW-18] in References.md
 * @Note See #[RW+91] in LLM.md
 *
 * @param repository existing campsite persistence implementation.
 */
class RepositoryCampsiteQrLookup(
    private val repository: CampsiteRepository = CampsiteRepository()
) : CampsiteQrLookup {

    /**
     * @param campsiteId validated campsite document ID.
     * @param onResult receives the current campsite, missing result or failure.
     */
    override fun load(campsiteId: String, onResult: (CampsiteQrResult) -> Unit) {
        repository.getCampsite(campsiteId, object : CampsiteRepository.LoadCallback {
            override fun onLoaded(campsite: Campsite) {
                onResult(CampsiteQrResult.Found(campsite))
            }
            override fun onNotFound() = onResult(CampsiteQrResult.Missing())
            override fun onError(error: Exception) = onResult(CampsiteQrResult.Failed(error))
        }, source = Source.SERVER)
    }
}