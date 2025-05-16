package io.github.frankieshao.ktorsniffer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Main screen composable for the KtorSniffer UI.
 * Displays the log list, detail, or error screen based on the current UI state.
 */
@Composable
fun KtorSnifferScreen(viewModel: LogViewModel = viewModel { LogViewModel() }, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.uiType == UiType.Error) {
            LogErrorScreen(
                error = uiState.error!!
            )
        } else {
            LogListContent(
                uiState = uiState,
                onBack = onBack,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onLogSelected = viewModel::onLogItemSelected,
                onClearLogs = viewModel::onClearLogs
            )
        }

        if (uiState.uiType == UiType.Detail) {
            LogDetailScreen(
                log = uiState.selectedLog!!,
                onDismiss = viewModel::onDismissDetail
            )
        }
    }
}

/**
 * Error screen composable for displaying an error message at the top of the network logs UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogErrorScreen(error: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network Logs") }
            )
        }
    ) { paddingValues ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(error)
        }
    }
}
