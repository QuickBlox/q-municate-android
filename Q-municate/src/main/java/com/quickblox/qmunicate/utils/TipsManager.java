package com.quickblox.qmunicate.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Created by stas on 26.05.14.
 */
public class TipsManager {

    private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
            .setDuration(Configuration.DURATION_INFINITE)
            .build();

    private static TextView tipTextView;
    private static Button tipButton;
    private static Button tipAlternativeButton;
    private static View tipView;
    private static LayoutInflater inflater;
    private static Context context;

    private static void init(Context context){
        if(inflater != null){
            return;
        }
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
        init(fragment.getActivity());
        PrefsHelper pHelper = new PrefsHelper(fragment.getActivity());
        if(pHelper.isPrefExists(fragment.getClass().getName())){
            return;
        }
        pHelper.savePref(fragment.getClass().getName(), true);
        String title = fragment.getActivity().getString(R.string.tip_friend_list_button);
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
}
