package examples.animal.forest.chat.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import examples.animal.forest.chat.util.PNFragmentImpl
import examples.animal.forest.chat.util.ParentActivityImpl


abstract class ParentFragment : Fragment(),
    PNFragmentImpl {

    private val TAG = "PF_" + javaClass.simpleName
    lateinit var fragmentContext: Context
    private var isFromCache = false
    private var rootView: View? = null
    abstract fun provideLayoutResourceId(): Int
    abstract fun setViewBehaviour(viewFromCache: Boolean)
    abstract fun setScreenTitle(): String?
    abstract fun onReady()
    open fun extractArguments() {}

    // tag::FRG-1.1[]
    lateinit var hostActivity // field of fragment
            : ParentActivityImpl

    // end::FRG-1.1[]
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        if (rootView != null) {
            isFromCache = true
            return rootView
        } else {
            rootView = view
            if (rootView == null) {
                rootView = inflater.inflate(provideLayoutResourceId(), container, false)
                isFromCache = false
            } else {
                isFromCache = true
            }
        }
        setHasOptionsMenu(true)
        Log.d(TAG, "onCreateView $isFromCache")
        return rootView
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "setViewBehaviour, cache: $isFromCache")
        setViewBehaviour(isFromCache)
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
        hostActivity.setTitle(setScreenTitle())
    }

    // tag::FRG-5.1[]
    override fun onCreate(savedInstanceState: Bundle?) {
        // tag::ignore[]
        Log.d(TAG, "onCreate")
        // end::ignore[]
        super.onCreate(savedInstanceState)
        // tag::ignore[]
        Log.d(TAG, "onReady")
        onReady()
        // end::ignore[]
        hostActivity.pubNub.addListener(provideListener())
        // tag::ignore[]
        if (arguments != null) {
            extractArguments()
        }
        // end::ignore[]
    }

    // end::FRG-5.1[]
    // tag::FRG-1.2[]
    override fun onAttach(context: Context) {
        // tag::ignore[]
        Log.d(TAG, "onAttach")
        // end::ignore[]
        super.onAttach(context)
        // tag::ignore[]
        fragmentContext = context
        // end::ignore[]
        hostActivity = context as ParentActivityImpl
    }

    // end::FRG-1.2[]
    override fun onDetach() {
        Log.d(TAG, "onDetach")
        rootView = null
        super.onDetach()
    }

    // tag::FRG-5.2[]
    override fun onDestroy() {
        // tag::ignore[]
        Log.d(TAG, "onDestroy")
        // end::ignore[]
        hostActivity.pubNub.removeListener(provideListener())
        super.onDestroy()
    }

    // end::FRG-5.2[]
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView")
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    fun runOnUiThread(runnable: () -> Unit) {
        (fragmentContext as Activity?)!!.runOnUiThread(runnable)
    }
}
