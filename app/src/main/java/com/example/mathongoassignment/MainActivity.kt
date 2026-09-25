package com.example.mathongoassignment

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mathongoassignment.ui.attempt.AttemptScreen
import com.example.mathongoassignment.ui.attempt.AttemptViewModel
import com.example.mathongoassignment.ui.richtext.ContentWebViewPool
import com.example.mathongoassignment.ui.richtext.LocalContentWebViewPool
import com.example.mathongoassignment.ui.theme.MathOnGoAssignmentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The header is dark in both themes, so the status bar always uses light icons.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            MathOnGoAssignmentTheme {
                val webViewPool = remember { ContentWebViewPool(this) }
                DisposableEffect(webViewPool) {
                    onDispose { webViewPool.destroy() }
                }

                CompositionLocalProvider(LocalContentWebViewPool provides webViewPool) {
                    val viewModel: AttemptViewModel = viewModel(factory = AttemptViewModel.Factory)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    AttemptScreen(
                        state = state,
                        onAction = viewModel::onAction,
                        onBack = { onBackPressedDispatcher.onBackPressed() },
                    )
                }
            }
        }
    }
}
