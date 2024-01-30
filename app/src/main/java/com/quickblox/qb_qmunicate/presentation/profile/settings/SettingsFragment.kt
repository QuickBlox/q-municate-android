package com.quickblox.qb_qmunicate.presentation.profile.settings

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.presentation.checkStringByRegex
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUri
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUrl
import com.quickblox.android_ui_kit.presentation.screens.setOnClick
import com.quickblox.qb_qmunicate.databinding.SettingsLayoutBinding
import com.quickblox.qb_qmunicate.presentation.base.BaseFragment
import com.quickblox.qb_qmunicate.presentation.dialog.AvatarDialog
import com.quickblox.qb_qmunicate.presentation.start.StartActivity
import com.quickblox.qb_qmunicate.presentation.theme_manager.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val CAMERA_PERMISSION = "android.permission.CAMERA"
private const val REGEX_USER_NAME = "^(?=[a-zA-Z])[-a-zA-Z_ ]{3,49}(?<! )\$"

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {
    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    private val requestPermissionLauncher = registerPermissionLauncher()
    private var cameraLauncher: ActivityResultLauncher<Uri>? = registerCameraLauncher()
    private var galleryLauncher: ActivityResultLauncher<String>? = registerGalleryLauncher()

    private val viewModel by viewModels<SettingsViewModel>()

    private var binding: SettingsLayoutBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SettingsLayoutBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.btnSave?.makeClickableBackground(ContextCompat.getColor(requireContext(), R.color.primary))
        binding?.tvLogout?.makeClickableBackground(ContextCompat.getColor(requireContext(), R.color.primary))

        binding?.tilName?.isHintEnabled = false
        binding?.tilName?.isEndIconVisible = false
        setListeners()

        subscribeToError()
        subscribeToUpdateUser()
        subscribeToLoadUser()
        subscribeToSignOut()
        subscribeToAvatarLoading()
    }

    private fun setListeners() {
        binding?.btnSave?.setOnClick {
            val name = binding?.etName?.text.toString()

            if (isNotUploadingAvatar()) {
                binding?.saveProgressBar?.visibility = View.VISIBLE
                viewModel.updateUser(name)
                binding?.tilName?.isEndIconVisible = false
                return@setOnClick
            }
        }

        binding?.tvLogout?.setOnClick {
            binding?.saveProgressBar?.visibility = View.VISIBLE
            viewModel.signOut()
        }

        binding?.etName?.doAfterTextChanged {
            val enteredUserName = it.toString()
            val isValidName = enteredUserName.checkStringByRegex(REGEX_USER_NAME)

            enableSaveButton(isValidName)
        }

        binding?.ivAvatar?.setOnClickListener {
            avatarPressed()
        }
    }

    private fun enableSaveButton(isEnable: Boolean) {
        binding?.btnSave?.isEnabled = isEnable
    }

    private fun isNotUploadingAvatar(): Boolean {
        val isLoading = viewModel.loading.value
        return if (isLoading == true) {
            showToast(getString(com.quickblox.qb_qmunicate.R.string.please_wait_for_the_avatar_to_load))
            false
        } else {
            true
        }
    }

    private fun subscribeToUpdateUser() {
        viewModel.updateUser.observe(viewLifecycleOwner) {
            binding?.saveProgressBar?.visibility = View.GONE
            showToast(getString(com.quickblox.qb_qmunicate.R.string.user_updated))
        }
    }

    private fun subscribeToLoadUser() {
        viewModel.loadUser.observe(viewLifecycleOwner) { user ->
            val isValidAvatarUrl = !user?.avatarFileUrl.isNullOrBlank()
            val view = binding?.ivAvatar
            if (isValidAvatarUrl) {
                view?.loadCircleImageFromUrl(user?.avatarFileUrl, R.drawable.user_avatar_holder)
            } else {
                view?.setImageResource(R.drawable.user_avatar_holder)
            }

            val userName = user?.fullName
            binding?.etName?.setText(userName ?: "")

            val isValidName = userName.toString().checkStringByRegex(REGEX_USER_NAME)
            enableSaveButton(isValidName)
        }
    }

    private fun subscribeToSignOut() {
        viewModel.signOut.observe(viewLifecycleOwner) { user ->
            binding?.saveProgressBar?.visibility = View.GONE
            StartActivity.show(requireContext())
        }
    }


    private fun avatarPressed() {
        val uiKitTheme = ThemeManager.checkModeAndGetUIKitTheme(requireContext())
        AvatarDialog(requireContext(), AvatarListenerImpl(), uiKitTheme).show()
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(CAMERA_PERMISSION)
    }

    private fun registerPermissionLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                checkPermissionAndLaunchCamera()
            } else {
                showToast(getString(R.string.permission_denied))
            }
        }
    }

    private fun registerCameraLauncher(): ActivityResultLauncher<Uri> {
        return registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val uri = viewModel.getUri()
                val view = binding?.ivAvatar
                view?.loadCircleImageFromUri(uri, R.drawable.user_avatar_holder)

                uploadFileBy(uri)
            }
        }
    }

    private fun registerGalleryLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val view = binding?.ivAvatar
            view?.loadCircleImageFromUri(uri, R.drawable.user_avatar_holder)

            uploadFileBy(uri)
        }
    }

    private fun uploadFileBy(uri: Uri?) {
        lifecycleScope.launch {
            val file = uri?.let {
                viewModel.getFileBy(it)
            }
            viewModel.uploadFile(file)
        }
    }

    private fun checkPermissionRequest(): Boolean {
        val checkedCameraPermission = ContextCompat.checkSelfPermission(requireContext(), CAMERA)
        return checkedCameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionAndLaunchCamera() {
        val isHasPermission = checkPermissionRequest()
        if (isHasPermission) {
            lifecycleScope.launch {
                val uri = viewModel.createFileAndGetUri()
                cameraLauncher?.launch(uri)
            }
        } else if (shouldShowRequestPermissionRationale(CAMERA)) {
            val contentText = getString(R.string.permission_alert_text)
            val positiveText = getString(R.string.yes)
            val negativeText = getString(R.string.no)

            val uiKitTheme = ThemeManager.checkModeAndGetUIKitTheme(requireContext())
            PositiveNegativeDialog.show(requireContext(),
                contentText,
                positiveText,
                negativeText,
                uiKitTheme,
                positiveListener = {
                    requestCameraPermission()
                },
                negativeListener = {
                    showToast(getString(R.string.permission_denied))
                })
        } else {
            requestCameraPermission()
        }
    }

    private fun subscribeToAvatarLoading() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding?.progressBar?.visibility = View.VISIBLE
            } else {
                binding?.progressBar?.visibility = View.GONE
            }
        }
    }

    private fun subscribeToError() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            showToast(message)
            binding?.progressBar?.visibility = View.GONE
            binding?.saveProgressBar?.visibility = View.GONE

        }
    }

    private inner class AvatarListenerImpl : AvatarDialog.UserAvatarListener {
        override fun onClickCamera() {
            checkPermissionAndLaunchCamera()
        }

        override fun onClickGallery() {
            val IMAGE_MIME = "image/*"
            galleryLauncher?.launch(IMAGE_MIME)
        }

        override fun onClickRemove() {
            val view = binding?.ivAvatar
            view?.setImageResource(R.drawable.user_avatar_holder)
            viewModel.clearAvatar()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    fun updateUser() {
        viewModel.loadUser()
    }
}