package com.divifriends.xyon.pro

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.divifriends.xyon.pro.ui.DiviAppRoot
import com.divifriends.xyon.pro.ui.DiviViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        val repository = (application as DiviFriendsApp).repository
        setContent {
            val viewModel: DiviViewModel = viewModel(factory = DiviViewModel.factory(repository))
            DiviAppRoot(viewModel = viewModel)
        }
    }
}
