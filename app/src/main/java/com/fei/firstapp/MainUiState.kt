package com.fei.firstapp

data class MainUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isGuest: Boolean = false,
    val isRegistering: Boolean = false,
    val permissions: PermissionResponse? = null,
    val errorMessage: String? = null
)
