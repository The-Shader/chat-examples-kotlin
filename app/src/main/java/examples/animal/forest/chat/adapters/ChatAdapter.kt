package examples.animal.forest.chat.adapters

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatexample.BuildConfig
import com.example.chatexample.R
import examples.animal.forest.chat.pubnub.Message
import examples.animal.forest.chat.util.AndroidUtils
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_OWN_END
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_OWN_HEADER_FULL
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_OWN_HEADER_SERIES
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_OWN_MIDDLE
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_REC_END
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_REC_HEADER_FULL
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_REC_HEADER_SERIES
import examples.animal.forest.chat.util.ChatItem.Companion.TYPE_REC_MIDDLE
import examples.animal.forest.chat.util.Helper
import java.util.*

class ChatAdapter(private val channel: String) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder?>() {

    // tag::BIND-2[]
    private val items: MutableList<Message>

    // end::BIND-2[]
    init {
        items = ArrayList<Message>()
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    // tag::BIND-1[]
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            TYPE_OWN_HEADER_FULL, TYPE_OWN_HEADER_SERIES, TYPE_OWN_MIDDLE, TYPE_OWN_END -> {
                val sentMessageView = layoutInflater
                    .inflate(R.layout.item_message_sent, parent, false)
                MessageViewHolder(sentMessageView, viewType)
            }
            TYPE_REC_HEADER_FULL, TYPE_REC_HEADER_SERIES, TYPE_REC_MIDDLE, TYPE_REC_END -> {
                val receivedMessageView = layoutInflater
                        .inflate(R.layout.item_message_received, parent, false)
                MessageViewHolder(receivedMessageView, viewType)
            }
            else -> throw IllegalStateException("No applicable viewtype found.")
        }
    }

    // end::BIND-1[]
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bindData(items[position])
    }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int): Long {
        return items[position].timetoken
    }

    // tag::BIND-4[]
    fun update(newData: List<Message>?) {
        val diffResult: DiffUtil.DiffResult =
            DiffUtil.calculateDiff(DiffCallback(newData!!, items))
        diffResult.dispatchUpdatesTo(this)
        items.clear()
        items.addAll(newData)
    }

    // end::BIND-4[]
    internal inner class DateViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val dateTextView: TextView = itemView.findViewById(R.id.item_date)

        fun bindData(key: Long?) {
            key?.let {
                dateTextView.text = Helper.parseDateTime(key)
            }
        }
    }

    inner class MessageViewHolder(itemView: View, private val type: Int) :
        RecyclerView.ViewHolder(itemView) {

        var root: RelativeLayout = itemView.findViewById(R.id.root)
        var avatar: ImageView = itemView.findViewById(R.id.message_avatar)
        var sender: TextView  = itemView.findViewById(R.id.message_sender)
        var bubble: TextView = itemView.findViewById(R.id.message_bubble)
        var timestamp: TextView = itemView.findViewById(R.id.message_timestamp)

        init {
            root.setOnClickListener { view ->
                if (BuildConfig.DEBUG) {
                    message?.let {
                        showMessageInfoDialog(view.context, it)
                    }
                }
            }
        }

        var message: Message? = null
        fun bindData(message: Message?) {
            this.message = message
            handleType()
            bubble.text = this.message?.text
            sender.text = this.message?.getUser()?.displayName
            timestamp.text = this.message?.timestamp
            Glide.with(this.itemView)
                .load(this.message?.getUser()?.profilePictureUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(avatar)
            this.message?.let {
                if (it.isSent) {
                    bubble.alpha = 1.0f
                } else {
                    bubble.alpha = 0.5f
                }
            }

        }

        private fun handleType() {
            when (type) {
                TYPE_OWN_HEADER_FULL, TYPE_REC_HEADER_FULL -> {
                    avatar.visibility = View.VISIBLE
                    sender.visibility = View.VISIBLE
                    timestamp.visibility = View.VISIBLE
                }
                TYPE_OWN_HEADER_SERIES, TYPE_REC_HEADER_SERIES -> {
                    avatar.visibility = View.VISIBLE
                    sender.visibility = View.VISIBLE
                    timestamp.visibility = View.GONE
                }
                TYPE_OWN_MIDDLE, TYPE_REC_MIDDLE -> {
                    avatar.visibility = View.INVISIBLE
                    sender.visibility = View.GONE
                    timestamp.visibility = View.GONE
                }
                TYPE_OWN_END, TYPE_REC_END -> {
                    avatar.visibility = View.INVISIBLE
                    sender.visibility = View.GONE
                    timestamp.visibility = View.VISIBLE
                }
            }
        }
    }

    internal inner class DiffCallback(
        private var newMessages: List<Message>,
        private var oldMessages: List<Message>
    ) :
        DiffUtil.Callback() {

        override fun getNewListSize() = newMessages.size

        override fun getOldListSize() = oldMessages.size

        override fun areItemsTheSame(i: Int, i1: Int): Boolean {
            return oldMessages[i].timetoken == newMessages[i1].timetoken
        }

        override fun areContentsTheSame(i: Int, i1: Int): Boolean {
            val type =
                oldMessages[i].type == newMessages[i1].type
            val sent = oldMessages[i].isSent == newMessages[i1].isSent
            return type && sent
        }

    }

    private fun showMessageInfoDialog(
        context: Context,
        message: Message
    ) {
        val contentBuilder = StringBuilder("")
        contentBuilder.append(AndroidUtils.emphasizeText("Sender: "))
        contentBuilder.append(message.senderId)
        contentBuilder.append(AndroidUtils.newLine())
        contentBuilder.append(AndroidUtils.emphasizeText("Date time: "))
        contentBuilder.append(Helper.parseDateTime(message.timetoken / 10000L))
        contentBuilder.append(AndroidUtils.newLine())
        contentBuilder.append(AndroidUtils.emphasizeText("Relative: "))
        contentBuilder.append(Helper.getRelativeTime(message.timetoken / 10000L))
        contentBuilder.append(AndroidUtils.newLine())
        contentBuilder.append(AndroidUtils.emphasizeText("Own message: "))
        contentBuilder.append(message.isOwnMessage)
        contentBuilder.append(AndroidUtils.newLine())
        contentBuilder.append(AndroidUtils.emphasizeText("Type: "))
        contentBuilder.append(message.type)
        contentBuilder.append(AndroidUtils.newLine())
        contentBuilder.append(AndroidUtils.emphasizeText("Is sent: "))
        contentBuilder.append(message.isSent)
        val materialDialog: MaterialDialog = MaterialDialog.Builder(context)
            .title(R.string.message_info)
            .content(Html.fromHtml(contentBuilder.toString()))
            .positiveText(android.R.string.ok)
            .build()
        materialDialog.show()
    }
}
