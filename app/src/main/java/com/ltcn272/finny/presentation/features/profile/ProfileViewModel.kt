//package com.ltcn272.finny.presentation.features.profile
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.ltcn272.finny.data.SettingDataStore
//import com.ltcn272.finny.domain.model.User
//import com.ltcn272.finny.domain.repository.ProfileRepository
//import com.ltcn272.finny.domain.util.AppResult
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//data class ProfileUiState(
//    val isLoading: Boolean = false,
//    val user: User? = null,
//    val error: String? = null,
//    val displayName: String = ""
//)
//
//@HiltViewModel
//class ProfileViewModel @Inject constructor(
//    private val profileRepository: ProfileRepository,
//    private val settingDataStore: SettingDataStore
//) : ViewModel() {
//
//    var uiState by mutableStateOf(ProfileUiState())
//        private set
//
//    init {
//        getProfile()
//    }
//
//    private fun getProfile() {
//        viewModelScope.launch {
//            profileRepository.getProfile().onEach { result ->
//                uiState = when (result) {
//                    is AppResult.Loading -> ProfileUiState(isLoading = true)
//                    is AppResult.Success -> {
//                        // displayName in domain User is non-nullable, use it directly
//                        ProfileUiState(user = result.data, displayName = result.data.displayName)
//                    }
//                    is AppResult.Error -> ProfileUiState(error = result.message)
//                }
//            }.launchIn(this)
//        }
//    }
//
//    fun onDisplayNameChange(newName: String) {
//        uiState = uiState.copy(displayName = newName)
//    }
//
//    fun onDoneClick() {
//        val currentName = uiState.displayName
//        viewModelScope.launch {
//            uiState = uiState.copy(isLoading = true, error = null)
//            val res = profileRepository.updateProfile(currentName, null, null)
//            uiState = when (res) {
//                is AppResult.Success -> {
//                    // save new display name locally for quick access
//                    viewModelScope.launch { settingDataStore.saveUsername(res.data.displayName) }
//                    uiState.copy(isLoading = false, user = res.data, displayName = res.data.displayName)
//                }
//                is AppResult.Error -> uiState.copy(isLoading = false, error = res.message)
//                is AppResult.Loading -> uiState.copy(isLoading = true)
//            }
//        }
//    }
//}
