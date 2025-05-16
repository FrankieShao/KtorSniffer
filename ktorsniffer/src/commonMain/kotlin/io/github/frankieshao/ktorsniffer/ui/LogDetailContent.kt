package io.github.frankieshao.ktorsniffer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sebastianneubauer.jsontree.JsonTree
import io.github.frankieshao.ktorsniffer.core.parseContentType
import io.github.frankieshao.ktorsniffer.model.NetworkLog
import io.ktor.http.ContentType
import ktorsniff.ktorsniffer.generated.resources.Res
import ktorsniff.ktorsniffer.generated.resources.arrow_back
import ktorsniff.ktorsniffer.generated.resources.arrow_right
import org.jetbrains.compose.resources.vectorResource

/**
 * @author Frank
 * @created 5/2/25
 */

data class DetailTab(val title: String)

/**
 * List of tabs for the log detail screen: General, Request, Response.
 */
val Tabs = listOf(
    DetailTab("General"),
    DetailTab("Request"),
    DetailTab("Response")
)

/**
 * Main screen for displaying the details of a network log entry.
 * Shows a tabbed interface for general info, request, and response.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailScreen(log: NetworkLog, onDismiss: () -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail", maxLines = 1, style = Style.Text.Headline) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(vectorResource(Res.drawable.arrow_back), contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> GeneralSectionContent(log)
                    1 -> RequestSectionContent(log)
                    2 -> ResponseSectionContent(log)
                }
            }
        }
    }
}

@Composable
private fun GeneralSectionContent(log: NetworkLog) {
    Column {
        DetailItem("URL", log.requestUrl)
        DetailItem("Method", log.requestMethod)
        DetailItem(
            "Status Code",
            log.responseStatusCode.toString(),
            colorByHttpCode(log.responseStatusCode)
        )
        DetailItem("Protocol", log.protocol ?: "Unknown")
        if (log.requestTimestamp != null && log.responseTimestamp != null) {
            DetailItem("Request Time", formatTimestamp(log.requestTimestamp!!))
            DetailItem("Response Time", formatTimestamp(log.responseTimestamp!!))
            DetailItem("Duration", "${log.responseTimestamp!! - log.requestTimestamp!!} ms")
        }
    }
}

@Composable
private fun RequestSectionContent(log: NetworkLog) {
    Column {
        DetailHeaders(Modifier.weight(0.35f), "Headers", log.requestHeaders)
        Spacer(modifier = Modifier.height(2.dp))
        DetailBody(
            Modifier.weight(0.65f),
            parseContentType(log.requestBodyType),
            log.requestBody
        )
    }
}

@Composable
private fun ResponseSectionContent(log: NetworkLog) {
    Column {
        DetailHeaders(
            Modifier.weight(0.35f),
            "Headers",
            log.responseHeaders
        )
        Spacer(modifier = Modifier.height(2.dp))

        if (!log.errorDetails.isNullOrBlank()) {
            DetailError(error = log.errorDetails!!)
        }

        log.responseBodyType?.let {
            DetailBody(
                Modifier.weight(0.65f),
                parseContentType(it),
                log.responseBody
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, valueColor: Color = LocalContentColor.current) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp)
                .align(androidx.compose.ui.Alignment.Top) // Align labels
        )
        Text(
            text = value,
            color = valueColor,
            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
        )
    }
}

@Composable
fun DetailHeaders(
    modifier: Modifier = Modifier,
    label: String,
    headers: Map<String, List<String>>?
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.width(100.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.arrow_right),
                contentDescription = null
            )
            Text(
                text = "$label:",
                style = Style.Text.BodyNormal.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        if (headers.isNullOrEmpty()) {
            Text("Empty", style = Style.Text.BodyNormal)
        } else {
            HorizontalDivider(thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 8.dp))
            Spacer(modifier = Modifier.height(5.dp))

            LazyColumn(
                modifier = Modifier.padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(headers.entries.toList(), key = { it.key }) { header ->
                    Row {
                        Text(
                            "${header.key}: ",
                            style = Style.Text.BodyNormal.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            header.value.joinToString("; "),
                            style = Style.Text.BodyNormal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailError(
    modifier: Modifier = Modifier,
    error: String
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.width(100.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.arrow_right),
                contentDescription = null
            )
            Text(
                text = "Error:",
                style = Style.Text.BodyNormal.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Red
                ),
            )
        }

        HorizontalDivider(thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 8.dp))
        Text(
            error,
            modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
            style = Style.Text.BodyNormal.copy(color = Color.Red)
        )

    }
}

@Composable
fun ColumnScope.DetailBody(
    modifier: Modifier = Modifier,
    bodyType: ContentType?,
    body: String?
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.width(100.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.arrow_right),
                contentDescription = null
            )
            Text(
                text = "Body:",
                style = Style.Text.BodySmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        HorizontalDivider(thickness = 0.6.dp, modifier = Modifier.padding(horizontal = 8.dp))
        Box(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            when {
                body.isNullOrBlank() -> {
                    Text("Empty", style = Style.Text.BodyNormal)
                }

                bodyType?.match(ContentType.Application.Json) == true -> {
                    JsonTree(
                        json = body,
                        onLoading = { Text(text = "Loading...") }
                    )
                }

                else -> {
                    Text(
                        body,
                        style = Style.Text.BodyNormal
                    )
                }
            }
        }
    }
}