package jp.hotdrop.createblogsupporter.ui.settings

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.hotdrop.createblogsupporter.R
import jp.hotdrop.createblogsupporter.data.repository.LlmSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class LlmSettingsViewModel @Inject constructor(
    private val repository: LlmSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LlmSettingsUiState(isLoading = true))
    val uiState: StateFlow<LlmSettingsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<LlmSettingsEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<LlmSettingsEffect> = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.observe().collect { settings ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        modelDisplayName = settings.modelDisplayName,
                        modelFilePath = settings.modelFilePath,
                    )
                }
            }
        }
    }

    fun onPickModelClick() {
        if (_uiState.value.isImportingModel) return
        viewModelScope.launch {
            _effects.send(LlmSettingsEffect.OpenModelPicker)
        }
    }

    fun onModelSelected(uri: Uri) {
        if (_uiState.value.isImportingModel) return
        _uiState.update {
            it.copy(
                isImportingModel = true,
                messageResId = R.string.llm_settings_model_importing,
                errorMessageResId = null,
            )
        }
        viewModelScope.launch {
            try {
                val selection = withContext(Dispatchers.IO) {
                    repository.importModelFile(uri)
                }
                repository.saveModelSelection(selection)
                _uiState.update {
                    it.copy(
                        isImportingModel = false,
                        modelDisplayName = selection.displayName,
                        modelFilePath = selection.absolutePath,
                        messageResId = R.string.llm_settings_model_selected,
                        errorMessageResId = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isImportingModel = false,
                        messageResId = null,
                        errorMessageResId = R.string.llm_settings_error_model_import_failed,
                    )
                }
            }
        }
    }
}

data class LlmSettingsUiState(
    val isLoading: Boolean = false,
    val isImportingModel: Boolean = false,
    val modelDisplayName: String? = null,
    val modelFilePath: String? = null,
    @param:StringRes val messageResId: Int? = null,
    @param:StringRes val errorMessageResId: Int? = null,
) {
    val hasSelectedModel: Boolean
        get() = !modelFilePath.isNullOrBlank()
}

sealed interface LlmSettingsEffect {
    data object OpenModelPicker : LlmSettingsEffect
}
