package tr.hizmet.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tr.hizmet.app.ui.HizmetNavHost
import tr.hizmet.app.ui.login.LoginScreen
import tr.hizmet.app.ui.theme.HizmetTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = (application as HizmetApp).session

        setContent {
            HizmetTheme {
                val isLoggedIn by session.isLoggedIn.collectAsStateWithLifecycle()
                if (isLoggedIn) {
                    HizmetNavHost()
                } else {
                    LoginScreen()
                }
            }
        }
    }
}
