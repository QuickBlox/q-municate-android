package com.quickblox.q_municate.ui.fragments.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.agreements.UserAgreementActivity;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate_core.utils.DialogUtils;

@Deprecated
public class UserAgreementDialog extends DialogFragment {

    private BaseActivity activity;

    private DialogInterface.OnClickListener positiveButtonOnClickListener;
    private DialogInterface.OnClickListener negativeButtonOnClickListener;
    private TextView userAgreementTextView;

    public static UserAgreementDialog newInstance(DialogInterface.OnClickListener positiveClickListener,
                                                  DialogInterface.OnClickListener negativeClickListener) {
        UserAgreementDialog userAgreementDialog = new UserAgreementDialog();
        userAgreementDialog.setListeners(positiveClickListener, negativeClickListener);
        return userAgreementDialog;
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

    public void setListeners(DialogInterface.OnClickListener positiveButtonOnClickListener,
            DialogInterface.OnClickListener negativeButtonOnClickListener) {
        this.positiveButtonOnClickListener = positiveButtonOnClickListener;
        this.negativeButtonOnClickListener = negativeButtonOnClickListener;
    }
}