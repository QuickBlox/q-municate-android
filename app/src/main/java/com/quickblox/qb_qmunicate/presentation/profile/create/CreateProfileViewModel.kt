package com.quickblox.qb_qmunicate.presentation.profile.create

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
import com.quickblox.qb_qmunicate.domain.use_case.user.UpdateUserUseCase
import com.quickblox.qb_qmunicate.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProfileViewModel @Inject constructor(
    private val updateUserUseCase: UpdateUserUseCase,
    private val getUserUseCase: GetUserUseCase,
) : BaseViewModel() {
    private var uri: Uri? = null
    private var user: UserEntity? = null

    init {
        loadUser()
    }

    private val _updateUser = MutableLiveData<Unit>()
    val updateUser: LiveData<Unit>
        get() = _updateUser

    private fun loadUser() {
        viewModelScope.launch {
            runCatching {
                user = getUserUseCase.execute(Unit)
            }.onFailure {
                showError(it.message.toString())
            }
        }
    }

    fun getUri(): Uri? {
        return uri
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
            user?.avatarFileId = remoteEntity?.getId()
            hideLoading()
        } catch (exception: DomainException) {
            hideLoading()
            showError(exception.message)
        }
    }

    fun updateUser(name: String) {
        user?.fullName = name
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

    fun clearAvatar() {
        user?.applyEmptyAvatar()
    }
}