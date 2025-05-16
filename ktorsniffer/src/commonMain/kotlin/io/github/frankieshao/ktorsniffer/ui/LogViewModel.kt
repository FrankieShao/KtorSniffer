package io.github.frankieshao.ktorsniffer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.frankieshao.ktorsniffer.KtorSniffer
import io.github.frankieshao.ktorsniffer.model.NetworkLog
import io.github.frankieshao.ktorsniffer.model.NetworkLogSummary
import io.github.frankieshao.ktorsniffer.persist.NetworkLogDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


enum class UiType{ List, Detail, Error}

data class LogUiState(
    val isLoading: Boolean = true,
    val logs: List<NetworkLogSummary> = emptyList(), // Filtered list
    val searchListQuery: String = "",
    val searchDetailQuery: String = "",
    val selectedLog: NetworkLog? = null,
    val error: String? = null
) {
    val uiType: UiType = when {
        error != null -> UiType.Error
        selectedLog != null -> UiType.Detail
        else -> UiType.List
    }
}

/**
 * ViewModel for managing the state and logic of the network log UI.
 * Handles loading, searching, selecting, and clearing logs.
 */
class LogViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()
    private val logDao: NetworkLogDao by lazy { KtorSniffer.getNetworkLogDao() }
    private val searchTrigger = MutableStateFlow(_uiState.value.searchListQuery)

    init {
        observeLogs()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeLogs() {
        viewModelScope.launch {
            searchTrigger
                 .debounce(500)
                .flatMapLatest { query ->
                    logDao.getAllAsFlow(query, 5000)
                        .catch { e ->
                            _uiState.update { it.copy(isLoading = false, error = "Failed to load logs: ${e.message}") }
                            emit(emptyList())
                        }
                }
                .collect { summaries ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            logs = summaries,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Called when the search query changes in the log list.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchListQuery = query) }
    }

    /**
     * Called when a log item is selected for detail view.
     * Loads the full log from the database.
     */
    fun onLogItemSelected(logId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val log = logDao.getById(logId)
                if (log == null) {
                    _uiState.update { it.copy(selectedLog = null, error = "Log not found") }
                } else {
                    _uiState.update { it.copy(selectedLog = log, error = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(selectedLog = null, error = "error happened: ${e.message}") }
            }
        }
    }

    fun onDismissDetail() {
        _uiState.update { it.copy(selectedLog = null, error = null) }
    }

    /**
     * Called to clear all logs from the database.
     * Updates the UI state accordingly.
     */
    fun onClearLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                logDao.clearAllLogs()
                _uiState.update { it.copy(isLoading = false, selectedLog = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to clear logs: ${e.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}