package io.github.frankieshao

import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

fun MainViewController() = ComposeUIViewController { App() }

actual fun getDefaultEngine(): HttpClientEngineFactory<*> = Darwin