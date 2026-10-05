package com.example.gridlink.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.gridlink.app.ui.navigation.AppNavigation
import com.example.gridlink.app.ui.theme.GridLinkTheme

import android.util.Log

class MainActivity : ComponentActivity() {
    private val screenKeyMutable = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val key = intent.getStringExtra("screen")
        Log.d("GridLinkNav", "onCreate screen: $key")
        screenKeyMutable.value = key
        setContent {
            GridLinkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val currentKey = screenKeyMutable.value
                    Log.d("GridLinkNav", "setContent AppNavigation currentKey: $currentKey")
                    AppNavigation(initialScreenKey = currentKey)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val key = intent.getStringExtra("screen")
        Log.d("GridLinkNav", "onNewIntent screen: $key")
        screenKeyMutable.value = key
    }
}