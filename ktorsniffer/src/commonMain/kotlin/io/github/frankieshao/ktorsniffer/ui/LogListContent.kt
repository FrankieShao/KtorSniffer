package io.github.frankieshao.ktorsniffer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.frankieshao.ktorsniffer.model.NetworkLogSummary
import io.ktor.http.ContentType
import ktorsniff.ktorsniffer.generated.resources.Res
import ktorsniff.ktorsniffer.generated.resources.arrow_back
import ktorsniff.ktorsniffer.generated.resources.delete
import org.jetbrains.compose.resources.vectorResource

/**
 * Main content composable for displaying a list of network logs.
 * Includes search, loading, empty, and error states, as well as a clear logs dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogListContent(
    uiState: LogUiState,
    onBack: () -> Unit = {},
    onSearchQueryChanged: (String) -> Unit,
    onLogSelected: (String) -> Unit,
    onClearLogs: () -> Unit
) {
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            vectorResource(Res.drawable.arrow_back),
                            contentDescription = "Clear Logs"
                        )
                    }
                },
                title = {
                    Box {
                        Text(
                            text = "Network",
                            style = Style.Text.Headline,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteConfirmationDialog = true },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(vectorResource(Res.drawable.delete), contentDescription = "Clear Logs")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp).pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchListQuery,
                onValueChange = onSearchQueryChanged,
                label = { Text("Search URL, Method, Status...") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 14.dp),
                singleLine = true,
                textStyle = Style.Text.BodyNormal
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading && uiState.logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.logs.isEmpty() && uiState.searchListQuery.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No matching logs found.", style = Style.Text.BodyNormal)
                }
            } else if (uiState.logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No network logs recorded yet.", style = Style.Text.BodyNormal)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.logs, key = { it.id }) { log ->
                        LogListItem(log = log, onClick = {
                            focusManager.clearFocus()
                            onLogSelected(log.id)
                        })
                    }
                }
            }
        }

        // Confirmation dialog for clearing logs
        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false },
                title = { Text("Confirm Clear", style = Style.Text.BodyHead) },
                text = {
                    Text(
                        "Are you sure you want to clear all logs?",
                        style = Style.Text.BodyNormal
                    )
                },
                confirmButton = {
                    IconButton(onClick = {
                        onClearLogs()
                        showDeleteConfirmationDialog = false
                    }) {
                        Text("Yes", style = Style.Text.BodyNormal.copy(color = Color.Red))
                    }
                },
                dismissButton = {
                    IconButton(onClick = { showDeleteConfirmationDialog = false }) {
                        Text("No", style = Style.Text.BodyNormal)
                    }
                }
            )
        }
    }
}

/**
 * Displays a single log item in the list.
 * Shows URL, method, status code, content type, and timestamp.
 */
@Composable
fun LogListItem(log: NetworkLogSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = log.requestUrl,
                style = Style.Text.BodyNormal.copy(fontSize = 16.sp),
                maxLines = 2 // Limit URL length display
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val color = colorByHttpCode(log.responseStatusCode)
                Row {
                    Text(
                        text = log.requestMethod,
                        style = Style.Text.BodySmall.copy(color = color)
                    )
                    Text(
                        text = "*",
                        style = Style.Text.BodySmall,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                    Text(
                        text = log.responseStatusCode.toString(),
                        style = Style.Text.BodySmall.copy(color = color),
                        color = color
                    )
                    Text(
                        text = "*",
                        style = Style.Text.BodySmall,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                }

                log.requestTimestamp?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ContentType.parse(log.responseBodyType ?: "").contentSubtype,
                            style = Style.Text.BodySmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.width(70.dp).padding(end = 10.dp)
                        )

                        Text(
                            text = formatTimestamp(it),
                            style = Style.Text.BodySmall.copy(Color.Gray),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }

    }
}