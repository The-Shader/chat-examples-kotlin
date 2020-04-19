package examples.animal.forest.chat.tests

import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
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

    private fun destroyClient(client: PubNub?) {
        var client = client
        client!!.unsubscribeAll()
        client.forceDestroy()
        client = null
    }

    private val pubNubClient: PubNub
        private get() = PubNub(pnConfiguration)

    /*pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);*/
    private val pnConfiguration: PNConfiguration
        private get() {
            val pnConfiguration = PNConfiguration()
            pnConfiguration.subscribeKey =
                SUB_KEY
            pnConfiguration.publishKey =
                PUB_KEY
            /*pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);*/return pnConfiguration
        }

    val uuid: String
        get() = pubNub!!.configuration.uuid

    fun randomUuid(): String {
        return UUID.randomUUID().toString()
    }

    companion object {
        val SUB_KEY: String = com.example.chatexample.BuildConfig.SUB_KEY
        val PUB_KEY: String = com.example.chatexample.BuildConfig.PUB_KEY
        const val TIMEOUT_SHORT = 2
        const val TIMEOUT_MEDIUM = 5
        const val TIMEOUT_LONG = 10
    }
}