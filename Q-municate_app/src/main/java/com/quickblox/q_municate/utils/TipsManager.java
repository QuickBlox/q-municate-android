package com.quickblox.q_municate.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate_core.utils.PrefsHelper;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class TipsManager {

    private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
            .setDuration(Configuration.DURATION_INFINITE)
            .build();

    private static TextView tipTextView;
    private static Button tipButton;
    private static Button tipAlternativeButton;
    private static View tipView;
    private static LayoutInflater inflater;
    private static boolean isJustLogined;

    private static void init(Context context){
        inflater = LayoutInflater.from(context);
        tipView = inflater.inflate(R.layout.list_item_tip, null);
        tipTextView = (TextView) tipView.findViewById(R.id.tip_textview);
        tipButton = (Button) tipView.findViewById(R.id.ok_button);
        tipAlternativeButton = (Button) tipView.findViewById(R.id.alternative_button);
    }

    public static void showTipIfNotShownYet(BaseFragment fragment, String tipText){
        init(fragment.getActivity());
        PrefsHelper pHelper = new PrefsHelper(fragment.getActivity());
        if(!pHelper.isPrefExists(fragment.getClass().getName())){
            pHelper.savePref(fragment.getClass().getName(), true);
            showTip(fragment.getActivity(), tipText, null, null);
        }
    }

    public static void showTipWithButtonsIfNotShownYet(BaseFragment fragment, String tipText, View.OnClickListener listener){
        if(isJustLogined){
            isJustLogined = false;
            return;
        }
        init(fragment.getActivity());
        PrefsHelper pHelper = new PrefsHelper(fragment.getActivity());
        if(pHelper.isPrefExists(fragment.getClass().getName())){
            return;
        }
        pHelper.savePref(fragment.getClass().getName(), true);
//        String title = fragment.getActivity().getString(R.string.tip_friend_list_button);
        String title = "";
        showTip(fragment.getActivity(), tipText, listener, title);
    }

    private static void showTip(Activity activity, String tip, View.OnClickListener secondButtonListener, String secondButtonTitle){
        tipTextView.setText(tip);
        final Crouton crouton = Crouton.make(activity, tipView);
        crouton.setConfiguration(CONFIGURATION_INFINITE);
        crouton.show();
        tipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crouton.hide();
            }
        });
        if(secondButtonListener != null){
            tipAlternativeButton.setVisibility(View.VISIBLE);
            tipAlternativeButton.setOnClickListener(secondButtonListener);
            tipAlternativeButton.setText(secondButtonTitle);
            tipAlternativeButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    crouton.hide();
                    return false;
                }
            });
        } else{
            tipAlternativeButton.setVisibility(View.GONE);
        }
    }

    public static void setIsJustLogined(boolean isJustLogined) {
        TipsManager.isJustLogined = isJustLogined;
    }
}
