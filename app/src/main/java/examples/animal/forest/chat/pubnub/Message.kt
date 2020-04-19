package examples.animal.forest.chat.pubnub


import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pubnub.api.models.consumer.history.PNHistoryItemResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import examples.animal.forest.chat.model.Users
import examples.animal.forest.chat.prefs.Prefs
import examples.animal.forest.chat.util.ChatItem
import examples.animal.forest.chat.util.Helper


// tag::BIND-3[]
class Message : ChatItem {
    var senderId: String? = null
        private set
    var text: String? = null
        private set

    /**
     * Formatted timestamp
     */
    @Transient
    var timestamp: String? = null
        private set

    @Transient
    var timetoken: Long = 0
        private set

    @Transient
    var isOwnMessage = false
        private set

    /**
     * On of the six possible view types
     */
    @Transient
    override var type = 0

    /**
     * Key for grouping messages by their timetoken.
     */
    @Transient
    var key: Long? = null
        private set

    /**
     * Message owner.
     */
    @Transient
    private var user: Users.User? = null

    // tag::ignore[]
    /*
// end::ignore[]
}
// tag::ignore[]
*/
    // end::ignore[]
    // end::BIND-3[]
    @Transient
    var isSent = true

    /**
     * Disable instance creation via constructor.
     * Use the `newBuilder` method instead.
     */
    private constructor() {}

    /**
     * Adopts the builder pattern and it's used to create instances.
     */
    class Builder private constructor() {
        internal var text: String? = null
        internal var timetoken: Long = 0
        fun text(text: String?): Builder {
            this.text = text
            return this
        }

        fun timetoken(timetoken: Long): Builder {
            this.timetoken = timetoken
            return this
        }

        fun build(): JsonObject {
            return Message(this).generate()
        }

        companion object {
            fun newBuilder(): Builder {
                return Builder()
            }
        }
    }

    private constructor(builder: Builder) {
        senderId = Prefs.get()?.uuid()
        text = builder.text
        timetoken = builder.timetoken
        initializeCustomProperties()
    }

    private fun initializeCustomProperties() {
        isOwnMessage = Prefs.get()!!.uuid().equals(senderId)
        timestamp = Helper.parseTime(timetoken / TIMESTAMP_DIVIDER)
        user = Users.getUserById(senderId!!)
        key = Helper.trimTime(timetoken / TIMESTAMP_DIVIDER)
        type = if (isOwnMessage) TYPE_OWN_HEADER_FULL else TYPE_REC_HEADER_FULL
    }

    private fun generate(): JsonObject {
        val json: String = Gson().toJson(this)
        val payload: JsonObject = JsonParser().parse(json).asJsonObject
        if (timetoken != 0L) {
            // if editing an existing message, pass it's timetoken within the payload
            payload.addProperty("timetoken", timetoken)
        }
        return payload
    }

    fun getUser(): Users.User? {
        return user
    }

    companion object {
        private const val TIMESTAMP_DIVIDER = 10000L

        fun serialize(pnHistoryItemResult: PNHistoryItemResult): Message {
            val message: Message =
                Gson().fromJson(pnHistoryItemResult.getEntry(), Message::class.java)
            message.timetoken = pnHistoryItemResult.getTimetoken()
            message.initializeCustomProperties()
            return message
        }

        // tag::MSG-2[]
        fun serialize(pnMessageResult: PNMessageResult): Message {
            val message: Message =
                Gson().fromJson(pnMessageResult.message, Message::class.java)
            message.timetoken = pnMessageResult.timetoken
            message.initializeCustomProperties()
            return message
        }

        // end::MSG-2[]
        fun createUnsentMessage(jsonObject: JsonObject?): Message {
            val message: Message = Gson().fromJson(jsonObject, Message::class.java)
            message.timetoken =
                System.currentTimeMillis() * TIMESTAMP_DIVIDER
            message.initializeCustomProperties()
            message.isSent = false
            return message
        }
    }
}
