package examples.animal.forest.chat.integrationtests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonObject
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.enums.PNOperationType
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult
import org.awaitility.Awaitility
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class MessageIntegrationTest : TestHarness() {
    @Test
    fun testSubscribeChannel() {
        val success =
            AtomicBoolean(false)
        val channel = randomUuid()
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
                Assert.assertNotNull(presence.uuid)
                if (presence.event == "join") {
                    Assert.assertEquals(channel, presence.channel)
                    Assert.assertEquals(
                        pubNub!!.configuration.uuid,
                        presence.uuid
                    )
                    success.set(true)
                }
            }
        })
        subscribeToChannel(channel)
        Awaitility.await().atMost(TIMEOUT_SHORT.toLong(), TimeUnit.SECONDS)
            .untilTrue(success)
    }

    // tag::TEST-1[]
    @Test
    fun testPublishMessages() {
        val success =
            AtomicBoolean(false)
        val message = randomUuid()
        val channel = randomUuid()
        subscribeToChannel(channel)
        pubNub!!.publish()
            .message(message)
            .channel(channel)
            .shouldStore(true)
            .async { result, status ->
                Assert.assertFalse(status.isError)
                Assert.assertNotNull(result!!.timetoken)
                success.set(true)
            }
        Awaitility.await().atMost(TIMEOUT_SHORT.toLong(), TimeUnit.SECONDS)
            .untilTrue(success)
    }

    // end::TEST-1[]
    @Test
    fun testReceiveMessage() {
        val success =
            AtomicBoolean(false)
        val channel = randomUuid()
        val messagePayload: JsonObject = randomMessage()
        pubNub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (status.operation == PNOperationType.PNSubscribeOperation
                    && status.affectedChannels!!.contains(channel)
                ) {
                    publishMessage(channel, messagePayload)
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
            override fun message(pubnub: PubNub, message: PNMessageResult) {
                Assert.assertEquals(messagePayload, message.message)
                success.set(true)
            }
            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
            }
        })
        subscribeToChannel(channel)
        Awaitility.await().atMost(TIMEOUT_SHORT.toLong(), TimeUnit.SECONDS)
            .untilTrue(success)
    }

    @Test
    @Throws(InterruptedException::class)
    fun testChannelHistory() {
        val signal = CountDownLatch(1)
        val messagePayload: JsonObject = randomMessage()
        val channel = randomUuid()
        publishMessage(channel, messagePayload)
        wait(1)
        pubNub!!.history()
            .channel(channel)
            .async { result, status ->
                Assert.assertFalse(status.isError)
                Assert.assertEquals(
                    messagePayload,
                    result!!.messages[0].entry
                )
                signal.countDown()
            }
        signal.await()
    }

    @Test
    fun testHistoryChunkMessages() {
        val recursiveHistoryCount =
            AtomicInteger(0)
        val channel = UUID.randomUUID().toString()
        val messageCount = 120
        val expectedHistoryCallCount = 2
        subscribeToChannel(channel)
        publishMessages(channel, messageCount)
        wait(TIMEOUT_MEDIUM)
        getAllMessages(channel, null, recursiveHistoryCount)
        Awaitility.await().atMost(TIMEOUT_LONG.toLong(), TimeUnit.SECONDS)
            .untilAtomic(recursiveHistoryCount, Matchers.equalTo(expectedHistoryCallCount))
    }

    private fun getAllMessages(
        channel: String,
        startTimeToken: Long?,
        historyCallCount: AtomicInteger
    ) {
        pubNub!!.history()
            .channel(channel)
            .start(startTimeToken)
            .reverse(false)
            .async { result, status ->
                Assert.assertFalse(status.isError)
                Assert.assertNotNull(result)
                val messages =
                    result!!.messages
                if (messages.size == 100) {
                    getAllMessages(channel, result.startTimetoken, historyCallCount)
                }
                historyCallCount.incrementAndGet()
            }
    }
}
