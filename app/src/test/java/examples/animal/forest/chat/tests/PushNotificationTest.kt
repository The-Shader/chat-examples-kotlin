package examples.animal.forest.chat.tests

import com.google.gson.JsonObject
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.enums.PNPushType
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
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class PushNotificationTest : TestHarness() {
    @Test
    fun testAddDeviceToken() {
        val pushRegisteredSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        val firebaseInstanceId = randomUuid()

        // tag::PUSH-1[]
        pubNub!!.addPushNotificationsOnChannels() // tag::ignore[]
            .channels(listOf(expectedChannel)) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channels(Arrays.asList("ch1"))
                // tag::ignore[]
                */
            // end::ignore[]
            .pushType(PNPushType.GCM)
            .deviceId(firebaseInstanceId)
            .async { result, status ->
                // handle status, response
                // tag::ignore[]
                Assert.assertFalse(status.isError)
                Assert.assertNotNull(result)
                pushRegisteredSuccess.set(true)
                // end::ignore[]
            }
        // end::PUSH-1[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(pushRegisteredSuccess)
    }

    @Test
    fun testRemoveDeviceToken() {
        val pushUnregisteredSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        val firebaseInstanceId = randomUuid()

        // tag::PUSH-2[]
        pubNub!!.removePushNotificationsFromChannels() // tag::ignore[]
            .channels(Arrays.asList(expectedChannel)) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channels(Arrays.asList("ch1"))
                // tag::ignore[]
                */
            // end::ignore[]
            .pushType(PNPushType.GCM)
            .deviceId(firebaseInstanceId)
            .async { result, status ->
                // handle status, response
                // tag::ignore[]
                Assert.assertFalse(status.isError)
                Assert.assertNotNull(result)
                pushUnregisteredSuccess.set(true)
                // end::ignore[]
            }
        // end::PUSH-2[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(pushUnregisteredSuccess)
    }

    @Test
    fun testFormattingMessages() {
        val formattedMessageSentSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        observerClient!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (PnUtils.isSubscribed(status, expectedChannel)) {
                    // tag::PUSH-3[]
                    val payload = JsonObject()
                    val message = JsonObject()
                    val gcmData = JsonObject()
                    message.addProperty("league", "NBA")
                    message.addProperty("match", "Orlando Magic - Toronto Raptors")
                    message.addProperty("date", "22. Apr 2019, 01:00")
                    gcmData.add("data", message)
                    payload.add("message", message)
                    payload.add("pn_gcm", gcmData)
                    pubNub!!.publish() // tag::ignore[]
                        .channel(expectedChannel) // end::ignore[]
                        // tag::ignore[]
                        /*
                            // end::ignore[]
                            .channel("ch1")
                            // tag::ignore[]
                            */
                        // end::ignore[]
                        .message(payload)
                        .async { result, status ->
                            // tag::ignore[]
                            Assert.assertFalse(status.isError)
                            Assert.assertNotNull(result)
                            formattedMessageSentSuccess.set(true)
                            // end::ignore[]
                        }
                    // end::PUSH-3[]
                }
            }

            override fun message(pubnub: PubNub, message: PNMessageResult) {
                if (message.publisher == uuid && message.channel == expectedChannel) {
                    formattedMessageSentSuccess.set(true)
                } else {
                    formattedMessageSentSuccess.set(false)
                }
            }

            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
            }

            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {
            }

            override fun messageAction(
                pubnub: PubNub,
                pnMessageActionResult: PNMessageActionResult
            ) {
            }

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
            }

            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {
            }

            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {
            }
        })
        observerClient!!.subscribe()
            .channels(listOf(expectedChannel))
            .withPresence()
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(formattedMessageSentSuccess)
    }
}