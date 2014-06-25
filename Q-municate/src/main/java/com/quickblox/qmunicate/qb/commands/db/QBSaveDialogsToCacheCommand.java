package com.quickblox.qmunicate.qb.commands.db;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;

public class QBSaveDialogsToCacheCommand extends ServiceCommand {


    public QBSaveDialogsToCacheCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }


    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        return null;
    }
}
