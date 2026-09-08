package com.shipsafe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shipsafe.data.models.UserProfileDto
import com.shipsafe.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = true,
    val currentUser: UserProfileDto? = UserProfileDto(
        id = "usr-relmgr",
        email = "release@shipsafe.dev",
        name = "Sarah Chen",
        role = "RELEASE_MANAGER",
        avatar = null
    ),
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun switchRole(role: String) {
        RetrofitClient.currentRole = role
        val name = when (role) {
            "ADMIN" -> "Alex Mercer (Platform Admin)"
            "RELEASE_MANAGER" -> "Sarah Chen (Release Manager)"
            "ON_CALL_ENGINEER" -> "David Miller (On-Call SRE)"
            else -> "Elena Rostova (DevOps Observer)"
        }
        _uiState.value = _uiState.value.copy(
            currentUser = UserProfileDto(
                id = "usr-${role.lowercase()}",
                email = "${role.lowercase()}@shipsafe.dev",
                name = name,
                role = role,
                avatar = null
            ),
            isAuthenticated = true
        )
    }

    fun loginWithDemo(role: String = "RELEASE_MANAGER") {
        switchRole(role)
    }
}
