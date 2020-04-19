package examples.animal.forest.chat.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


object AndroidUtils {
    /**
     * Hides the soft-keyboard.
     *
     * @param view    The view which currently has the focus.
     * @param context The view context.
     */
    fun hideKeyboard(view: View, context: Context?) {
        if (context != null) {
            val inputMethodManager =
                context.getSystemService(
                    Activity.INPUT_METHOD_SERVICE
                ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun emphasizeText(text: String): String {
        return "<b>$text</b>"
    }

    fun newLine(): String {
        return "<br>"
    }
}
