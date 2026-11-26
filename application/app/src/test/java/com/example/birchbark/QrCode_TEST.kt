package com.example.birchbark.qr

import com.example.birchbark.model.*
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.*
import org.junit.Test

/**
 * US 01.12.01 and US 02.02.01: concrete JUnit 4 functional test suite.
 * Tests real encoding/decoding, role dispatch, and resolver behaviour with
 * an injected repository stub. No Android runtime, camera or Firebase required.
 */
class QrCodeTest {
    private val codec = CampsiteQrCodec()
    private val campsite = Campsite(id = "fdfE02vc", name = "Hope Lake")
    private val payload = "birchbark://campsite/fdfE02vc"

    /** Independent codec instances produce and understand the same QR. */
    @Test fun independent_codec_instances_that_test_QR_fpr_equivalency() {
        val other = CampsiteQrCodec()
        assertNotSame(codec, other)
        assertEquals(codec.encode(campsite.id), other.encode(campsite.id))
        assertEquals(campsite.id, other.decode(codec.encode(campsite.id)))
    }

    /** Valid ID is reversible, stable, and distinct from another site's (campsite) ID. */
    @Test fun codec_QR_encode_to_decode_back_to_encode_roundTrip() {
        assertEquals(payload, codec.encode(campsite.id))
        assertEquals(campsite.id, codec.decode(payload))
        assertEquals(payload, codec.encode(campsite.id))
        assertNotEquals(payload, codec.encode("ABC124"))
        assertTrue(codec.isValid(payload))
    }

    /** Malformed or foreign QRs cannot become a Firestore document path. */
    @Test fun rejects_invalid_or_malicious_payloads() {
        listOf("", "https://example.com", "birchbark://campsite/", "$payload/other",
            "$payload?x=1", "$payload#x", "birchbark://campsite/..",
            "birchbark://campsite/%2F", "birchbark://campsite/has space",
            "birchbark://user/fdfE02vc", "birchbark://campsite/" + "a".repeat(1501)
        ).forEach { assertNull(it, codec.decode(it)) }
    }

    /** Invalid campsite IDs cannot be generated accidentally. */
    @Test(expected = IllegalArgumentException::class)
    fun rejects_invalid_campsite_id() { codec.encode("a/b") }

    /** A real QR symbol can be decoded by ZXing back to the exact input. */
    @Test fun generated_QR_image_can_be_decoded() {
        val image = ZXingQrCodeGenerator().generate(payload, 512)
        assertEquals(512, image.width)
        assertEquals(512, image.height)
        assertEquals(512 * 512, image.pixels.size)
        assertTrue(image.pixels.contains(0xFF000000.toInt()))
        assertTrue(image.pixels.contains(0xFFFFFFFF.toInt()))
        val luminance = RGBLuminanceSource(image.width, image.height, image.pixels)
        val decoded = QRCodeReader().decode(BinaryBitmap(HybridBinarizer(luminance)))
        assertEquals(payload, decoded.text)
    }

    /** Generator validates its dimensions. */
    @Test(expected = IllegalArgumentException::class)
    fun rejects_invalid_QR_image_size() { ZXingQrCodeGenerator().generate(payload, 0) }

    /** Resolver delegates exactly once, only after validating the QR. */
    @Test fun valid_QR_loads_campsite() {
        var calls = 0
        val resolver = CampsiteQrResolver(CampsiteQrLookup { id, callback ->
            calls++
            assertEquals(campsite.id, id)
            callback(CampsiteQrResult.Found(campsite))
        })
        var result: CampsiteQrResult? = null
        resolver.resolve(payload) { result = it }
        assertEquals(1, calls)
        assertEquals(CampsiteQrResult.Found(campsite), result)
    }

    /** An invalid QR never reaches the repository. */
    @Test fun invalid_QR_does_not_load_details() {
        val resolver = CampsiteQrResolver(CampsiteQrLookup { _, _ -> fail("Unexpected lookup") })
        resolver.resolve("invalid") { assertTrue(it is CampsiteQrResult.Invalid) }
    }

    /** Missing and failed reads are distinguishable rather than treated as invalid QR text. */
    @Test fun missing_and_failed_QR_lookups_handled_with_handler_give_feedback() {
        val missing = CampsiteQrResolver(CampsiteQrLookup { _, callback -> callback(CampsiteQrResult.Missing()) })
        val error = IllegalStateException("offline")
        val failed = CampsiteQrResolver(CampsiteQrLookup { _, callback -> callback(CampsiteQrResult.Failed(error)) })
        missing.resolve(payload) { assertTrue(it is CampsiteQrResult.Missing) }
        failed.resolve(payload) { assertEquals(CampsiteQrResult.Failed(error), it) }
    }

    /** Active role determines the destination without granting new privileges. */
    @Test fun role_determines_QR_success_read_destinations() {
        assertEquals(CampsiteQrDestination.ENTRANT_DETAILS, campsiteQrDestinationForRole("entrant"))
        assertEquals(CampsiteQrDestination.OWNER_NOTICE, campsiteQrDestinationForRole("owner"))
        assertEquals(CampsiteQrDestination.ADMIN_DETAILS, campsiteQrDestinationForRole("admin"))
        assertEquals(CampsiteQrDestination.DENIED, campsiteQrDestinationForRole("unknown"))
    }
}