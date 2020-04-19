package examples.animal.forest.chat


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.chatexample.R


abstract class ParentActivity : AppCompatActivity() {
    protected abstract fun provideLayoutResourceId(): Int
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(provideLayoutResourceId())
    }

    protected fun addFragment(fragment: Fragment) {
        val manager = supportFragmentManager
        val ft = manager.beginTransaction()
        setupAnimations(ft)
        ft.replace(R.id.container, fragment)
        ft.addToBackStack(fragment.javaClass.simpleName)
        ft.commit()
    }

    private fun setupAnimations(ft: FragmentTransaction) {
        /*ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim
                .exit_to_right);*/
        ft.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
        )
    }
}