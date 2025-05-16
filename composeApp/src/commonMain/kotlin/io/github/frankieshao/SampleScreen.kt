package io.github.frankieshao

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * The main sample screen for demonstrating network requests and navigation.
 * Provides buttons to trigger GET, POST (JSON), and POST (Protobuf) requests, and to navigate to the network log screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleScreen(onNavToNetworkLog: () -> Unit) {
    val scope = rememberCoroutineScope()
    var responseText by remember { mutableStateOf("") }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "KtorSniffer Sample") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(0.7f),
                onClick = onNavToNetworkLog
            ) {
                Text("Network Log")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(0.7f),
                onClick = {
                    scope.launch {
                        try {
                            responseText = get()
                        } catch (e: Exception) {
                            responseText = "Error: ${e.message}"
                            e.printStackTrace()
                        }
                    }
                }
            ) {
                Text("Get Test - Json")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(0.7f),
                onClick = {
                    scope.launch {
                        try {
                            val result = post()
                            responseText = result
                        } catch (e: Exception) {
                            responseText = "Error: ${e.message}"
                            e.printStackTrace()
                        }
                    }
                }
            ) {
                Text("Post Test - Json")
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Button to trigger a POST request with Protobuf
            Button(
                modifier = Modifier.fillMaxWidth(0.7f),
                onClick = {
                    scope.launch {
                        try {
                            val result = postProto()
                            responseText = result
                        } catch (e: Exception) {
                            responseText = "Error: ${e.message}"
                            e.printStackTrace()
                        }
                    }
                }
            ) {
                Text("Post Test - Protobuf")
            }
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    text = responseText
                )
            }
        }
    }
}