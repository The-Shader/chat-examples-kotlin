package examples.animal.forest.chat.integrationtests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonObject
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
import org.awaitility.Awaitility
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class PresenceEventsIntegrationTests : TestHarness() {
    @Test
    fun testJoinChannel() {
        val success =
            AtomicBoolean(false)
        val channel = UUID.randomUUID().toString()
        pubNub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {}
            override fun message(pubnub: PubNub, message: PNMessageResult) {}
            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}

            override fun messageAction(
                pubnub: PubNub,
                pnMessageActionResult: PNMessageActionResult
            ) {
            }
            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}
            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
                if (presence.event == "join") {
                    assertEquals(channel, presence.channel)
                    assertEquals(
                        pubnub.configuration.uuid,
                        presence.uuid
                    )
                    success.set(true)
                }
            }
        })
        subscribeToChannel(channel)
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(success)
    }

    @Test
    fun testLeaveChannel() {
        val success =
            AtomicBoolean(false)
        val channel = randomUuid()
        observerClient!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {}
            override fun message(pubnub: PubNub, message: PNMessageResult) {}
            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}
            override fun messageAction(
                pubnub: PubNub,
                pnMessageActionResult: PNMessageActionResult
            ) {
            }
            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}
            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
                if (presence.event == "leave") {
                    assertEquals(channel, presence.channel)
                    assertEquals(pubNub!!.configuration.uuid, presence.uuid)
                    success.set(true)
                }
            }
        })
        subscribeToChannel(observerClient!!, channel)
        subscribeToChannel(channel)
        wait(TIMEOUT_SHORT)
        pubNub!!.unsubscribe()
            .channels(listOf(channel))
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(success)
    }

    @Test
    fun testTimeoutFromChannel() {
        val success =
            AtomicBoolean(false)
        val channel = randomUuid()
        pubNub!!.configuration.presenceTimeout = 2
        pubNub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {}
            override fun message(pubnub: PubNub, message: PNMessageResult) {}
            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}
            override fun messageAction(
                pubnub: PubNub,
                pnMessageActionResult: PNMessageActionResult
            ) {
            }
            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}
            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
                if (presence.event == "timeout") {
                    assertEquals(channel, presence.channel)
                    assertEquals(
                        pubNub!!.configuration.uuid,
                        presence.uuid
                    )
                    success.set(true)
                }
            }
        })
        subscribeToChannel(channel)
        Awaitility.await().atMost(TIMEOUT_LONG.toLong(), TimeUnit.SECONDS)
            .untilTrue(success)
    }

    @Test
    fun testStateChangeEvent() {
        val atomic =
            AtomicBoolean(false)
        val channel = UUID.randomUUID().toString()
        val state = JsonObject()
        state.addProperty("is_typing", true)
        subscribeToChannel(channel)
        wait(1)
        pubNub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {}
            override fun message(pubnub: PubNub, message: PNMessageResult) {}
            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {}
            override fun messageAction(
                pubnub: PubNub,
                pnMessageActionResult: PNMessageActionResult
            ) {
            }
            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}
            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}
            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
                if (presence.event == "state-change" && (presence.uuid
                            == pubnub.configuration.uuid)
                ) {
                    assertEquals("state-change", presence.event)
                    pubnub.removeListener(this)
                    atomic.set(true)
                }
            }
        })
        pubNub!!.setPresenceState()
            .channels(listOf(channel))
            .state(state)
            .async { _, _ -> }
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(atomic)
    }
}