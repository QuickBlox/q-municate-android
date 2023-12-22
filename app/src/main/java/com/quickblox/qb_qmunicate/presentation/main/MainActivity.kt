package com.quickblox.qb_qmunicate.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.quickblox.android_ui_kit.QuickBloxUiKit
import com.quickblox.android_ui_kit.presentation.screens.dialogs.DialogsScreenSettings
import com.quickblox.qb_qmunicate.R
import com.quickblox.qb_qmunicate.databinding.MainLayoutBinding
import com.quickblox.qb_qmunicate.presentation.base.BaseActivity
import com.quickblox.qb_qmunicate.presentation.profile.settings.SettingsFragment
import com.quickblox.qb_qmunicate.presentation.theme_manager.ThemeManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: MainLayoutBinding

    private var activeFragment: Fragment? = null

    private val viewModel by viewModels<MainViewModel>()

    companion object {
        fun show(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uiKitTheme = ThemeManager.checkModeAndGetUIKitTheme(this)
        val dialogsScreenSettings = DialogsScreenSettings.Builder(this).setTheme(uiKitTheme).build()
        dialogsScreenSettings.getHeaderComponent()?.setVisibleLeftButton(false)

        val dialogsFragment = QuickBloxUiKit.getScreenFactory().createDialogs(dialogsScreenSettings)

        val settingsFragment = SettingsFragment.newInstance()

        supportFragmentManager.beginTransaction().add(R.id.container, dialogsFragment, getString(R.string.dialog_tag))
            .add(R.id.container, settingsFragment, getString(R.string.settings_tag)).hide(settingsFragment).commitNow()

        activeFragment = dialogsFragment

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dialogs_page -> {
                    switchFragment(dialogsFragment, R.id.dialogs_page)
                    true
                }

                R.id.settings_page -> {
                    settingsFragment.updateUser()
                    switchFragment(settingsFragment, R.id.settings_page)
                    true
                }

                else -> false
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // empty
            }
        })
    }

    private fun switchFragment(fragment: Fragment, id: Int) {
        if (fragment !== activeFragment) {
            var transaction = supportFragmentManager.beginTransaction()
            transaction = setAnimation(transaction, id)

            transaction.hide(activeFragment!!)
            transaction.show(fragment)
            transaction.commitNow()

            activeFragment = fragment
        }
    }

    private fun setAnimation(transaction: FragmentTransaction, id: Int): FragmentTransaction {
        if (id == R.id.settings_page) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (id == R.id.dialogs_page) {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        return transaction
    }
}