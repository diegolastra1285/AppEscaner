package com.escaner.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.escaner.app.ui.navigation.AppNavigation
import com.escaner.app.ui.theme.EscanerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EscanerTheme {
                AppNavigation()
            }
        }
    }
}
