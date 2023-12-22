package com.quickblox.qb_qmunicate.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.use_case.user.CheckUserExistUseCase
import com.quickblox.qb_qmunicate.domain.use_case.user.SignInUserUseCase
import com.quickblox.qb_qmunicate.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val signInUserUseCase: SignInUserUseCase,
    private val checkUserExistUseCase: CheckUserExistUseCase,
) : BaseViewModel() {
    private val _showUiKit = MutableLiveData<Unit>()
    val showUiKit: LiveData<Unit>
        get() = _showUiKit

    private val _showFirebase = MutableLiveData<Unit>()
    val showFirebase: LiveData<Unit>
        get() = _showFirebase

    private val _showProfile = MutableLiveData<Unit>()
    val showProfile: LiveData<Unit>
        get() = _showProfile

    fun checkUserExistAndNotify() {
        viewModelScope.launch {
            val isUserExist = isUserExist()
            if (isUserExist) {
                signInAndShowProfileOrUIKit()
            } else {
                _showFirebase.postValue(Unit)
            }
        }
    }

    fun signInAndShowProfileOrUIKit() {
        viewModelScope.launch {
            runCatching {
                val user = signInUserUseCase.execute(Unit)
                if (user?.fullName.isNullOrEmpty()) {
                    _showProfile.postValue(Unit)
                } else {
                    _showUiKit.postValue(Unit)
                }
            }.onFailure {
                showError(it.message.toString())
            }
        }
    }

    private suspend fun isUserExist(): Boolean {
        runCatching {
            return checkUserExistUseCase.execute(Unit)
        }.onFailure {
            showError(it.message.toString())
        }
        return false
    }
}