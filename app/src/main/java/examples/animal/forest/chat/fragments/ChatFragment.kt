package examples.animal.forest.chat.fragments


import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.chatexample.BuildConfig
import examples.animal.forest.chat.adapters.ChatAdapter
import examples.animal.forest.chat.pubnub.History
import examples.animal.forest.chat.pubnub.Message
import examples.animal.forest.chat.services.ConnectivityListener
import examples.animal.forest.chat.util.Helper
import examples.animal.forest.chat.view.EmptyView
import examples.animal.forest.chat.view.MessageComposer
import com.example.chatexample.R
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.enums.PNOperationType
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult
import kotlinx.android.synthetic.main.fragment_chat.*
import java.util.*


class ChatFragment : ParentFragment(), MessageComposer.Listener,
    ConnectivityListener {

    lateinit var coordinatorLayout: CoordinatorLayout

    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    lateinit var chatsRecyclerView: // tag::HIS-4.1[]
            RecyclerView

    // end::HIS-4.1[]
    lateinit var messageComposer: MessageComposer

    lateinit var emptyView: EmptyView

    // tag::HIS-4.2[]
    private lateinit var chatAdapter: ChatAdapter
    private val messages: MutableList<Message> = ArrayList<Message>()

    // end::HIS-4.2[]
    private lateinit var channel: String
    private lateinit var pubNubListener: SubscribeCallback
    private val onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val firstCompletelyVisibleItemPosition: Int =
                (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            if (firstCompletelyVisibleItemPosition == History.TOP_ITEM_OFFSET && dy < 0) {

                fetchHistory()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        coordinatorLayout = coordinator
        swipeRefreshLayout = chat_swipe
        chatsRecyclerView = chat_recycler_view
        messageComposer = chats_message_composer
        emptyView = chat_empty_view
        super.onViewCreated(view, savedInstanceState)
    }


    override fun provideLayoutResourceId(): Int {
        return R.layout.fragment_chat
    }

    override fun setViewBehaviour(viewFromCache: Boolean) {
        if (!viewFromCache) {
            setHasOptionsMenu(true)
            prepareRecyclerView()
            swipeRefreshLayout.isRefreshing = true
            subscribe()
        }
    }

    private fun prepareRecyclerView() {
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshLayout.setOnRefreshListener { fetchHistory() }
        val linearLayoutManager = LinearLayoutManager(fragmentContext)
        linearLayoutManager.reverseLayout = false
        linearLayoutManager.stackFromEnd = true
        chatsRecyclerView.layoutManager = linearLayoutManager
        val dividerItemDecoration =
            DividerItemDecoration(fragmentContext, RecyclerView.VERTICAL)
        dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.chats_divider))
        chatsRecyclerView.addItemDecoration(dividerItemDecoration)
        chatsRecyclerView.itemAnimator = DefaultItemAnimator()
        chatAdapter = ChatAdapter(channel)
        chatsRecyclerView.adapter = chatAdapter
        messageComposer.setListener(this)

        // tag::HIS-5.2[]
        chatsRecyclerView.addOnScrollListener(onScrollListener)
        // end::HIS-5.2[]
    }

    // end::HIS-5.1[]
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_chat, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_chat_info -> {
                hostActivity.addFragmentToActivity(ChatInfoFragment.newInstance(channel))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setScreenTitle(): String? {
        hostActivity.enableBackButton(false)
        scrollChatToBottom()
        loadCurrentOccupancy()
        return channel
    }

    override fun extractArguments() {
        super.extractArguments()
        channel = arguments?.getString(ARGS_CHANNEL) ?: ""
    }

    override fun onReady() {
        initListener()

        // tag::FRG-2[]
        // tag::ignore[]
        /*
        // end::ignore[]
        hostActivity.getPubNub();
        // tag::ignore[]
        */
        // end::ignore[]
        // end::FRG-2[]
    }

    // tag::SUB-2[]
    private fun initListener() {
        pubNubListener = object : SubscribeCallback() {
            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                handleNewMessage(pnMessageResult)
            }

            override fun messageAction(
                pubnub: PubNub,
                pnMessageActionResult: PNMessageActionResult
            ) {
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (pnPresenceEventResult.channel == channel) {
                    val members: Int = pnPresenceEventResult.occupancy
                    runOnUiThread {
                        hostActivity.setSubtitle(
                            fragmentContext.resources
                                .getQuantityString(R.plurals.members_online, members, members)
                        )
                    }
                }
            }

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
            }

            override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {
            }

            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation === PNOperationType.PNSubscribeOperation && pnStatus.affectedChannels
                        !!.contains(channel)
                ) {
                    swipeRefreshLayout.isRefreshing = false
                    fetchHistory()
                }
            }

            override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {
            }
        }
    }

    // end::SUB-2[]
    private fun loadCurrentOccupancy() {
        hostActivity.pubNub
            .hereNow()
            .channels(listOf(channel))
            ?.async { result, status ->
                if (!status.isError) {
                    result?.let {
                        val members: Int = result.totalOccupancy
                        hostActivity.setSubtitle(
                            fragmentContext.resources
                                .getQuantityString(R.plurals.members_online, members, members)
                        )
                    }
                }
            }
    }

    // tag::MSG-1[]
    private fun handleNewMessage(message: PNMessageResult) {
        if (message.channel == channel) {
            val msg: Message = Message.serialize(message)
            messages.add(msg)
            History.chainMessages(messages, messages.size)
            runOnUiThread {
                if (emptyView.visibility == View.VISIBLE) {
                    emptyView.visibility = View.GONE
                }
                chatAdapter.update(messages)
                scrollChatToBottom()
            }
        }
    }

    // end::MSG-1[]
    // tag::SUB-1[]
    private fun subscribe() {
        hostActivity.pubNub
            .subscribe()
            .channels(listOf(channel))
            ?.withPresence()
            ?.execute()
    }

    // end::SUB-1[]
    // tag::HIS-1[]
    private fun fetchHistory() {
        if (History.isLoading) {
            return
        }
        History.isLoading = true
        swipeRefreshLayout.isRefreshing = true
        History.getAllMessages(hostActivity.pubNub, channel, earliestTimestamp,
            object : History.CallbackSkeleton() {
                override fun handleResponse(newMessages: List<Message>?) {
                    newMessages?.let {
                        when {
                            newMessages.isNotEmpty() -> {
                                messages.addAll(0, newMessages)
                                History.chainMessages(messages, messages.size)
                                runOnUiThread { chatAdapter.update(messages) }
                            }
                            messages.isEmpty() -> {
                                runOnUiThread { emptyView.visibility = View.VISIBLE }
                            }
                            else -> {
                                runOnUiThread {
                                    Toast.makeText(
                                        fragmentContext, getString(R.string.no_more_messages),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        runOnUiThread {
                            swipeRefreshLayout.isRefreshing = false
                            Log.d("new_arrival", "size: " + messages.size)
                        }
                        History.isLoading = false
                    }
                }
            })
    }

    // end::HIS-1[]
    private val earliestTimestamp: Long?
        get() = if (messages.isNotEmpty()) {
            messages[0].timetoken
        } else null

    override fun onDestroy() {
        hostActivity.pubNub.removeListener(pubNubListener)
        super.onDestroy()
    }

    // tag::SEND-2[]
    override fun onSentClick(message: String?) {
        // tag::ignore[]
        var msg = message
        if (TextUtils.isEmpty(msg)) {
            msg = if (BuildConfig.DEBUG) {
                val messageBuilder = StringBuilder("")
                messageBuilder.append(
                    UUID.randomUUID().toString().substring(0, 8)
                        .toUpperCase(Locale.US)
                )
                messageBuilder.append("\n")
                messageBuilder.append(Helper.parseDateTime(System.currentTimeMillis()))
                messageBuilder.toString()
            } else {
                return
            }
        }
        // end::ignore[]
        val finalMessage = msg
        hostActivity.pubNub
            .publish()
            .channel(channel)
            .shouldStore(true)
            .message(Message.Builder.newBuilder().text(message).build())
            .async { result, status ->
                if (!status.isError) {
                    val newMessageTimetoken: Long = result?.timetoken ?: 0
                } else {
                    val m: Message = Message.createUnsentMessage(
                        Message.Builder.newBuilder().text(finalMessage).build()
                    )
                    messages.add(m)
                    History.chainMessages(messages, messages.size)
                    runOnUiThread {
                        Runnable {
                            if (emptyView.visibility == View.VISIBLE) {
                                emptyView.visibility = View.GONE
                            }
                            chatAdapter.update(messages)
                            scrollChatToBottom()
                            Toast.makeText(
                                fragmentContext,
                                R.string.message_not_sent,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
    }

    // end::SEND-2[]
    // tag::MSG-3[]
    private fun scrollChatToBottom() {
        chatsRecyclerView.scrollToPosition(messages.size - 1)
    }

    // end::MSG-3[]
    override fun provideListener(): SubscribeCallback {
        return pubNubListener
    }

    override fun onConnected() {}

    companion object {
        private const val ARGS_CHANNEL = "ARGS_CHANNEL"
        fun newInstance(channel: String?): ChatFragment {
            val args = Bundle()
            args.putString(ARGS_CHANNEL, channel)
            val fragment = ChatFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
