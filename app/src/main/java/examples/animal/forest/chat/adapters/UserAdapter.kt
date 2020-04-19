package examples.animal.forest.chat.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.chatexample.R
import examples.animal.forest.chat.pubnub.User
import java.util.*


class UserAdapter(private val channel: String) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder?>() {
    private val items: MutableList<User> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val receivedMessageView: View = layoutInflater
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(receivedMessageView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bindData(items[position])
    }

    fun update(newData: List<User>) {
        Collections.sort<User>(newData,
            Comparator<User> { o1: User, o2: User ->
                java.lang.Boolean.compare(
                    o1.isMe,
                    o2.isMe
                ) * -1
            }
        )
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(DiffCallback(newData, items))
        diffResult.dispatchUpdatesTo(this)
        items.clear()
        items.addAll(newData)
    }

    inner class UserViewHolder(itemView: View):
        RecyclerView.ViewHolder(itemView) {

        var avatar: ImageView  = itemView.findViewById(R.id.user_avatar)

        var username: TextView = itemView.findViewById(R.id.user_username)

        var status: TextView = itemView.findViewById(R.id.user_status)

        var user: User? = null

        fun bindData(user: User) {
            this.user = user
            username.text = (user.displayName)
            status.text = user.getUser()?.designation
            Glide.with(this.itemView)
                .load(user.getUser()?.profilePictureUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(avatar)
        }
    }

    internal inner class DiffCallback(
        newChats: List<User>,
        oldChats: List<User>
    ) :
        DiffUtil.Callback() {
        var newUsers: List<User> = newChats
        var oldUsers: List<User> = oldChats

        override fun areItemsTheSame(i: Int, i1: Int): Boolean {
            return oldUsers[i] == newUsers[i1]
        }

        override fun getOldListSize(): Int = oldUsers.size

        override fun getNewListSize(): Int = newUsers.size

        override fun areContentsTheSame(i: Int, i1: Int): Boolean {
            return areItemsTheSame(i, i1)
        }
    }

    override fun getItemCount(): Int = items.size
}
