package io.github.frankieshao

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.frankieshao.ktorsniffer.ui.KtorSnifferScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import io.ktor.client.engine.HttpClientEngineFactory

/**
 * Returns the default HttpClientEngineFactory for the current platform.
 * The actual implementation is provided in each platform module.
 */
expect fun getDefaultEngine(): HttpClientEngineFactory<*>

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "Sample") {
            composable("Sample") {
                SampleScreen {
                    navController.navigate("KtorSniff")
                }
            }
            composable("KtorSniff") {
                KtorSnifferScreen {
                    navController.popBackStack()
                }
            }
        }
    }
}
