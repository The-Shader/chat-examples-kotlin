package examples.animal.forest.chat.model

import android.content.res.TypedArray
import examples.animal.forest.chat.App
import com.example.chatexample.R
import java.util.*

object Users {
    private var users: MutableList<User> = mutableListOf()


    init {
        users = ArrayList()

        // addData();
        addRichData()
    }

    private fun addRichData() {
        val firstNames: Array<String> =
            App.get().resources.getStringArray(R.array.first_names)
        val lastNames: Array<String> =
            App.get().resources.getStringArray(R.array.last_names)
        val images: TypedArray = App.get().resources.obtainTypedArray(R.array.images)
        val designations: Array<String> =
            App.get().resources.getStringArray(R.array.designations)
        for (i in firstNames.indices) {
            users.add(
                User.Builder.newBuilder()
                    .firstName(firstNames[i])
                    .lastName(lastNames[i])
                    .profilePictureUrl(images.getResourceId(i, 0))
                    .designation(designations[i])
                    .uuid("forest-animal-" + (i + 1))
                    .build()
            )
        }
        images.recycle()
    }

    private fun addData() {
        users.add(
            User.Builder.newBuilder()
                .firstName("Finny")
                .lastName("Fish")
                .uuid("u-00000")
                .build()
        )
        users.add(
            User.Builder.newBuilder()
                .firstName("Daniel")
                .lastName("Dog")
                .uuid("u-00001")
                .build()
        )
        users.add(
            User.Builder.newBuilder()
                .firstName("Bernie")
                .lastName("Bear")
                .uuid("u-00002")
                .build()
        )
        users.add(
            User.Builder.newBuilder()
                .firstName("Carl")
                .lastName("Cat")
                .uuid("u-00003")
                .build()
        )
        users.add(
            User.Builder.newBuilder()
                .firstName("Uri")
                .lastName("Unicorn")
                .uuid("u-00004")
                .build()
        )
        users.add(
            User.Builder.newBuilder()
                .firstName("Monty")
                .lastName("Monkey")
                .uuid("u-00005")
                .build()
        )
        users.add(
            User.Builder.newBuilder()
                .firstName("Ollie")
                .lastName("Owl")
                .uuid("u-00006")
                .build()
        )
        users.add(
            User.Builder.newBuilder()
                .firstName("Larry")
                .lastName("Lion")
                .uuid("u-00007")
                .build()
        )
    }

    fun all(): List<User> {
        return users
    }

    fun getUserById(id: String): User {

        return users.find { user -> user.uuid == id } ?: User.Builder.newBuilder().build()
//        for (user in users) {
//            if (user.uuid == id) return user
//        }
//        return User.Builder.newBuilder().build()
    }

    class User private constructor(builder: Builder) {
        val firstName = builder.firstName
        val lastName = builder.lastName
        val uuid = builder.uuid
        val displayName = "$firstName $lastName"
        val designation = builder.designation
        val profilePictureUrl = builder.profilePictureUrl

        class Builder private constructor() {
            var firstName: String? = null
            var lastName: String? = null
            var uuid = "null"
            var profilePictureUrl: Int? = null
            var designation: String? = null
            fun firstName(firstName: String?): Builder {
                this.firstName = firstName
                return this
            }

            fun lastName(lastName: String?): Builder {
                this.lastName = lastName
                return this
            }

            fun uuid(uuid: String): Builder {
                this.uuid = uuid
                return this
            }

            fun profilePictureUrl(profilePictureUrl: Int?): Builder {
                this.profilePictureUrl = profilePictureUrl
                return this
            }

            fun designation(designation: String?): Builder {
                this.designation = designation
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
    }
}
