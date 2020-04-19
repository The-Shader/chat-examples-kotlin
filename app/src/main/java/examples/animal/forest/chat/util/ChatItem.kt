package examples.animal.forest.chat.util

abstract class ChatItem {
    abstract val type: Int

    companion object {
        const val TYPE_OWN_HEADER_FULL = 1
        const val TYPE_OWN_HEADER_SERIES = 2
        const val TYPE_OWN_MIDDLE = 3
        const val TYPE_OWN_END = 4
        const val TYPE_REC_HEADER_FULL = 5
        const val TYPE_REC_HEADER_SERIES = 6
        const val TYPE_REC_MIDDLE = 7
        const val TYPE_REC_END = 8
        const val TYPE_DATE = 9
    }
}