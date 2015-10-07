package com.quickblox.q_municate.utils.bridges;

import android.view.View;

public interface SnackbarBridge {

    void createSnackBar(int titleResId, int duration);

    void showSnackbar(int titleResId, int duration);

    void showSnackbar(int titleResId, int duration, int buttonTitleResId, View.OnClickListener onClickListener);

    void showSnackbar(String title, int duration, int buttonTitleResId, View.OnClickListener onClickListener);

    void hideSnackBar();
}