package com.quickblox.q_municate.ui.authorization.landing;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.agreements.UserAgreementActivity;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate_core.utils.DialogUtils;

public class UserAgreementDialog extends DialogFragment {

    private BaseActivity activity;

    private static DialogInterface.OnClickListener positiveButtonOnClickListener;
    private static DialogInterface.OnClickListener negativeButtonOnClickListener;
    private TextView userAgreementTextView;

    public static UserAgreementDialog newInstance(DialogInterface.OnClickListener positiveClickListener,
                                                  DialogInterface.OnClickListener negativeClickListener) {
        positiveButtonOnClickListener = positiveClickListener;
        negativeButtonOnClickListener = negativeClickListener;
        return new UserAgreementDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_user_agreement, null);

        activity = (BaseActivity) getActivity();

        initUI(rootView);
        initListeners();

        return DialogUtils.createDialog(getActivity(), R.string.uag_user_agreement_title, rootView,
                positiveButtonOnClickListener, negativeButtonOnClickListener);
    }

    private void initUI(View rootView) {
        userAgreementTextView = (TextView) rootView.findViewById(R.id.user_agreement_textview);
    }

    private void initListeners() {
        userAgreementTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserAgreementActivity.start(activity);
            }
        });
    }
}