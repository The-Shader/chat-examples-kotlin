package examples.animal.forest.chat.tests

import examples.animal.forest.chat.tests.mock.Log
import com.google.gson.JsonObject
import com.pubnub.api.PubNub
import com.pubnub.api.PubNubException
import com.pubnub.api.PubNubUtil
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
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class MessagesTest : TestHarness() {
    @Test
    fun testSendMessage() {
        val messageSendSuccess =
            AtomicBoolean(false)
        val message = JsonObject()
        message.addProperty("senderId", "user123")
        message.addProperty("text", "hello")
        // tag::MSG-1[]
        pubNub!!.publish()
            .message(message)
            .channel("room-1")
            .async { result, status ->
                // tag::ignore[]
                Assert.assertFalse(status.isError)
                Assert.assertNotNull(result)
                messageSendSuccess.set(true)
                // end::ignore[]
                if (!status.isError) {
                    // message is sent
                    val timetoken = result?.timetoken // message timetoken
                }
            }
        // end::MSG-1[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(messageSendSuccess)
    }

    @Test
    fun testReceiveMessage() {
        val messageReceivedSuccess =
            AtomicBoolean(false)
        val messageObject = JsonObject()

        // tag::MSG-2[]
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
                // tag::ignore[]
                if (PnUtils.isSubscribed(status, "room-1")) {
                    messageObject.addProperty("senderId", "user123")
                    messageObject.addProperty("text", "hello")
                    pubNub!!.publish()
                        .channel("room-1")
                        .message(messageObject)
                        .async { result, status -> Assert.assertFalse(status.isError) }
                }
                // end::ignore[]
            }

            override fun message(pubnub: PubNub, message: PNMessageResult) {
                // the channel for which the message belongs
                val channel = message.channel

                // the channel group or wildcard subscription match (if exists)
                val channelGroup = message.subscription

                // publish timetoken
                val publishTimetoken = message.timetoken

                // the payload
                val messagePayload = message.message

                // the publisher
                val publisher = message.publisher

                // tag::ignore[]
                assertEquals("room-1", channel)
                assertEquals(publisher, uuid)
                assertEquals(messageObject, messagePayload)
                messageReceivedSuccess.set(true)
                // end::ignore[]
            }

            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
            }
        })
        // end::MSG-2[]
        pubNub!!.subscribe()
            .channels(Arrays.asList("room-1"))
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(messageReceivedSuccess)
    }

    // tag::MSG-3[]
    // Calculating a PubNub Message Payload Size
    @Throws(PubNubException::class)
    private fun payloadSize(channel: String, message: Any): Int {
        val encodedPayload =
            PubNubUtil.urlEncode(channel + "/" + pubNub!!.mapper.toJson(message))
        println(encodedPayload)
        return encodedPayload.length + 150 // 150 is length of publish API prefix.
    }

    // end::MSG-3[]
    @Test
    @Throws(PubNubException::class)
    fun testSendImagesAndFiles() {
        // tag::MSG-3[]
        // usage example
        val channel = "room-1"
        val messagePayload = JsonObject()
        messagePayload.addProperty("senderId", "user123")
        messagePayload.addProperty("text", "Hello World")
        val size = payloadSize(channel, messagePayload)
        Log.i("payload_size", size.toString())
        // end::MSG-3[]
        Assert.assertEquals(230, size.toLong())
    }

    @Test
    fun testSendTypingIndicators() {
        val typingIndicatorSendSuccess =
            AtomicBoolean(false)
        // tag::MSG-4[]
        val message = JsonObject()
        message.addProperty("senderId", "user123")
        message.addProperty("isTyping", true)
        pubNub!!.publish()
            .channel("room-1")
            .message(message)
            .shouldStore(false)
            .async { result, status ->
                // tag::ignore[]
                Assert.assertFalse(status.isError)
                typingIndicatorSendSuccess.set(true)
                // end::ignore[]
            }
        // end::MSG-4[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(typingIndicatorSendSuccess)
    }

    @Test
    fun testShowMessageTimestamp() {
        val timestampShownSuccess =
            AtomicBoolean(false)
        // tag::MSG-5[]
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
                // tag::ignore[]
                if (PnUtils.isSubscribed(status, "room-1")) {
                    val message = JsonObject()
                    message.addProperty("senderId", "user123")
                    message.addProperty("text", "hello")
                    pubNub!!.publish()
                        .channel("room-1")
                        .message(message)
                        .async { result, status -> Assert.assertFalse(status.isError) }
                }
                // end::ignore[]
            }

            override fun message(pubnub: PubNub, message: PNMessageResult) {
                // tag::ignore[]
                Assert.assertEquals("room-1", message.channel)
                assertEquals(uuid, message.publisher)
                // end::ignore[]
                val timetoken = message.timetoken / 10000L
                val sdf =
                    SimpleDateFormat("dd MMM yyyy HH:mm:ss")
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timetoken
                val localDateTime = sdf.format(calendar.timeInMillis)
                Log.d("localDateTime", localDateTime)
                // tag::ignore[]
                timestampShownSuccess.set(true)
                // end::ignore[]
            }

            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
            }
        })
        // end::MSG-5[]
        pubNub!!.subscribe()
            .channels(Arrays.asList("room-1"))
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(timestampShownSuccess)
    }

    @Test
    fun testUpdatingMessages() {
        val messageUpdatedSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        val expectedText = randomUuid()
        val initialTimetoken =
            AtomicLong(0)
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
                if (PnUtils.isSubscribed(
                        status,
                        expectedChannel
                    )
                ) {
                    // tag::MSG-6.1[]
                    // tag::ignore[]
                    /*
                    // end::ignore[]
                    Long firstMessageTimeToken; // keep track of message timetoken that should be edited later
                    // tag::ignore[]
                    */
                    // end::ignore[]
                    val messagePayload = JsonObject()
                    messagePayload.addProperty("senderId", "user123")
                    messagePayload.addProperty("text", "Hello, hoomans!")
                    pubNub!!.publish() // tag::ignore[]
                        .channel(expectedChannel) // end::ignore[]
                        // tag::ignore[]
                        /*
                            // end::ignore[]
                            .channel("room-1")
                            // tag::ignore[]
                            */
                        // end::ignore[]
                        .message(messagePayload)
                        .async { result, status ->
                            // tag::ignore[]
                            Assert.assertFalse(status.isError)
                            Assert.assertNotNull(result)
                            initialTimetoken.set(result!!.timetoken)
                            // end::ignore[]
                            // tag::ignore[]
                            /*
                            // end::ignore[]
                            if (!status.isError()) {
                                firstMessageTimeToken = result.getTimetoken(); // save timetoken
                            }
                            // tag::ignore[]
                            */
                            // end::ignore[]
                        }

                    // end::MSG-6.1[]
                }
            }

            override fun message(pubnub: PubNub, message: PNMessageResult) {
                if (message.channel == expectedChannel && message.publisher == uuid) {
                    if (!message.message.asJsonObject.has("timetoken")) {
                        // tag::MSG-6.2[]

                        // edit the message
                        val messagePayload = JsonObject()
                        // tag::ignore[]
                        /*
                        // end::ignore[]
                        // attach timetoken of previous message
                        messagePayload.addProperty("timetoken", firstMessageTimeToken);
                        // tag::ignore[]
                        */
                        // end::ignore[]
                        // tag::ignore[]
                        messagePayload.addProperty("timetoken", message.timetoken)
                        // end::ignore[]
                        messagePayload.addProperty("senderId", "user123")
                        // tag::ignore[]
                        messagePayload.addProperty("text", expectedText)
                        // end::ignore[]
                        // tag::ignore[]
                        /*
                        // end::ignore[]
                        messagePayload.addProperty("text", "Fixed. I had a typo earlier...");
                        // tag::ignore[]
                        */
                        // end::ignore[]
                        pubNub!!.publish() // tag::ignore[]
                            .channel(expectedChannel) // end::ignore[]
                            // tag::ignore[]
                            /*
                                // end::ignore[]
                                .channel("room-1")
                                // tag::ignore[]
                                */
                            // end::ignore[]
                            .message(messagePayload)
                            .async { result, status ->
                                // tag::ignore[]
                                Assert.assertFalse(status.isError)
                                Assert.assertNotNull(result)
                                // end::ignore[]
                                // handle status, response
                            }
                        // end::MSG-6.2[]
                    } else {
                        Assert.assertEquals(
                            expectedText,
                            message.message.asJsonObject["text"].asString
                        )
                        Assert.assertEquals(
                            initialTimetoken.get(), message.message
                                .asJsonObject["timetoken"]
                                .asLong
                        )
                        messageUpdatedSuccess.set(true)
                    }
                }
            }

            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
            }
        })
        pubNub!!.subscribe()
            .channels(Arrays.asList(expectedChannel))
            .withPresence()
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(messageUpdatedSuccess)
    }

    @Test
    fun testSendingAnnouncements() {
        val announcementSentSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        val expectedText = randomUuid()
        observerClient!!.addListener(object : SubscribeCallback() {
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
                if (PnUtils.isSubscribed(
                        status,
                        expectedChannel
                    )
                ) {

                    // tag::MSG-7[]
                    val messagePayload = JsonObject()
                    messagePayload.addProperty("senderId", "user123")
                    // tag::ignore[]
                    /*
                    // end::ignore[]
                    messagePayload.addProperty("text", "Hello, this is an announcement");
                    // tag::ignore[]
                    */
                    // end::ignore[]
                    // tag::ignore[]
                    messagePayload.addProperty("text", expectedText)
                    // end::ignore[]
                    pubNub!!.publish()
                        .message(messagePayload) // tag::ignore[]
                        .channel(expectedChannel) // end::ignore[]
                        // tag::ignore[]
                        /*
                            // end::ignore[]
                            .channel("room-1")
                            // tag::ignore[]
                            */
                        // end::ignore[]
                        .async { result, status ->
                            // tag::ignore[]
                            Assert.assertFalse(status.isError)
                            Assert.assertNotNull(result)
                            // end::ignore[]
                            // handle status, response
                        }
                    // end::MSG-7[]
                }
            }

            override fun message(pubnub: PubNub, message: PNMessageResult) {
                if (message.channel == expectedChannel && message.publisher == uuid) {
                    Assert.assertEquals(
                        expectedText,
                        message.message.asJsonObject["text"].asString
                    )
                    announcementSentSuccess.set(true)
                }
            }

            override fun presence(
                pubnub: PubNub,
                presence: PNPresenceEventResult
            ) {
            }
        })
        observerClient!!.subscribe()
            .channels(Arrays.asList(expectedChannel))
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(announcementSentSuccess)
    }
}