package it.polito.mad.cookbookcommunity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import it.polito.mad.cookbookcommunity.ui.app.CookBookCommunityRoot
import it.polito.mad.cookbookcommunity.ui.theme.CookBookCommunityTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as CookBookCommunityApp).appContainer

        setContent {
            CookBookCommunityTheme {
                CookBookCommunityRoot(
                    appContainer = appContainer,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}