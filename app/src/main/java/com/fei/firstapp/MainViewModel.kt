package com.fei.firstapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(application)
    private val _uiState = MutableStateFlow(MainUiState(isLoading = true))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val user = repository.restoreSession()
                val permissions = if (user != null) repository.getPermissions() else null
                _uiState.value = MainUiState(user = user, permissions = permissions)
            } catch (error: Exception) {
                _uiState.value = MainUiState(errorMessage = repository.errorMessage(error))
            }
        }
    }

    fun login(phone: String, password: String) = authenticate(phone, password, registering = false)
    fun register(phone: String, password: String) = authenticate(phone, password, registering = true)

    private fun authenticate(phone: String, password: String, registering: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isRegistering = registering) }
            try {
                val user = if (registering) repository.register(phone, password)
                    else repository.login(phone, password)
                val permissions = repository.getPermissions()
                _uiState.value = MainUiState(user = user, permissions = permissions)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = repository.errorMessage(error))
                }
            }
        }
    }

    fun setRegistering(value: Boolean) {
        _uiState.update { it.copy(isRegistering = value, errorMessage = null) }
    }

    fun enterGuestMode() {
        _uiState.value = MainUiState(isGuest = true)
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { repository.logout() }
            _uiState.value = MainUiState()
        }
    }
}
