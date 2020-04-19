package examples.animal.forest.chat.view


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.example.chatexample.R

class ProgressView : RelativeLayout {

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.view_progress, this)
    }

    private fun show() {
        this.visibility = View.VISIBLE
    }

    private fun hide() {
        this.visibility = View.GONE
    }

    override fun setEnabled(enable: Boolean) {
        if (enable) show() else hide()
    }
}
