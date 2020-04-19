package examples.animal.forest.chat.pubnub


import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.PNCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.history.PNHistoryItemResult
import com.pubnub.api.models.consumer.history.PNHistoryResult
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


object History {
    const val TOP_ITEM_OFFSET = 3
    private const val CHUNK_SIZE = 20
    private val LOADING =
        AtomicBoolean(false)

    // tag::HIS-2[]
    fun getAllMessages(
        pubNub: PubNub, channel: String?, start: Long?,
        callback: CallbackSkeleton
    ) {
        pubNub.history()
            .channel(channel) // where to fetch history from
            .count(CHUNK_SIZE) // how many items to fetch
            .start(start) // where to start
            .includeTimetoken(true)
            .async { result, status ->
                Thread(Runnable {
                    if (!status.isError && result != null && result.messages.isNotEmpty()) {
                        val messages: MutableList<Message> =
                            ArrayList<Message>()
                        for (message in result.messages) {
                            val msg: Message = Message.serialize(message)
                            messages.add(msg)
                        }
                        callback.handleResponse(messages)
                    } else {
                        callback.handleResponse(listOf())
                    }
                }).start()
            }
    }

    // end::HIS-2[]
    // tag::HIS-3[]
    fun chainMessages(list: List<Message>, count: Int) {
        var limit = count
        if (limit > list.size) {
            limit = list.size
        }
        for (i in 0 until limit) {
            val message: Message = list[i]
            if (i > 0) {
                MessageHelper.chain(message, list[i - 1])
            }
        }
    }

    /*public static HashMap<Integer, ViewType> groupMessages(List<Message> list) {
        HashMap<Long, List<Message>> hashMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            Message message = list.get(i);
            Long key = message.getKey();
            if (hashMap.containsKey(key)) {
                hashMap.get(key).add(message);
            } else {
                ArrayList<Message> messages = new ArrayList<>();
                messages.add(message);
                hashMap.put(key, messages);
            }
        }

        HashMap<Integer, ViewType> flatMap = new HashMap<>();

        int total = 0;
        for (Map.Entry<Long, List<Message>> entry : hashMap.entrySet()) {
            Long key = entry.getKey();
            ViewType keyViewType = new ViewType(flatMap.size(), ChatAdapter.TYPE_DATE);
            flatMap.put(flatMap.size(), keyViewType);
            for (int i = 0; i < entry.getValue().size(); i++) {
                total++;
                int index = total;
                int type = -1;

                if (entry.getValue().get(i).isOwnMessage()) {
                    switch (entry.getValue().get(i).getSuccessivenessType()) {
                        case Message.TYPE_HEADER:
                            type = TYPE_OWN_HEADER_SERIES;
                            break;
                        case Message.TYPE_MIDDLE:
                            type = TYPE_OWN_MIDDLE;
                            break;
                        case Message.TYPE_END:
                            type = TYPE_OWN_END;
                            break;
                    }
                } else {
                    switch (entry.getValue().get(i).getSuccessivenessType()) {
                        case Message.TYPE_HEADER:
                            type = TYPE_REC_HEADER_SERIES;
                            break;
                        case Message.TYPE_MIDDLE:
                            type = TYPE_REC_MIDDLE;
                            break;
                        case Message.TYPE_END:
                            type = TYPE_REC_END;
                            break;
                    }
                }

                ViewType messageViewType = new ViewType(index, type);
                flatMap.put(flatMap.size(), messageViewType);
            }
        }

        return flatMap;
    }*/
    // end::HIS-3[]
    var isLoading: Boolean
        get() = LOADING.get()
        set(loading) {
            LOADING.set(loading)
        }

    abstract class CallbackSkeleton {
        abstract fun handleResponse(messages: List<Message>?)
    }
}
