package com.quickblox.qb_qmunicate.presentation.base

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.regex.Pattern

abstract class BaseActivity : AppCompatActivity() {
    protected fun showToast(title: String) {
        lifecycleScope.launch {
            Toast.makeText(this@BaseActivity, title, Toast.LENGTH_SHORT).show()
        }
    }
}