package examples.animal.forest.chat.tests

import com.pubnub.api.enums.PNOperationType
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import java.text.SimpleDateFormat
import java.util.*

object PnUtils {
    fun isSubscribed(status: PNStatus, channel: String?): Boolean {
        return (status.operation == PNOperationType.PNSubscribeOperation
                && status.affectedChannels!!.contains(channel))
    }

    fun isSubscribedGroup(status: PNStatus, group: String?): Boolean {
        return (status.operation == PNOperationType.PNSubscribeOperation
                && status.affectedChannelGroups!!.contains(group))
    }

    fun isUnsubscribed(
        status: PNStatus,
        channel: String?,
        uuid: String
    ): Boolean {
        return (status.operation == PNOperationType.PNUnsubscribeOperation && status.affectedChannels!!.contains(
            channel
        )
                && status.uuid == uuid)
    }

    fun isUnsubscribedGroup(
        status: PNStatus,
        group: String?,
        uuid: String
    ): Boolean {
        return (status.operation == PNOperationType.PNUnsubscribeOperation && status.affectedChannelGroups!!.contains(
            group
        )
                && status.uuid == uuid)
    }

    fun checkPresence(
        presence: PNPresenceEventResult,
        uuid: String,
        event: String,
        channel: String
    ): Boolean {
        return presence.event == event && presence.uuid == uuid && presence.channel == channel
    }

    fun printStatus(status: PNStatus) {
        val logBuilder = StringBuilder("")
        logBuilder.append("Operation: ").append(status.operation).append("\n")
        logBuilder.append("Category: ").append(status.category).append("\n")
        logBuilder.append("UUID: ").append(status.uuid).append("\n")
        logBuilder.append("Channels: ")
            .append(Arrays.toString(arrayOf<List<*>?>(status.affectedChannels)))
            .append("\n")
        logBuilder.append("Groups: ")
            .append(Arrays.toString(arrayOf<List<*>?>(status.affectedChannelGroups)))
            .append("\n")
        println(logBuilder.toString())
    }

    fun printMessageMeta(message: PNMessageResult) {
        val logBuilder = StringBuilder("")
        logBuilder.append("Channel: ").append(message.channel).append("\n")
        logBuilder.append("Subscription: ").append(message.subscription).append("\n")
        logBuilder.append("Publisher: ").append(message.publisher).append("\n")
        logBuilder.append("Content: ").append(message.message).append("\n")
        logBuilder.append("Timetoken: ").append(message.timetoken).append("\n")
        println(logBuilder.toString())
    }

    fun printPresence(presence: PNPresenceEventResult) {
        val logBuilder = StringBuilder("")
        logBuilder.append("UUID: ").append(presence.uuid).append("\n")
        logBuilder.append("Event: ").append(presence.event).append("\n")
        logBuilder.append("Channel: ").append(presence.channel).append("\n")
        println(logBuilder.toString())
    }

    fun parseTimetoken(timetoken: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss dd MMM YYYY")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timetoken / 10000L
        return sdf.format(calendar.time)
    }
}