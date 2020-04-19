package examples.animal.forest.chat.tests

import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult
import examples.animal.forest.chat.tests.PnUtils
import examples.animal.forest.chat.tests.TestHarness
import org.awaitility.Awaitility
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class PresenceTest : TestHarness() {
    @Test
    fun testReceivePresenceEvents() {
        val presenceEventReceivedSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        pubNub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (PnUtils.isSubscribed(status, expectedChannel)) {
                    observerClient!!.subscribe()
                        .channels(listOf(expectedChannel))
                        .withPresence()
                        .execute()
                }
            }
            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}
            override fun messageAction(
                pubnub: PubNub,
                pnMessageActionResult: PNMessageActionResult
            ) {
            }
            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}
            override fun message(pubnub: PubNub, message: PNMessageResult) {}
            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
                if (presence.event == "join" && presence.uuid == observerClient!!.configuration
                        .uuid && presence.channel == expectedChannel
                ) {
                    presenceEventReceivedSuccess.set(true)
                }
            }
        })

        // tag::PRE-1[]
        pubNub!!.subscribe() // tag::ignore[]
            .channels(listOf(expectedChannel)) /*
                // end::ignore[]
                .channels(Arrays.asList("room-1"))
                // tag::ignore[]
                */
            // end::ignore[]
            .withPresence()
            .execute()
        // end::PRE-1[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(presenceEventReceivedSuccess)
    }

    @Test
    fun testRequestOnDemandPresenceStatus() {
        val presenceStatusReceivedSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        pubNub!!.addListener(object : SubscribeCallback() {

            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}
            override fun messageAction(
                pubnub: PubNub,
                pnMessageActionResult: PNMessageActionResult
            ) {
            }
            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (PnUtils.isSubscribed(status, expectedChannel)) {
                    try {
                        TimeUnit.SECONDS.sleep(TIMEOUT_SHORT.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    // tag::PRE-2[]
                    pubNub!!.hereNow() // tag::ignore[]
                        .channels(listOf(expectedChannel)) /*
                            // end::ignore[]
                            .channels(Arrays.asList("room-1"))
                            // tag::ignore[]
                            */
                        // end::ignore[]
                        .includeUUIDs(true)
                        .includeState(true)
                        .async { result, status ->
                            // tag::ignore[]
                            val expectedOccupants = 1
                            val expectedIndex = 0
                            assertFalse(status.isError)
                            assertNotNull(result)
                            assertEquals(
                                expectedOccupants.toLong(),
                                result!!.channels.size.toLong()
                            )
                            assertNotNull(
                                result.channels[expectedChannel]
                            )
                            assertEquals(
                                expectedChannel, result.channels[expectedChannel]
                                    !!.channelName
                            )
                            assertEquals(
                                expectedOccupants.toLong(), result.channels[expectedChannel]
                                    !!.occupants
                                    .size.toLong()
                            )
                            assertEquals(
                                expectedOccupants.toLong(), result.channels[expectedChannel]
                                    !!.occupancy.toLong()
                            )
                            assertEquals(uuid, result.channels[expectedChannel]
                                    !!.occupants[expectedIndex]
                                    .uuid)
                            assertNull(
                                result.channels[expectedChannel]
                                    !!.occupants[expectedIndex]
                                    .state
                            )
                            presenceStatusReceivedSuccess.set(true)
                            // end::ignore[]
                            // handle status, response
                        }
                    // end::PRE-2[]
                }
            }
            override fun message(pubnub: PubNub, message: PNMessageResult) {}
            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
            }
        })
        pubNub!!.subscribe()
            .channels(listOf(expectedChannel))
            .withPresence()
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(presenceStatusReceivedSuccess)
    }

    @Test
    fun testLastOnlineTimestamp() {
        val lastOnlineTimestampSuccess =
            AtomicBoolean(true)
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(lastOnlineTimestampSuccess)
        // tag::PRE-3[]
        // in progress
        // end::PRE-3[]
    }
}