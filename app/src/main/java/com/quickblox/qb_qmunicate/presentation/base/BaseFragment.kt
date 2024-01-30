package com.quickblox.qb_qmunicate.presentation.base

import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.regex.Pattern

abstract class BaseFragment : Fragment() {
    protected fun showToast(title: String) {
        Toast.makeText(requireContext(), title, Toast.LENGTH_SHORT).show()
    }
}