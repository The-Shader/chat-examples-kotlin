package examples.animal.forest.chat.tests

import com.google.gson.JsonObject
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.enums.PNReconnectionPolicy
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
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ConnectToPubNubTest : TestHarness() {
    @Test
    fun testSetup() {
        /*
        // tag::CON-1[]
        implementation 'com.pubnub:pubnub-gson:4.22.0-beta'
        // end::CON-1[]
        */
    }

    @Test
    fun testInitializingPubNub() {
        // tag::CON-2[]
        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = SUB_KEY
        pnConfiguration.publishKey = PUB_KEY
        val pubNub = PubNub(pnConfiguration)
        // end::CON-2[]
        Assert.assertNotNull(pubNub)
        Assert.assertNotNull(pubNub.configuration.uuid)
    }

    @Test
    fun testSettingUuid() {
        val uuid = randomUuid()
        // tag::CON-3[]
        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = SUB_KEY
        pnConfiguration.publishKey = PUB_KEY
        pnConfiguration.uuid = uuid
        val pubNub = PubNub(pnConfiguration)
        // end::CON-3[]
        Assert.assertNotNull(uuid)
        Assert.assertNotNull(pubNub)
        Assert.assertEquals(uuid, pubNub.configuration.uuid)
    }

    @Test
    fun testSettingState() {
        val setStateSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()

        // tag::CON-4[]
        val state = JsonObject()
        state.addProperty("mood", "grumpy")
        pubNub!!.setPresenceState()
            .state(state) // tag::ignore[]
            .channels(Arrays.asList(expectedChannel)) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channels(Arrays.asList("room-1"))
                // tag::ignore[]
                */
            // end::ignore[]
            .async { result, status ->
                // tag::ignore[]
                Assert.assertNotNull(status)
                Assert.assertNotNull(result)
                Assert.assertFalse(status.isError)
                Assert.assertEquals(result!!.state, state)
                setStateSuccess.set(true)
                // end::ignore[]
                if (!status.isError) {
                    // handle state setting response
                }
            }
        // end::CON-4[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(setStateSuccess)
        val getStateSuccess =
            AtomicBoolean(false)
        // tag::CON-5[]
        pubNub!!.presenceState // tag::ignore[]
            .channels(Arrays.asList(expectedChannel)) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channels(Arrays.asList("room-1"))
                // tag::ignore[]
                */
            // end::ignore[]
            .async { result, status ->
                // tag::ignore[]
                Assert.assertNotNull(status)
                Assert.assertNotNull(result)
                Assert.assertFalse(status.isError)
                Assert.assertEquals(
                    result!!.stateByUUID[expectedChannel]
                        !!.asJsonObject["mood"], state["mood"]
                )
                getStateSuccess.set(true)
                // end::ignore[]
                if (!status.isError) {
                    // handle state setting response
                }
            }
        // end::CON-5[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(getStateSuccess)
    }

    @Test
    fun testDisconnecting() {
        val unsubscribedSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        observerClient!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (PnUtils.isSubscribed(
                        status,
                        expectedChannel
                    )
                ) {
                    pubNub!!.subscribe() // tag::ignore[]
                        .channels(listOf(expectedChannel)) // end::ignore[]
                        // tag::ignore[]
                        /*
                            // end::ignore[]
                            .channels(Arrays.asList("room-1"))
                            // tag::ignore[]
                            */
                        // end::ignore[]
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
                if (presence.event == "leave" && (presence.uuid
                            == pubNub!!.configuration.uuid)
                ) {
                    unsubscribedSuccess.set(true)
                }
            }
        })
        pubNub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (PnUtils.isSubscribed(
                        status,
                        expectedChannel
                    )
                ) {
                    // tag::CON-6[]
                    pubNub!!.unsubscribeAll()
                    // end::CON-6[]
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
            }
        })
        observerClient!!.subscribe() // tag::ignore[]
            .channels(Arrays.asList(expectedChannel)) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channels(Arrays.asList("room-1"))
                // tag::ignore[]
                */
            // end::ignore[]
            .withPresence()
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(unsubscribedSuccess)
    }

    @Test
    fun testReconnectingManually() {
        // tag::CON-7.1[]
        val pnConfiguration = PNConfiguration()
        pnConfiguration.subscribeKey = SUB_KEY
        pnConfiguration.publishKey = PUB_KEY
        pnConfiguration.reconnectionPolicy = PNReconnectionPolicy.LINEAR
        val pubNub = PubNub(pnConfiguration)
        // end::CON-7.1[]

        // tag::CON-7.2[]
        /*
         * If connection availability check will be done in other way,
         * then use this  function to reconnect to PubNub.
         */pubNub.reconnect()
        // end::CON-7.2[]
        Assert.assertNotNull(pubNub)
        Assert.assertEquals(
            pubNub.configuration.reconnectionPolicy,
            PNReconnectionPolicy.LINEAR
        )
    }
}