package examples.animal.forest.chat.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import examples.animal.forest.chat.adapters.UserAdapter
import examples.animal.forest.chat.model.Users
import examples.animal.forest.chat.pubnub.User
import examples.animal.forest.chat.util.PNGlideModule
import examples.animal.forest.chat.view.EmptyView
import com.bumptech.glide.request.RequestOptions
import com.example.chatexample.R
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult
import kotlinx.android.synthetic.main.fragment_chat_info.*
import java.util.*


class ChatInfoFragment : ParentFragment() {

    lateinit var imageView: ImageView

    lateinit var description: TextView

    lateinit var usersRecyclerView: RecyclerView

    lateinit var emptyView: EmptyView
    lateinit var userAdapter: UserAdapter
    var users: MutableList<User> = ArrayList<User>()
    private var channel: String = ""
    private var pubNubListener: SubscribeCallback = object : SubscribeCallback() {

        override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {
            TODO("Not yet implemented")
        }

        override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {}

        override fun messageAction(
            pubnub: PubNub,
            pnMessageActionResult: PNMessageActionResult
        ) {}

        override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
            if (pnPresenceEventResult.uuid == null) {
                return
            }
            when (pnPresenceEventResult.event) {
                "join" -> users.add(
                    User.Builder.newBuilder().user(Users.getUserById(pnPresenceEventResult.uuid)).build()
                )
                "leave", "timeout" -> users.remove(
                    User.Builder.newBuilder().user(Users.getUserById(pnPresenceEventResult.uuid)).build()
                )
                "interval" -> {
                    for (uuid in pnPresenceEventResult.join) {
                        users.add(User.Builder.newBuilder().user(Users.getUserById(uuid)).build())
                    }
                    for (uuid in pnPresenceEventResult.leave) {
                        users.remove(User.Builder.newBuilder().user(Users.getUserById(uuid)).build())
                    }
                    for (uuid in pnPresenceEventResult.timeout) {
                        users.remove(User.Builder.newBuilder().user(Users.getUserById(uuid)).build())
                    }
                }
                "state-change" -> {
                }
            }
            handleUiVisibility()
            runOnUiThread { userAdapter.update(users) }
        }

        override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}

        override fun space(pubnub: PubNub, pnSpaceResult: PNSpaceResult) {}

        override fun status(pubnub: PubNub, status: PNStatus) {}

        override fun user(pubnub: PubNub, pnUserResult: PNUserResult) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        imageView = info_image
        description = info_description
        usersRecyclerView = info_recycler_view
        emptyView = info_empty_view

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onReady() {}

    override fun provideLayoutResourceId(): Int {
        return R.layout.fragment_chat_info
    }

    override fun setViewBehaviour(viewFromCache: Boolean) {
        usersRecyclerView.layoutManager = LinearLayoutManager(fragmentContext)
        usersRecyclerView.itemAnimator = DefaultItemAnimator()
        userAdapter = UserAdapter(channel)
        usersRecyclerView.adapter = userAdapter
        Glide.with(fragmentContext)
            .load(R.drawable.chat_logo)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
        fetchAvailableUsers()
    }

    override fun setScreenTitle(): String? {
        hostActivity.enableBackButton(true)
        return channel
    }

    // end::LIS-1[]
    @SuppressLint("WrongConstant")
    private fun handleUiVisibility() {
        var viewState = -1
        if (users.size > 0) {
            if (emptyView.visibility != View.GONE) viewState =
                View.GONE
        } else {
            if (emptyView.visibility != View.VISIBLE) viewState =
                View.VISIBLE
        }
        if (viewState != -1) {
            val finalViewState = viewState
            runOnUiThread { emptyView.visibility = finalViewState }
        }
    }

    override fun extractArguments() {
        super.extractArguments()
        channel = arguments?.getString(ARGS_CHANNEL) ?: ""
    }

    // tag::ONL-1[]
    private fun fetchAvailableUsers() {
        hostActivity.pubNub
            .hereNow()
            .channels(listOf(channel))
            .includeUUIDs(true)
            .includeState(true)
            .async { result, status ->
                if (!status.isError && result != null) {
                    users.clear()
                    val hereNowChannelData: PNHereNowChannelData? =
                        result.channels[channel]
                    hereNowChannelData?.let {
                        it.occupants.map { occupant ->
                            users.add(
                                User.Builder.newBuilder()
                                    .user(Users.getUserById(occupant.uuid))
                                    .build()
                            )
                        }
                    }
                    userAdapter.update(users)
                    val totalOccupancy: Int? = hereNowChannelData?.occupancy
                }
            }
    }

    // end::ONL-1[]
    override fun provideListener(): SubscribeCallback {
        return pubNubListener
    }

    companion object {
        private const val ARGS_CHANNEL = "ARGS_CHANNEL"
        fun newInstance(channel: String?): ChatInfoFragment {
            val args = Bundle()
            args.putString(ARGS_CHANNEL, channel)
            val fragment = ChatInfoFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
