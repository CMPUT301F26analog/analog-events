package com.example.birchbark.model

/**
 * US 02.04.08: an invite from one campsite owner to another, asking to put the
 * other owner's campsite in their lottery. Saved in the Firestore
 * "lotteryInvites" collection. The campsite only joins the lottery once the
 * other owner accepts.
 *
 * Every field has a default so Firestore can make an empty one before filling it in.
 *
 * @property id Firestore document ID, empty until it's saved.
 * @property lotteryId the lottery the campsite is being invited to.
 * @property fromOwnerId the owner who made the lottery and sent the invite.
 * @property toOwnerId the owner of the invited campsite, they accept or decline.
 * @property campsiteId the invited campsite.
 * @property campsiteName the invited campsite's name, so the invite screen doesn't need another lookup.
 * @property registrationStart the lottery's registration start (epoch ms), shown on the invite.
 * @property registrationEnd the lottery's registration end (epoch ms), shown on the invite.
 * @property status pending until the other owner accepts or declines.
 * @property createdAt when it was sent (epoch ms).
 */
data class LotteryInvite(
    val id: String = "",
    val lotteryId: String = "",
    val fromOwnerId: String = "",
    val toOwnerId: String = "",
    val campsiteId: String = "",
    val campsiteName: String = "",
    val registrationStart: Long = 0L,
    val registrationEnd: Long = 0L,
    val status: InviteStatus = InviteStatus.PENDING,
    val createdAt: Long = 0L,
)

/**
 * US 02.04.08: where a [LotteryInvite] is at. Saved in Firestore as the name, like "PENDING".
 */
enum class InviteStatus {
    /** Sent, the other owner hasn't answered yet. */
    PENDING,

    /** The other owner said yes, so their campsite is in the lottery now. */
    ACCEPTED,

    /** The other owner said no. */
    DECLINED,
}