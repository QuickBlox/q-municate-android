package com.quickblox.q_municate.utils.bridges;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public interface ActionBarBridge {

    void initActionBar();

    void setActionBarTitle(String title);

    void setActionBarTitle(@StringRes int title);

    void setActionBarSubtitle(String subtitle);

    void setActionBarSubtitle(@StringRes int subtitle);

    void setActionBarIcon(Drawable icon);

    void setActionBarIcon(@DrawableRes int icon);

    void setActionBarUpButtonEnabled(boolean enabled);
}