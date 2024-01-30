package com.quickblox.qb_qmunicate.presentation.start

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.qb_qmunicate.domain.use_case.user.CheckUserExistUseCase
import com.quickblox.qb_qmunicate.domain.use_case.user.SignInUserUseCase
import com.quickblox.qb_qmunicate.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.regex.Pattern
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

                val userName = user?.fullName

                val regex = Pattern.compile("^(?=[a-zA-Z])[-a-zA-Z_ ]{3,49}(?<! )\$")
                val matcher = regex.matcher(user?.fullName.toString())

                if (userName != null && matcher.find()) {
                    _showUiKit.postValue(Unit)
                } else {
                    _showProfile.postValue(Unit)
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