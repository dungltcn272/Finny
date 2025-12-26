package com.ltcn272.finny.presentation.features.profile

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ltcn272.finny.R
import com.ltcn272.finny.data.SettingDataStore
import com.ltcn272.finny.domain.model.User
import com.ltcn272.finny.domain.repository.ProfileRepository
import com.ltcn272.finny.domain.util.AppResult
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.presentation.common.ui.TopSnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Thêm data class cho SnackbarState
data class SnackbarState(
    val visible: Boolean = false,
    val message: String = "",
    val type: TopSnackbarType = TopSnackbarType.INFO,
)

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val snackbarState: SnackbarState = SnackbarState(),
    val displayName: String = "",
    val finished: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingDataStore: SettingDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        getProfile()
    }

    private fun getProfile() {
        profileRepository.getProfile().onEach { result ->
            uiState = when (result) {
                is AppResult.Loading -> uiState.copy(isLoading = true)
                is AppResult.Success -> {
                    uiState.copy(
                        isLoading = false,
                        user = result.data,
                        displayName = result.data.displayName ?: ""
                    )
                }
                is AppResult.Error -> uiState.copy(
                    isLoading = false,
                    snackbarState = SnackbarState(
                        visible = true,
                        message = mapErrorToString(result.errorType),
                        type = TopSnackbarType.ERROR
                    )
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onDisplayNameChange(newName: String) {
        uiState = uiState.copy(displayName = newName)
    }

    fun onDoneClick() {
        val originalUser = uiState.user ?: return
        val newName = uiState.displayName.trim()

        if (newName.isBlank()) {
            uiState = uiState.copy(
                snackbarState = SnackbarState(
                    visible = true,
                    message = context.getString(R.string.error_display_name_empty),
                    type = TopSnackbarType.WARNING
                )
            )
            return
        }

        if (newName == originalUser.displayName) {
            uiState = uiState.copy(
                snackbarState = SnackbarState(
                    visible = true,
                    message = "Không có gì thay đổi.",
                    type = TopSnackbarType.INFO
                )
            )
            return
        }

        viewModelScope.launch {
            val updateData = mapOf("display_name" to newName)
            profileRepository.updateProfile(updateData).onEach { result ->
                uiState = when (result) {
                    is AppResult.Loading -> uiState.copy(isLoading = true)
                    is AppResult.Success -> {
                        settingDataStore.saveUsername(newName)

                        uiState.copy(
                            isLoading = false,
                            user = result.data,
                            displayName = newName,
                            snackbarState = SnackbarState(
                                visible = true,
                                message = context.getString(R.string.profile_updated_successfully),
                                type = TopSnackbarType.SUCCESS
                            )
                        )
                    }
                    is AppResult.Error -> uiState.copy(
                        isLoading = false,
                        snackbarState = SnackbarState(
                            visible = true,
                            message = mapErrorToString(result.errorType),
                            type = TopSnackbarType.ERROR
                        )
                    )
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onSnackbarDismissed() {
        uiState = uiState.copy(snackbarState = uiState.snackbarState.copy(visible = false))
    }

    private fun mapErrorToString(errorType: ErrorType): String {
        return when (errorType) {
            ErrorType.NETWORK -> context.getString(R.string.error_network)
            ErrorType.TIMEOUT -> context.getString(R.string.error_timeout)
            ErrorType.UNAUTHORIZED -> context.getString(R.string.error_unauthorized)
            ErrorType.SERVER_ERROR -> context.getString(R.string.error_server)
            ErrorType.UNKNOWN -> context.getString(R.string.error_unknown)
        }
    }
}
