package com.quickblox.qb_qmunicate.presentation.start

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.dialogs.PositiveNegativeDialog
import com.quickblox.qb_qmunicate.BuildConfig
import com.quickblox.qb_qmunicate.BuildConfig.QB_AI_PROXY_SERVER_URL
import com.quickblox.qb_qmunicate.BuildConfig.QB_OPEN_AI_TOKEN
import com.quickblox.qb_qmunicate.R
import com.quickblox.qb_qmunicate.databinding.StartLayoutBinding
import com.quickblox.qb_qmunicate.presentation.base.BaseActivity
import com.quickblox.qb_qmunicate.presentation.main.MainActivity
import com.quickblox.qb_qmunicate.presentation.profile.create.CreateProfileActivity
import com.quickblox.qb_qmunicate.presentation.theme_manager.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class StartActivity : BaseActivity() {
    companion object {
        fun show(context: Context) {
            val intent = Intent(context, StartActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: StartLayoutBinding
    private val viewModel by viewModels<StartViewModel>()

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result: FirebaseAuthUIAuthenticationResult? ->
            if (result?.resultCode == Activity.RESULT_OK) {
                viewModel.signInAndShowProfileOrUIKit()
            } else {
                showRegistrationOrExitDialog()
            }
        }

    private fun showRegistrationOrExitDialog() {
        val contentText = getString(R.string.do_you_want_to_continue_authorization)
        val positiveText = getString(com.quickblox.android_ui_kit.R.string.yes)
        val negativeText = getString(com.quickblox.android_ui_kit.R.string.no)

        val uiKitTheme = ThemeManager.checkModeAndGetUIKitTheme(this)
        PositiveNegativeDialog.show(this, contentText, positiveText, negativeText, uiKitTheme, positiveListener = {
            signInLauncher.launch(getSignInIntent())
        }, negativeListener = {
            finish()
        }, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StartLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.main_color)

        subscribeToError()
        subscribeToShowUiKit()
        subscribeToFirebase()
        subscribeToProfile()

        updateAppVersionText()
        checkAndInstallUpdate()
    }

    private fun subscribeToShowUiKit() {
        viewModel.showUiKit.observe(this) {
            initUIKit()
            MainActivity.show(this@StartActivity)
        }
    }

    private fun subscribeToFirebase() {
        viewModel.showFirebase.observe(this) {
            signInLauncher.launch(getSignInIntent())
        }
    }

    private fun subscribeToProfile() {
        viewModel.showProfile.observe(this) {
            initUIKit()
            CreateProfileActivity.show(this@StartActivity)
        }
    }

    private fun getSignInIntent(): Intent {
        val selectedProviders: MutableList<AuthUI.IdpConfig> = ArrayList()
        selectedProviders.add(AuthUI.IdpConfig.PhoneBuilder().build())
        val builder: AuthUI.SignInIntentBuilder =
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(selectedProviders)
                .setTheme(R.style.AuthFbTheme).setTosAndPrivacyPolicyUrls(
                    "https://quickblox.com/terms-of-service/", "https://quickblox.com/privacy-policy/"
                )

        return builder.build()
    }

    private fun subscribeToError() {
        viewModel.errorMessage.observe(this) { message ->
            showToast(message)
        }
    }

    private fun initUIKit() {
        initAiUiKit()

        QuickBloxUiKit.init(applicationContext)

        val REGEX_USER_NAME = "^(?=[a-zA-Z])[-a-zA-Z_ ]{3,49}(?<! )\$"
        QuickBloxUiKit.setRegexUserName(REGEX_USER_NAME)
        val uiKitTheme = ThemeManager.checkModeAndGetUIKitTheme(this)
        QuickBloxUiKit.setTheme(uiKitTheme)
    }

    private fun initAiUiKit() {
        if (QB_OPEN_AI_TOKEN.isNotEmpty()) {
            QuickBloxUiKit.enableAITranslateWithOpenAIToken(QB_OPEN_AI_TOKEN)
            QuickBloxUiKit.enableAIRephraseWithOpenAIToken(QB_OPEN_AI_TOKEN)
            QuickBloxUiKit.enableAIAnswerAssistantWithOpenAIToken(QB_OPEN_AI_TOKEN)
        } else if (QB_AI_PROXY_SERVER_URL.isNotEmpty()) {
            QuickBloxUiKit.enableAITranslateWithProxyServer(QB_AI_PROXY_SERVER_URL)
            QuickBloxUiKit.enableAIRephraseWithProxyServer(QB_AI_PROXY_SERVER_URL)
            QuickBloxUiKit.enableAIAnswerAssistantWithProxyServer(QB_AI_PROXY_SERVER_URL)
        }
    }

    private fun checkAndInstallUpdate() {
        lifecycleScope.launch(Dispatchers.IO) {
            val appUpdateManager = AppUpdateManagerFactory.create(this@StartActivity)
            val appUpdateInfo = loadAppUpdateInfo(appUpdateManager)
            val isUpdateAvailable = appUpdateInfo?.isImmediateUpdateAllowed == true
            if (isUpdateAvailable && appUpdateInfo != null) {
                updateApp(appUpdateManager, appUpdateInfo)
            } else {
                viewModel.checkUserExistAndNotify()
            }
        }
    }

    private suspend fun loadAppUpdateInfo(appUpdateManager: AppUpdateManager): AppUpdateInfo? {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        runCatching {
            appUpdateInfoTask.await()
            return appUpdateInfoTask.result
        }.onFailure { error ->
            showToast(getString(R.string.error_app_update_config))
        }
        return null
    }

    private fun updateApp(appUpdateManager: AppUpdateManager, appUpdateInfo: AppUpdateInfo) {
        val appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, appUpdateResultLauncher, appUpdateOptions)
    }

    private val appUpdateResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            updateAppVersionText()
            viewModel.checkUserExistAndNotify()
        }

    private fun updateAppVersionText() {
        val versionCode = "(${BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE})"
        binding.tvVersion.text = getString(R.string.powered_by_quickblox, versionCode)
    }
}