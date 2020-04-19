package examples.animal.forest.chat.pubnub


import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_OWN_END
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_OWN_HEADER_FULL
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_OWN_HEADER_SERIES
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_OWN_MIDDLE
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_REC_END
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_REC_HEADER_FULL
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_REC_HEADER_SERIES
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_REC_MIDDLE
import java.util.concurrent.TimeUnit


internal object MessageHelper {
    private const val HEADER_FULL = 10
    private const val HEADER = 20
    private const val MIDDLE = 30
    private const val END = 40
    fun chain(currentMsg: Message, previousMsg: Message) {
        val diffToPrev: Long =
            (currentMsg.timetoken - previousMsg.timetoken) / 10000L
        val offset = TimeUnit.MINUTES.toMillis(1)
        val ownMessage: Boolean =
            previousMsg.getUser()?.uuid == currentMsg.getUser()?.uuid
        var chainable = false
        if (ownMessage) chainable = diffToPrev <= offset
        if (ownMessage) {
            if (chainable) {
                currentMsg.type = assignType(currentMsg, END)
                if (isTypeOf(previousMsg, HEADER_FULL)) {
                    previousMsg.type = assignType(previousMsg, HEADER)
                } else if (isTypeOf(previousMsg, END)) {
                    previousMsg.type = assignType(previousMsg, MIDDLE)
                }
            } else {
                currentMsg.type = assignType(currentMsg, HEADER_FULL)
                if (!isTypeOf(previousMsg, HEADER_FULL)) {
                    previousMsg.type = assignType(previousMsg, END)
                }
            }
        } else {
            currentMsg.type = assignType(currentMsg, HEADER_FULL)
        }
    }

    private fun isTypeOf(instance: Message, type: Int): Boolean {
        if (type == HEADER_FULL) {
            return instance.type == TYPE_OWN_HEADER_FULL || instance.type == TYPE_REC_HEADER_FULL
        }
        if (type == HEADER) {
            return instance.type == TYPE_OWN_HEADER_SERIES || instance.type == TYPE_REC_HEADER_SERIES
        }
        if (type == MIDDLE) {
            return instance.type == TYPE_OWN_MIDDLE || instance.type == TYPE_REC_MIDDLE
        }
        return if (type == END) {
            instance.type == TYPE_OWN_END || instance.type == TYPE_REC_END
        } else false
    }

    private fun assignType(instance: Message, type: Int): Int {
        if (type == HEADER_FULL) {
            return if (instance.isOwnMessage) TYPE_OWN_HEADER_FULL else TYPE_REC_HEADER_FULL
        }
        if (type == HEADER) {
            return if (instance.isOwnMessage) TYPE_OWN_HEADER_SERIES else TYPE_REC_HEADER_SERIES
        }
        if (type == MIDDLE) {
            return if (instance.isOwnMessage) TYPE_OWN_MIDDLE else TYPE_REC_MIDDLE
        }
        return if (type == END) {
            if (instance.isOwnMessage) TYPE_OWN_END else TYPE_REC_END
        } else -1
    }
}
