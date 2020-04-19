package examples.animal.forest.chat.tests

import android.util.Log
import com.pubnub.api.PubNubException
import org.awaitility.Awaitility
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class HistoryTest : TestHarness() {
    @Test
    fun testFetchMessageCount() {
        val messageCountSuccess =
            AtomicBoolean(false)
        messageCountSuccess.set(true)
        Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
            .untilTrue(messageCountSuccess)
        // tag::HIST-1[]
        // in progress
        // end::HIST-1[]
    }

    @Test
    @Throws(PubNubException::class, InterruptedException::class)
    fun testRetrievePastMessages() {
        val pastMessagesSuccess =
            AtomicBoolean(false)
        val count = 10
        val channel = randomUuid()
        publishMessages(channel, count, object:
            Callback {
            override fun onDone() {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MINUTE, -1)
                val endTimeToken = cal.timeInMillis * 10000L

                // tag::HIST-2[]
                pubNub!!.history() // tag::ignore[]
                    .channel(channel) /*
                        // end::ignore[]
                        .channel("room-1")
                        // tag::ignore[]
                        */
                    // end::ignore[]
                    // tag::ignore[]
                    .end(endTimeToken) // end::ignore[]
                    // tag::ignore[]
                    /*
                            // end::ignore[]
                            .end(13827485876355504L) // timetoken of the last message
                            // tag::ignore[]
                            */
                    // end::ignore[]
                    // tag::ignore[]
                    .count(count) // end::ignore[]
                    // tag::ignore[]
                    /*
                            // end::ignore[]
                            .count(50) // how many items to fetch
                            // tag::ignore[]
                            */
                    // end::ignore[]
                    .reverse(false)
                    .async { result, status ->
                        // tag::ignore[]
                        Assert.assertFalse(status.isError)
                        Assert.assertNotNull(result)
                        Assert.assertEquals(
                            count.toLong(),
                            result!!.messages.size.toLong()
                        )
                        pastMessagesSuccess.set(true)
                        // end::ignore[]
                        // handle status, response
                    }
                // end::HIST-2[]
                Awaitility.await().atMost(TIMEOUT_MEDIUM.toLong(), TimeUnit.SECONDS)
                    .untilTrue(pastMessagesSuccess)
            }
        })
    }

    @Test
    @Throws(PubNubException::class, InterruptedException::class)
    fun testRetrieveMoreThan100Messages() {
        val resursiveHistoryCount =
            AtomicInteger(0)
        val publishMessageCount = 110
        val expectedHistoryCallCount = 2
        val channel = randomUuid()
        publishMessages(channel, publishMessageCount, object:
            Callback {
            override fun onDone() {
                getAllMessages(channel, null, resursiveHistoryCount)
                Awaitility.await()
                    .atMost(TIMEOUT_LONG.toLong(), TimeUnit.SECONDS)
                    .untilAtomic(resursiveHistoryCount, Matchers.equalTo(expectedHistoryCallCount))
            }
        })

        // tag::HIST-3.2[]
        // tag::ignore[]
        /*
        // end::ignore[]
        // Usage example:
        getAllMessages(null);
        // tag::ignore[]
        */
        // end::ignore[]
        // end::HIST-3.2[]
    }

    // tag::HIST-3.1[]
    // tag::ignore[]
    private fun getAllMessages(
        channel: String,
        startTimeToken: Long?,
        historyCallCount: AtomicInteger
    ) {
        // end::ignore[]
        // tag::ignore[]
        /*
    // end::ignore[]
    private void getAllMessages(Long startTimeToken) {
    // tag::ignore[]
    */
        // end::ignore[]
        pubNub!!.history() // tag::ignore[]
            .channel(channel) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channel("room-1")
                // tag::ignore[]
                */
            // end::ignore[]
            .start(startTimeToken)
            .reverse(false)
            .async { result, status ->
                // tag::ignore[]
                Assert.assertFalse(status.isError)
                Assert.assertNotNull(result)
                // end::ignore[]
                if (!status.isError) {
                    val messages =
                        result!!.messages
                    val start = result.startTimetoken
                    val end = result.endTimetoken

                    // if 'messages' were retrieved, do something useful with them
                    if (messages != null && messages.isNotEmpty()) {
                        Log.i("messages", messages.size.toString())
                        Log.i("messages", "start: $start")
                        Log.i("messages", "end: $end")
                    }

                    /*
                                             * if 100 'messages' were retrieved, there might be more, call
                                             * history again
                                             */if (messages!!.size == 100) {
                        // tag::ignore[]
                        getAllMessages(channel, start, historyCallCount)
                        // end::ignore[]
                        // tag::ignore[]
                        /*
                                                // end::ignore[]
                                                getAllMessages(start);
                                                // tag::ignore[]
                                                */
                        // end::ignore[]
                    }
                    // tag::ignore[]
                    historyCallCount.incrementAndGet()
                    // end::ignore[]
                }
            }
    }

    // end::HIST-3.1[]
    @Test
    @Throws(PubNubException::class, InterruptedException::class)
    fun testRetrieveMessagesMultiChannel() {
        val pastMessagesSuccess =
            AtomicBoolean(false)
        val cal = Calendar.getInstance()
        var start = cal.timeInMillis * 10000L
        start--
        val channelsCount = 2
        val messagesCount = 2
        val channels: MutableList<String> =
            ArrayList(channelsCount)
        for (i in 0 until channelsCount) {
            channels.add(randomUuid())
            publishMessages(channels[i], messagesCount, object:
                Callback {
                override fun onDone() {}
            })
        }
        val end = Calendar.getInstance().timeInMillis * 10000L

        // tag::HIST-4[]
        pubNub!!.fetchMessages() // tag::ignore[]
            .channels(channels) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .channels(Arrays.asList("ch1", "ch2", "ch3"))
                // tag::ignore[]
                */
            // end::ignore[]
            // tag::ignore[]
            .start(start)
            .end(end) // end::ignore[]
            // tag::ignore[]
            /*
                // end::ignore[]
                .start(15343325214676133L)
                .end(15343325004275466L)
                // tag::ignore[]
                */
            // end::ignore[]
            .maximumPerChannel(15)
            .async { result, status -> // tag::ignore[]
                assertFalse(status.isError)
                assertNotNull(result)
                assertEquals(channelsCount, result!!.channels.entries.size)
                for ((key, value) in result.channels.entries) {
                    assertTrue(channels.contains(key))
                    assertEquals(
                        messagesCount.toLong(),
                        value.size.toLong()
                    )
                    for (pnMessageResult in value) {
                        assertTrue(channels.contains(pnMessageResult.message))
                    }
                }
                pastMessagesSuccess.set(true)
                // end::ignore[]
                if (!status.isError) {
                    for ((key, value) in result.channels) {
                        Log.i("batch_history", "Channel: $key")
                        for (message in value) {
                            Log.i("batch_history", "\tMessage: " + message.message)
                        }
                        Log.i("batch_history", "-----\n")
                    }
                }
            }
        // end::HIST-4[]
        Awaitility.await().atMost(TIMEOUT_LONG.toLong(), TimeUnit.SECONDS)
            .untilTrue(pastMessagesSuccess)
    }

    internal interface Callback {
        fun onDone()
    }

    @Throws(PubNubException::class, InterruptedException::class)
    private fun publishMessages(
        channel: String,
        count: Int,
        callback: Callback
    ) {
        for (i in 0 until count) {
            pubNub!!.publish()
                .channel(channel)
                .message("#" + (i + 1) + " " + UUID.randomUUID())
                .shouldStore(true)
                .sync()
        }
        TimeUnit.SECONDS.sleep(TIMEOUT_SHORT.toLong())
        callback.onDone()
    }
}