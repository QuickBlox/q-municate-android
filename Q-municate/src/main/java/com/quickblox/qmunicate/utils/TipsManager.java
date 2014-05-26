package com.quickblox.qmunicate.utils;

import android.view.View;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseFragment;

/**
 * Created by stas on 26.05.14.
 */
public class TipsManager {

    public static void showTipIfNotShownYet(BaseFragment fragment, String tipText){
        PrefsHelper pHelper = new PrefsHelper(fragment.getActivity());
        if(!pHelper.isPrefExists(fragment.getClass().getName())){
            pHelper.savePref(fragment.getClass().getName(), true);
            fragment.showTip(tipText);
        }
    }

    public static void showTipWithButtonsIfNotShownYet(BaseFragment fragment, String tipText, View.OnClickListener listener){
        PrefsHelper pHelper = new PrefsHelper(fragment.getActivity());
        if(pHelper.isPrefExists(fragment.getClass().getName())){
            return;
        }
        pHelper.savePref(fragment.getClass().getName(), true);
        String title = fragment.getActivity().getString(R.string.tip_friend_list_button);
        fragment.getBaseActivity().showTip(tipText, listener, title);
    }
}
