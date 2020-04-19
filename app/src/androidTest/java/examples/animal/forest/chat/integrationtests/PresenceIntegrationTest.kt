package examples.animal.forest.chat.integrationtests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pubnub.api.PubNub
import org.awaitility.Awaitility
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class PresenceIntegrationTest : TestHarness() {
    @Test
    fun testHereNow() {
        val success =
            AtomicBoolean(false)
        val channel = randomUuid()
        val userList: MutableList<String> =
            ArrayList()
        userList.add(randomUuid())
        userList.add(randomUuid())
        userList.add(randomUuid())
        val user1: PubNub = getPubNub(userList[0])
        val user2: PubNub = getPubNub(userList[1])
        val user3: PubNub = getPubNub(userList[2])
        subscribeToChannel(user1, channel)
        subscribeToChannel(user2, channel)
        subscribeToChannel(user3, channel)
        wait(TIMEOUT_SHORT)
        pubNub!!.hereNow()
            .channels(listOf(channel))
            .includeUUIDs(true)
            .async { result, status ->
                Assert.assertFalse(status.isError)
                var numberOfSubscribers = 0
                Assert.assertEquals(1, result!!.totalChannels.toLong())
                Assert.assertEquals(
                    userList.size.toLong(),
                    result.totalOccupancy.toLong()
                )
                for (channelData in result.channels.values) {
                    for (pnHereNowOccupantData in channelData.occupants) {
                        for (s in userList) {
                            if (pnHereNowOccupantData.uuid == s) {
                                numberOfSubscribers++
                            }
                        }
                    }
                }
                Assert.assertEquals(
                    userList.size.toLong(),
                    numberOfSubscribers.toLong()
                )
                destroyClient(user1)
                destroyClient(user2)
                destroyClient(user3)
                success.set(true)
            }
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(success)
    }
}