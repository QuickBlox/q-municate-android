package com.quickblox.qb_qmunicate.presentation.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.quickblox.android_ui_kit.databinding.BaseDialogBinding
import com.quickblox.android_ui_kit.presentation.dialogs.MenuItem
import com.quickblox.android_ui_kit.presentation.theme.UiKitTheme
import com.quickblox.qb_qmunicate.R

class AvatarDialog(context: Context, private val listener: UserAvatarListener, private val themeDialog: UiKitTheme?) :
    Dialog(context, R.style.RoundedCornersDialog) {
    init {
        prepare()
    }

    private fun prepare() {
        val binding = BaseDialogBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        applyParams()

        themeDialog?.getMainTextColor()?.let {
            binding.tvTitle.setTextColor(it)
        }

        themeDialog?.getMainBackgroundColor()?.let {
            binding.root.setBackgroundColor(it)
        }
        binding.tvTitle.text = context.getString(R.string.change_photo)

        val views = collectViewsTemplateMethod()
        for (view in views) {
            view?.let {
                binding.llContainer.addView(view)
            }
        }
    }

    private fun collectViewsTemplateMethod(): List<View?> {
        val views = mutableListOf<View?>()

        val cameraItem = buildCameraItem()
        val galleryItem = buildGalleryItem()
        val removeItem = buildRemoveItem()

        views.add(cameraItem)
        views.add(galleryItem)
        views.add(removeItem)

        return views
    }

    private fun buildCameraItem(): View {
        val cameraItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            cameraItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            cameraItem.setRipple(it)
        }
        cameraItem.setText(context.getString(R.string.take_photo_from_camera))
        cameraItem.setItemClickListener {
            dismiss()
            listener.onClickCamera()
        }

        return cameraItem
    }

    private fun buildGalleryItem(): View {
        val galleryItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            galleryItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            galleryItem.setRipple(it)
        }
        galleryItem.setText(context.getString(R.string.open_gallery))
        galleryItem.setItemClickListener {
            dismiss()
            listener.onClickGallery()
        }

        return galleryItem
    }

    private fun buildRemoveItem(): View {
        val removeItem = MenuItem(context)

        themeDialog?.getMainTextColor()?.let {
            removeItem.setColorText(it)
        }
        themeDialog?.getMainElementsColor()?.let {
            removeItem.setRipple(it)
        }
        removeItem.setText(context.getString(R.string.remove_photo))
        removeItem.setItemClickListener {
            dismiss()
            listener.onClickRemove()
        }

        return removeItem
    }

    interface UserAvatarListener {
        fun onClickCamera()
        fun onClickGallery()
        fun onClickRemove()
    }

    private fun applyParams() {
        setCancelable(true)
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}