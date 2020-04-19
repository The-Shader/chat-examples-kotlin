package examples.animal.forest.chat.integrationtests

import android.os.SystemClock
import com.example.chatexample.BuildConfig
import com.google.gson.JsonObject
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.enums.PNHeartbeatNotificationOptions
import org.junit.After
import org.junit.Before
import java.util.*

abstract class TestHarness {
    var pubNub: PubNub? = null
    var observerClient: PubNub? = null

    @Before
    fun before() {
        pubNub = pubNubClient
        observerClient = pubNubClient
    }

    @After
    fun after() {
        destroyClient(pubNub)
        destroyClient(observerClient)
    }

    fun destroyClient(client: PubNub?) {
        var client = client
        client!!.unsubscribeAll()
        client.forceDestroy()
        client = null
    }

    private val pubNubClient: PubNub
        get() = PubNub(pnConfiguration)

    private val pnConfiguration: PNConfiguration
        get() {
            val pnConfiguration = PNConfiguration()
            pnConfiguration.subscribeKey =
                SUB_KEY
            pnConfiguration.publishKey =
                PUB_KEY
            return pnConfiguration
        }

    protected fun getPubNub(uuid: String?): PubNub {
        val pnbConfiguration = PNConfiguration()
        pnbConfiguration.subscribeKey =
            SUB_KEY
        pnbConfiguration.publishKey =
            PUB_KEY
        pnbConfiguration.isSecure = true
        pnbConfiguration.uuid = uuid
        pnbConfiguration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.ALL
        return PubNub(pnbConfiguration)
    }

    val uuid: String
        get() = pubNub!!.configuration.uuid

    fun randomUuid(): String {
        return UUID.randomUUID().toString()
    }

    fun wait(seconds: Int) {
        SystemClock.sleep(seconds * 1000.toLong())
    }

    fun subscribeToChannel(channel: String) {
        pubNub!!.subscribe()
            .channels(listOf(channel))
            .withPresence()
            .execute()
    }

    fun subscribeToChannel(pubnub: PubNub, channel: String) {
        pubnub.subscribe()
            .channels(listOf(channel))
            .withPresence()
            .execute()
    }

    fun publishMessage(channel: String?, message: JsonObject?) {
        pubNub!!.publish()
            .message(message)
            .channel(channel)
            .shouldStore(true)
            .async { _, _ -> }
    }

    fun publishMessages(channel: String?, counter: Int) {
        for (i in 0 until counter) {
            publishMessage(channel, randomMessage())
        }
    }

    fun randomMessage(): JsonObject {
        val messagePayload = JsonObject()
        messagePayload.addProperty("senderId", pubNub!!.configuration.uuid)
        messagePayload.addProperty("text", randomUuid())
        return messagePayload
    }

    fun randomMessage(client: PubNub): JsonObject {
        val messagePayload = JsonObject()
        messagePayload.addProperty("senderId", client.configuration.uuid)
        messagePayload.addProperty("text", randomUuid())
        return messagePayload
    }

    companion object {
        val SUB_KEY: String = BuildConfig.SUB_KEY
        val PUB_KEY: String = BuildConfig.PUB_KEY
        const val TIMEOUT_SHORT = 2
        const val TIMEOUT_MEDIUM = 5
        const val TIMEOUT_LONG = 10
    }
}
