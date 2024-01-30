package com.quickblox.qb_qmunicate.presentation.profile.settings

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.quickblox.android_ui_kit.domain.entity.FileEntity
import com.quickblox.android_ui_kit.domain.exception.DomainException
import com.quickblox.android_ui_kit.domain.usecases.CreateLocalFileUseCase
import com.quickblox.android_ui_kit.domain.usecases.GetLocalFileByUriUseCase
import com.quickblox.android_ui_kit.domain.usecases.UploadFileUseCase
import com.quickblox.qb_qmunicate.domain.entity.UserEntity
import com.quickblox.qb_qmunicate.domain.use_case.user.GetUserUseCase
import com.quickblox.qb_qmunicate.domain.use_case.user.SignOutUserUseCase
import com.quickblox.qb_qmunicate.domain.use_case.user.UpdateUserUseCase
import com.quickblox.qb_qmunicate.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateUserUseCase: UpdateUserUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val signOutUserUseCase: SignOutUserUseCase,
) : BaseViewModel() {
    private var avatarFileId: Int? = null
    private var uri: Uri? = null
    private var user: UserEntity? = null

    init {
        loadUser()
    }

    private val _updateUser = MutableLiveData<Unit>()
    val updateUser: LiveData<Unit>
        get() = _updateUser

    private val _loadUser = MutableLiveData<UserEntity?>()
    val loadUser: LiveData<UserEntity?>
        get() = _loadUser

    private val _signOut = MutableLiveData<Unit>()
    val signOut: LiveData<Unit>
        get() = _signOut


    internal fun loadUser() {
        user = null
        avatarFileId = null
        uri = null

        viewModelScope.launch {
            runCatching {
                user = getUserUseCase.execute(Unit)
                avatarFileId = user?.avatarFileId

                _loadUser.postValue(user)
            }.onFailure {
                showError(it.message.toString())
            }
        }
    }

    fun getUri(): Uri? {
        return uri
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching {
                signOutUserUseCase.execute(Unit)
                _signOut.postValue(Unit)
            }.onFailure {
                showError(it.message.toString())
            }
        }
    }

    suspend fun createFileAndGetUri(): Uri? {
        try {
            val fileEntity = CreateLocalFileUseCase("jpg").execute()
            uri = fileEntity?.getUri()
            return uri
        } catch (exception: DomainException) {
            showError(exception.message)
            return null
        }
    }

    suspend fun getFileBy(uri: Uri): FileEntity? {
        try {
            val file = GetLocalFileByUriUseCase(uri).execute()
            return file
        } catch (exception: DomainException) {
            showError(exception.message)
            return null
        }
    }

    suspend fun uploadFile(fileEntity: FileEntity?) {
        if (fileEntity == null) {
            showError("The file doesn't exist")
            return
        }

        showLoading()
        try {
            val remoteEntity = UploadFileUseCase(fileEntity).execute()
            avatarFileId = remoteEntity?.getId()
            hideLoading()
        } catch (exception: DomainException) {
            hideLoading()
            showError(exception.message)
        }
    }

    fun updateUser(name: String) {
        val isNotExistChanges = !isExistChanges(user, name, avatarFileId)
        if (isNotExistChanges) {
            showError("User data has not been changed")
            return
        }

        user?.fullName = name
        avatarFileId?.let {
            user?.avatarFileId = avatarFileId
        }

        viewModelScope.launch {
            runCatching {
                user?.let {
                    updateUserUseCase.execute(it)
                    _updateUser.postValue(Unit)
                }
            }.onFailure {
                showError(it.message.toString())
            }
        }
    }

    private fun isExistChanges(userEntity: UserEntity?, name: String, avatarFileId: Int?): Boolean {
        return userEntity?.fullName != name || userEntity.avatarFileId != avatarFileId
    }

    fun clearAvatar() {
        avatarFileId = null
        uri = null
        user?.applyEmptyAvatar()
    }
}