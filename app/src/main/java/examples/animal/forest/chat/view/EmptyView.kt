package examples.animal.forest.chat.view


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.example.chatexample.R


class EmptyView(context: Context, attrs: AttributeSet?) :
    RelativeLayout(context, attrs) {
    private val title: TextView
    private val message: TextView
    private var titleResId: Int
    private var messageResId: Int

    init {
        val array =
            context.theme.obtainStyledAttributes(attrs, R.styleable.EmptyView, 0, 0)
        titleResId = array.getResourceId(R.styleable.EmptyView_evTitle, R.string.ops)
        messageResId = array.getResourceId(R.styleable.EmptyView_evMessage, 0)
        array.recycle()
        val root =
            View.inflate(getContext(), R.layout.view_empty, this)
        title = root.findViewById(R.id.empty_title)
        message = root.findViewById(R.id.empty_message)
        title.setText(titleResId)
        if (messageResId != 0) {
            message.setText(messageResId)
        }
    }

    fun setTitle(@StringRes resId: Int) {
        titleResId = resId
        title.setText(titleResId)
    }

    fun setMessage(@StringRes resId: Int) {
        messageResId = resId
        message.setText(messageResId)
    }

    fun setTitle(title: String?) {
        this.title.text = title
    }

    fun setMessage(message: String?) {
        this.message.text = message
    }
}
