package com.quickblox.q_municate.ui.fragments.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.agreements.UserAgreementActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserAgreementDialogFragment extends DialogFragment {

    private View customDialogView;
    private MaterialDialog.ButtonCallback buttonsCallback;

    public static void show(FragmentManager fragmentManager, MaterialDialog.ButtonCallback callback) {
        UserAgreementDialogFragment fragment = new UserAgreementDialogFragment();
        fragment.setCallbacks(callback);
        fragment.show(fragmentManager, UserAgreementDialogFragment.class.getSimpleName());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initUI();
        return createDialog();
    }

    private void initUI() {
        customDialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_user_agreement, null);
        ButterKnife.bind(this, customDialogView);
    }

    private MaterialDialog createDialog() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .customView(customDialogView, true)
                .positiveText(R.string.dlg_ok)
                .negativeText(R.string.dlg_cancel)
                .callback(buttonsCallback)
                .build();

        return materialDialog;
    }

    public void setCallbacks(MaterialDialog.ButtonCallback callback) {
        this.buttonsCallback = callback;
    }

    @OnClick(R.id.user_agreement_textview)
    public void openUserAgreement(View view) {
        UserAgreementActivity.start(getActivity());
    }
}