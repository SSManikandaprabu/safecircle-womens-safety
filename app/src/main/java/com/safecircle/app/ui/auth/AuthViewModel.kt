package com.safecircle.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safecircle.app.data.repository.AuthRepository
import com.safecircle.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email and password are required.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.signInWithEmail(email.trim(), password)) {
                is Result.Success ->
                    _uiState.value = AuthUiState(isSignedIn = true)
                is Result.Error ->
                    _uiState.value = AuthUiState(errorMessage = result.message)
                Result.Loading -> Unit
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "All fields are required for sign up."
            )
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Password must be at least 6 characters."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.signUpWithEmail(
                email.trim(), password, displayName.trim()
            )) {
                is Result.Success ->
                    _uiState.value = AuthUiState(isSignedIn = true)
                is Result.Error ->
                    _uiState.value = AuthUiState(errorMessage = result.message)
                Result.Loading -> Unit
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.signInWithGoogleIdToken(idToken)) {
                is Result.Success ->
                    _uiState.value = AuthUiState(isSignedIn = true)
                is Result.Error ->
                    _uiState.value = AuthUiState(errorMessage = result.message)
                Result.Loading -> Unit
            }
        }
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
