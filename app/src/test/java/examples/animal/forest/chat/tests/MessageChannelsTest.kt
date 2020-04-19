package examples.animal.forest.chat.tests

import examples.animal.forest.chat.tests.mock.Log
import com.pubnub.api.PubNub
import com.pubnub.api.PubNubException
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.enums.PNOperationType
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsAddChannelResult
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsAllChannelsResult
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsRemoveChannelResult
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

class ManageChannelsTest : TestHarness() {
    @Test
    fun testJoiningSingleChannel() {
        val joinSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        observerClient!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (PnUtils.isSubscribed(
                        status,
                        expectedChannel
                    )
                ) {
                    // tag::CHAN-1[]
                    pubNub!!.subscribe() // tag::ignore[]
                        .channels(listOf(expectedChannel)) // end::ignore[]
                        // tag::ignore[]
                        /*
                            // end::ignore[]
                            .channels(Arrays.asList("room-1"))
                            // tag::ignore[]
                            */
                        // end::ignore[]
                        .execute()
                    // end::CHAN-1[]
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
                if (PnUtils.checkPresence(
                        presence,
                        uuid,
                        "join",
                        expectedChannel
                    )
                ) {
                    joinSuccess.set(true)
                }
            }
        })
        observerClient!!.subscribe()
            .channels(listOf(expectedChannel))
            .withPresence()
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(joinSuccess)
    }

    @Test
    @Throws(PubNubException::class, InterruptedException::class)
    fun testJoiningMultipleChannels() {
        val expectedChannels = mutableListOf(randomUuid(), randomUuid(), randomUuid())
        expectedChannels.sort()

        // tag::CHAN-2[]
        pubNub!!.subscribe() // tag::ignore[]
            .channels(expectedChannels) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channels(Arrays.asList("room-1", "room-2", "room-3"))
                // tag::ignore[]
                */
            // end::ignore[]
            .execute()
        // end::CHAN-2[]
        TimeUnit.SECONDS.sleep(TIMEOUT_MEDIUM.toLong())
        val hereNowResult = pubNub!!.hereNow()
            .channels(expectedChannels)
            .includeUUIDs(true)
            .sync()
        Assert.assertNotNull(hereNowResult)
        Assert.assertEquals(
            expectedChannels.size.toLong(),
            hereNowResult!!.totalChannels.toLong()
        )
        val actualChannels: MutableList<String> =
            ArrayList()
        for ((key, value) in hereNowResult.channels) {
            actualChannels.add(key)
            var member = false
            for (occupant in value.occupants) {
                if (occupant.uuid == pubNub!!.configuration.uuid) {
                    member = true
                    break
                }
            }
            Assert.assertTrue(member)
        }
        Collections.sort(actualChannels)
        Assert.assertArrayEquals(
            arrayOf<List<*>>(expectedChannels),
            arrayOf<List<*>>(actualChannels)
        )
    }

    @Test
    fun testLeave() {
        val leaveSuccess =
            AtomicBoolean(false)
        val expectedChannel = randomUuid()
        pubNub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (status.affectedChannels!!.contains(expectedChannel)) {
                    if (status.operation == PNOperationType.PNSubscribeOperation) {
                        // tag::CHAN-3[]
                        pubNub!!.unsubscribe() // tag::ignore[]
                            .channels(Arrays.asList(expectedChannel)) // end::ignore[]
                            // tag::ignore[]
                            /*
                                // end::ignore[]
                                .channels(Arrays.asList("room-1"))
                                // tag::ignore[]
                                */
                            // end::ignore[]
                            .execute()
                        // end::CHAN-3[]
                    } else if (status.operation == PNOperationType.PNUnsubscribeOperation
                        && status.uuid == pubNub!!.configuration.uuid
                    ) {
                        leaveSuccess.set(true)
                    }
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
        pubNub!!.subscribe()
            .channels(Arrays.asList(expectedChannel))
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(leaveSuccess)
    }

    @Test
    fun testJoinChannelGroup() {
        val joinSuccess =
            AtomicBoolean(false)
        val expectedChannelGroup = randomUuid()
        pubNub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                joinSuccess.set(
                    PnUtils.isSubscribedGroup(
                        status,
                        expectedChannelGroup
                    )
                )
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

        // tag::CHAN-4[]
        pubNub!!.subscribe() // tag::ignore[]
            .channelGroups(Arrays.asList(expectedChannelGroup)) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channelGroups(Arrays.asList("family"))
                // tag::ignore[]
                */
            // end::ignore[]
            .execute()
        // end::CHAN-4[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(joinSuccess)
    }

    @Test
    @Throws(PubNubException::class)
    fun testAddChannelsToChannelGroup() {
        val addSuccess =
            AtomicBoolean(false)
        val expectedChannelGroup = randomUuid()
        val expectedChannel1 = randomUuid()
        val expectedChannel2 = randomUuid()
        Assert.assertNotNull(
            removeChannelsFromChannelGroup(
                expectedChannelGroup,
                expectedChannel1,
                expectedChannel2
            )
        )

        // tag::CHAN-5[]
        pubNub!!.addChannelsToChannelGroup() // tag::ignore[]
            .channelGroup(expectedChannelGroup)
            .channels(
                Arrays.asList(
                    expectedChannel1,
                    expectedChannel2
                )
            ) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channelGroup("family")
                .channels(Arrays.asList("son", "daughter"))
                // tag::ignore[]
                */
            // end::ignore[]
            .async { result, status ->
                // tag::ignore[]
                Assert.assertFalse(status.isError)
                pubNub!!.listChannelsForChannelGroup()
                    .channelGroup(expectedChannelGroup)
                    .async { result, status ->
                        Assert.assertFalse(status.isError)
                        Assert.assertTrue(
                            result!!.channels.contains(expectedChannel1)
                        )
                        Assert.assertTrue(
                            result.channels.contains(expectedChannel2)
                        )
                        addSuccess.set(true)
                    }
                // end::ignore[]
                // handle state setting response
            }
        // end::CHAN-5[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(addSuccess)
    }

    @Test
    @Throws(PubNubException::class)
    fun testRemoveChannelsFromChannelGroup() {
        val leaveSuccess =
            AtomicBoolean(false)
        val expectedChannelGroup = randomUuid()
        val expectedChannel = randomUuid()
        Assert.assertNotNull(
            addChannelsToChannelGroup(
                expectedChannelGroup,
                expectedChannel
            )
        )
        val familyChannelGroup =
            getChannelsForChannelGroup(expectedChannelGroup)
        Assert.assertNotNull(familyChannelGroup)
        Assert.assertTrue(familyChannelGroup!!.channels.contains(expectedChannel))

        // tag::CHAN-6[]
        pubNub!!.removeChannelsFromChannelGroup() // tag::ignore[]
            .channels(Arrays.asList(expectedChannel))
            .channelGroup(expectedChannelGroup) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channels(Arrays.asList("son"))
                .channelGroup("family")
                // tag::ignore[]
                */
            // end::ignore[]
            .async { result, status ->
                // tag::ignore[]
                Assert.assertFalse(status.isError)
                try {
                    Assert.assertFalse(
                        getChannelsForChannelGroup(expectedChannelGroup)!!.channels
                            .contains(expectedChannel)
                    )
                    leaveSuccess.set(true)
                } catch (e: PubNubException) {
                    e.printStackTrace()
                    leaveSuccess.set(false)
                }
                // end::ignore[]
            }
        // end::CHAN-6[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(leaveSuccess)
    }

    @Test
    @Throws(PubNubException::class)
    fun testListingChannelsInChannelGroup() {
        val listingSuccess =
            AtomicBoolean(false)
        val expectedChannelGroup = randomUuid()
        val expectedChannel = randomUuid()
        Assert.assertNotNull(
            removeChannelsFromChannelGroup(
                expectedChannelGroup,
                expectedChannel
            )
        )
        Assert.assertNotNull(
            addChannelsToChannelGroup(
                expectedChannelGroup,
                expectedChannel
            )
        )

        // tag::CHAN-7[]
        pubNub!!.listChannelsForChannelGroup() // tag::ignore[]
            .channelGroup(expectedChannelGroup) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channelGroup("family")
                // tag::ignore[]
                */
            // end::ignore[]
            .async { result, status -> // tag::ignore[]
                Assert.assertFalse(status.isError)
                Assert.assertNotNull(result)
                Assert.assertTrue(result!!.channels.contains(expectedChannel))
                listingSuccess.set(true)
                // end::ignore[]
                if (!status.isError) {
                    for (channel in result.channels) {
                        Log.i("channel", channel)
                    }
                }
            }
        // end::CHAN-7[]
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(listingSuccess)
    }

    @Test
    fun testLeaveChannelGroup() {
        val leaveSuccess =
            AtomicBoolean(false)
        val expectedChannelGroup = randomUuid()
        pubNub!!.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, status: PNStatus) {
                if (PnUtils.isSubscribedGroup(
                        status,
                        expectedChannelGroup
                    )
                ) {
                    // tag::CHAN-8[]
                    pubNub!!.unsubscribe() // tag::ignore[]
                        .channelGroups(Arrays.asList(expectedChannelGroup)) // end::ignore[]
                        // tag::ignore[]
                        /*
                            // end::ignore[]
                            .channelGroups(Arrays.asList("family"))
                            // tag::ignore[]
                            */
                        // end::ignore[]
                        .execute()
                    // end::CHAN-8[]
                } else if (PnUtils.isUnsubscribedGroup(
                        status,
                        expectedChannelGroup,
                        uuid
                    )
                ) {
                    leaveSuccess.set(true)
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
        pubNub!!.subscribe()
            .channelGroups(Arrays.asList(expectedChannelGroup))
            .execute()
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(leaveSuccess)
    }

    @Throws(PubNubException::class)
    private fun getChannelsForChannelGroup(channelGroup: String): PNChannelGroupsAllChannelsResult? {
        return pubNub!!.listChannelsForChannelGroup()
            .channelGroup(channelGroup)
            .sync()
    }

    @Throws(PubNubException::class)
    private fun addChannelsToChannelGroup(
        channelGroup: String,
        vararg channels: String
    ): PNChannelGroupsAddChannelResult? {
        return pubNub!!.addChannelsToChannelGroup()
            .channelGroup(channelGroup)
            .channels(Arrays.asList(*channels))
            .sync()
    }

    @Throws(PubNubException::class)
    private fun removeChannelsFromChannelGroup(
        channelGroup: String,
        vararg channels: String
    ): PNChannelGroupsRemoveChannelResult? {
        return pubNub!!.removeChannelsFromChannelGroup()
            .channelGroup(channelGroup)
            .channels(Arrays.asList(*channels))
            .sync()
    }
}
