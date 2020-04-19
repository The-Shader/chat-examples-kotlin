package examples.animal.forest.chat.view


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.chatexample.R


// tag::SEND-1[]
class MessageComposer(
    context: Context?,
    attrs: AttributeSet?
) :
    RelativeLayout(context, attrs) {
    private val input: EditText
    private val send: ImageView
    private val attachment: ImageView
    private lateinit var listener: Listener

    init {
        val root =
            View.inflate(getContext(), R.layout.view_message_composer, this)
        input = root.findViewById(R.id.composer_edittext)
        send = root.findViewById(R.id.composer_send)
        attachment = root.findViewById(R.id.composer_attachment)
        send.setOnClickListener { v: View? ->
            listener.onSentClick(input.text.toString().trim { it <= ' ' })
            input.setText("")
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun onSentClick(message: String?)
    }
}
// end::SEND-1[]
