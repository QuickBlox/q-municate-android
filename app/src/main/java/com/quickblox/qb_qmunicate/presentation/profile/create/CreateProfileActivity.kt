package com.quickblox.qb_qmunicate.presentation.profile.create

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.quickblox.android_ui_kit.R
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog
import com.quickblox.android_ui_kit.presentation.makeClickableBackground
import com.quickblox.android_ui_kit.presentation.screens.loadCircleImageFromUri
import com.quickblox.android_ui_kit.presentation.screens.setOnClick
import com.quickblox.qb_qmunicate.databinding.CreateProfileLayoutBinding
import com.quickblox.qb_qmunicate.presentation.base.BaseActivity
import com.quickblox.qb_qmunicate.presentation.dialog.AvatarDialog
import com.quickblox.qb_qmunicate.presentation.main.MainActivity
import com.quickblox.qb_qmunicate.presentation.theme_manager.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val CAMERA_PERMISSION = "android.permission.CAMERA"

@AndroidEntryPoint
class CreateProfileActivity : BaseActivity() {
    private lateinit var binding: CreateProfileLayoutBinding

    private val requestPermissionLauncher = registerPermissionLauncher()
    private var cameraLauncher: ActivityResultLauncher<Uri>? = registerCameraLauncher()
    private var galleryLauncher: ActivityResultLauncher<String>? = registerGalleryLauncher()
    private val viewModel by viewModels<CreateProfileViewModel>()

    companion object {
        fun show(context: Context) {
            val intent = Intent(context, CreateProfileActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateProfileLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFinish.makeClickableBackground(ContextCompat.getColor(this, R.color.primary))
        binding.tilName.isHintEnabled = false

        setListeners()

        subscribeToError()
        subscribeToUpdateUser()
        subscribeToAvatarLoading()
    }

    private fun setListeners() {
        binding.btnFinish.setOnClick {
            val name = binding.etName.text.toString()
            if (isNameValid(name) && isNotUploadingAvatar()) {
                binding.finishProgressBar.visibility = View.VISIBLE
                viewModel.updateUser(name)
                return@setOnClick
            }
        }

        binding.etName.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            if (dstart == 0 && end > 0) {
                if (!Character.isLetter(source[start])) {
                    return@InputFilter ""
                }
            }
            null
        })

        binding.etName.doAfterTextChanged {
            binding.tilName.isErrorEnabled = false
        }

        binding.ivAvatar.setOnClickListener {
            avatarPressed()
        }
    }

    private fun isNotUploadingAvatar(): Boolean {
        val isLoading = viewModel.loading.value
        return if (isLoading == true) {
            showToast("Please wait for the avatar to load.")
            false
        } else {
            true
        }
    }

    private fun subscribeToUpdateUser() {
        viewModel.updateUser.observe(this) {
            binding.finishProgressBar.visibility = View.GONE
            showMainScreen()
        }
    }

    private fun showMainScreen() {
        MainActivity.show(this)
    }

    private fun isNameValid(name: String): Boolean {
        val isValidName = name.isNotBlank() && name.length > 2

        if (!isValidName) {
            binding.tilName.error = getString(com.quickblox.qb_qmunicate.R.string.min_3_symbols)
        }
        return isValidName
    }

    private fun avatarPressed() {
        val uiKitTheme = ThemeManager.checkModeAndGetUIKitTheme(this)
        AvatarDialog(this, AvatarListenerImpl(), uiKitTheme).show()
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
                val view = binding.ivAvatar
                view.loadCircleImageFromUri(uri, R.drawable.user_avatar_holder)

                uploadFileBy(uri)
            }
        }
    }

    private fun registerGalleryLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val view = binding.ivAvatar
            view.loadCircleImageFromUri(uri, R.drawable.user_avatar_holder)

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
        val checkedCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return checkedCameraPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionAndLaunchCamera() {
        val isHasPermission = checkPermissionRequest()
        if (isHasPermission) {
            lifecycleScope.launch {
                val uri = viewModel.createFileAndGetUri()
                cameraLauncher?.launch(uri)
            }
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            val contentText = getString(R.string.permission_alert_text)
            val positiveText = getString(R.string.yes)
            val negativeText = getString(R.string.no)

            val uiKitTheme = ThemeManager.checkModeAndGetUIKitTheme(this)
            PositiveNegativeDialog.show(this, contentText, positiveText, negativeText, uiKitTheme, positiveListener = {
                requestCameraPermission()
            }, negativeListener = {
                showToast(getString(R.string.permission_denied))
            })
        } else {
            requestCameraPermission()
        }
    }

    private fun subscribeToAvatarLoading() {
        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun subscribeToError() {
        viewModel.errorMessage.observe(this) { message ->
            showToast(message)
            binding.progressBar.visibility = View.GONE
            binding.finishProgressBar.visibility = View.GONE
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
            val view = binding.ivAvatar
            view.setImageResource(R.drawable.user_avatar_holder)
            viewModel.clearAvatar()
        }
    }
}