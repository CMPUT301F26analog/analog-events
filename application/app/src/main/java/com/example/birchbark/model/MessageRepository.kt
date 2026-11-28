package com.example.birchbark.model

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

/**
 * US 01.14.01, US 02.12.01 and US 03.12.01
 * Firestore implementation of [ReservationMessaging] and [OwnerMessageLogReader].
 * Uses the reservation ID for both participants' thread and reads owner-authored entries directly
 * for moderation.
 *
 * @Note See #[RW-2-LLM] in LLM.md
 *
 * @param db Firestore instance used for conversations, messages and campsite lookups.
 * @param reservations existing repository that resolves bookings for each participant.
 */
class FirestoreMessageRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val reservations: ReservationRepository = ReservationRepository()
) : ReservationMessaging, OwnerMessageLogReader {
    private val threads = db.collection("reservationConversations")
    private val entries = "reservationMessages"

    /**
     * Resolves campsite names once per distinct site and checks the selected site owner for the
     * owner picker.
     **/
    override fun bookings(fid: String, side: MessageSide,
                          loaded: (List<MessageBooking>) -> Unit,
                          failed: MessageFailure) {
        val resolve: (List<Reservation>) -> Unit = {
            rows ->
            val active = rows.filter {
                it.acceptsMessages(System.currentTimeMillis())
            }
            val ids = active.mapNotNull {
                it.campsiteId }.distinct()
            val reads = ids.map { db.collection("campsites").document(it).get() }
            Tasks.whenAllSuccess<DocumentSnapshot>(reads).addOnSuccessListener {
                sites -> val byId = sites.associateBy {
                    it.id
                }
                loaded(active.mapNotNull {
                    r -> val site = byId[r.campsiteId] ?: return@mapNotNull null

                    if (!site.exists() || (side == MessageSide.OWNER && site.getString("ownerId") != fid))
                        return@mapNotNull null
                    MessageBooking(r, site.getString("name").orEmpty())
                }
                )
            }.addOnFailureListener { failed(it) }
        }

        if (side == MessageSide.OWNER) reservations.getReservationsForOwner(fid, resolve, failed)
        else reservations.getReservationsForEntrant(fid, resolve, failed)
    }

    /**
     * Queries the role-specific participant field and reads the matching receipt
     * from each summary.
     **/
    override fun inbox(fid: String, side: MessageSide, loaded: (List<ConversationSummary>) -> Unit, failed: MessageFailure) =
        threads.whereEqualTo(side.field, fid).orderBy("sentAt", Query.Direction.DESCENDING)
            .observe({ doc ->
                ConversationSummary(doc.id, doc.string("siteName"), doc.string("entrantName"),
                    doc.string("preview"), doc.millis(), doc.string("lastId"), doc.string("sender"),
                    doc.getString(seenField(side)))
            }, loaded, failed)

    /**
     * Listens to the reservation’s message subcollection in server timestamp order.
     **/
    override fun history(id: String, loaded: (List<ReservationMessage>) -> Unit, failed: MessageFailure) =
        threads.document(id).collection(entries).orderBy("sentAt").observe(::message, loaded, failed)

    /**
     * Queries owner-authored messages across all reservation threads in descending send order.
     **/
    override fun ownerLog(loaded: (List<ReservationMessage>) -> Unit, failed: MessageFailure) =
        db.collectionGroup(entries).whereEqualTo("ownerSent", true)
            .orderBy("sentAt", Query.Direction.DESCENDING).observe(::message, loaded, failed)

    /**
     * Listens to the source reservation and uses its document ID when mapping the result.
     **/
    override fun booking(id: String, loaded: (Reservation?) -> Unit,
                         failed: MessageFailure): MessageSubscription {
            val listener = db.collection("reservations").document(id).addSnapshotListener { doc, error ->
                if (error != null) failed(error) else loaded(doc?.toObject(Reservation::class.java)?.copy(id = id))
            }
        return MessageSubscription { listener.remove() }
    }

    /**
     * Compares the latest committed message ID before updating the participant’s receipt.
     **/
    override fun markSeen(id: String, side: MessageSide,
                          messageId: String,
                          failed: MessageFailure) {
        val ref = threads.document(id)
        db.runTransaction { tx ->
            val thread = tx.get(ref)
            if (thread.getString("lastId") == messageId && thread.getString(seenField(side)) != messageId)
                tx.update(ref, seenField(side), messageId)
            Unit
        }.addOnFailureListener { failed(it) }
    }

    /**
     * Resolves the booked campsite owner inside the transaction. Retrying the same ID, sender
     * and body completes without writing a second entry.
     **/
    override fun send(id: String, side: MessageSide,
                      fid: String, messageId: String,
                      body: String, sent: () -> Unit,
                      failed: MessageFailure) {

        val text = body.trim()

        if (text.isEmpty()) {
            failed(IllegalArgumentException("Enter a message."))
            return
        }

        val thread = threads.document(id)
        val entry = thread.collection(entries).document(messageId)
        db.runTransaction { tx ->
            // Replaying a confirmed send after rotation or a lost response is idempotent.
            val existing = tx.get(entry)
            if (existing.exists()) {
                check(existing.getString("sender") == fid && existing.getString("body") == text)
                return@runTransaction
            }
            val r = tx.get(db.collection("reservations").document(id)).toObject(Reservation::class.java)

            check(r != null && r.acceptsMessages(System.currentTimeMillis())) {
                "This reservation is read-only."
            }

            val site = tx.get(db.collection("campsites").document(requireNotNull(r.campsiteId)))
            val owner = requireNotNull(site.getString("ownerId"))
            val ownerSent = side == MessageSide.OWNER

            check(fid == (if (ownerSent) owner else r.entrantId)) {
                "This booking belongs to another participant."
            }

            val recipient = if (ownerSent) r.entrantId else owner
            val stamp = FieldValue.serverTimestamp()

            tx.set(entry, mapOf("sender" to fid, "recipient" to recipient, "ownerSent" to ownerSent,
                "body" to text, "sentAt" to stamp))
            tx.set(thread, mapOf("ownerId" to owner, "entrantId" to r.entrantId, "siteId" to site.id,
                "siteName" to site.string("name"), "entrantName" to r.entrantName,
                "preview" to text, "sender" to fid, "lastId" to entry.id, "sentAt" to stamp),
                SetOptions.merge())
            Unit
        }.addOnSuccessListener { sent() }.addOnFailureListener { failed(it) }
    }

    /**
     * Returns the receipt field for [side] on the shared thread document.
     **/
    private fun seenField(side: MessageSide) = if (side == MessageSide.OWNER) "ownerSeen"
        else "entrantSeen"

    /**
     * Reads a text field, using an empty display value when the field is absent.
     **/
    private fun DocumentSnapshot.string(key: String) = getString(key).orEmpty()

    /**
     * Returns the server send time in epoch milliseconds, or zero before a timestamp is available.
     **/
    private fun DocumentSnapshot.millis() = getTimestamp("sentAt")?.toDate()?.time ?: 0L

    /**
     * Maps one conversation entry into the shared participant and admin message model.
     **/
    private fun message(d: DocumentSnapshot) = ReservationMessage(d.id, d.string("sender"),
        d.string("recipient"),
        d.string("body"), d.millis(),
        d.getBoolean("ownerSent") == true)

    /**
     * Maps each query snapshot through [map] and returns a detachable listener.
     **/
    private fun <T> Query.observe(map: (DocumentSnapshot) -> T,
                                  loaded: (List<T>) -> Unit,
                                  failed: MessageFailure): MessageSubscription {
        val listener = addSnapshotListener { snapshot, error ->
            if (error != null) failed(error) else if (snapshot != null) loaded(snapshot.documents.map(map))
        }
        return MessageSubscription { listener.remove() }
    }
}