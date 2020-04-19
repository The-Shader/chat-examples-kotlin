package examples.animal.forest.chat.pubnub


import com.google.common.base.Objects
import examples.animal.forest.chat.model.Users
import examples.animal.forest.chat.prefs.Prefs


class User private constructor(builder: Builder) {
    private var user: Users.User? = builder.user
    var status: String? = builder.status
        private set
    var displayName: String? = null
        private set

    init {
        status += "Available"
        displayName = user?.displayName
        if (isMe) {
            displayName += " (You)"
        }
    }

    class Builder private constructor() {
        internal var user: Users.User? = null
        var status: String? = null
        fun user(user: Users.User?): Builder {
            this.user = user
            return this
        }

        fun status(status: String?): Builder {
            this.status = status
            return this
        }

        fun build(): User {
            return User(this)
        }

        companion object {
            fun newBuilder(): Builder {
                return Builder()
            }
        }
    }

    fun getUser(): Users.User? {
        return user
    }

    val isMe: Boolean
        get() = Prefs.get()?.uuid().equals(user?.uuid)

    override fun equals(other: Any?): Boolean {
        return if (other is User) {
            other.user?.uuid.equals(user?.uuid)
        } else false
    }

    override fun hashCode(): Int {
        return Objects.hashCode(this)
    }
}
