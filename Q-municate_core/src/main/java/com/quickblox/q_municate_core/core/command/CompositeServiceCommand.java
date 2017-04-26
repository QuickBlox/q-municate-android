package com.quickblox.q_municate_core.core.command;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class CompositeServiceCommand extends ServiceCommand {

    private List<ServiceCommand> commandList = new LinkedList<ServiceCommand>();

    public CompositeServiceCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public void addCommand(ServiceCommand command) {
        commandList.add(command);
    }

    public void removeCommand(ServiceCommand command) {
        commandList.remove(command);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        Bundle params = extras;
        for (ServiceCommand command : commandList) {
            if (command != null) {
                Log.d("CompositeServiceCommand", "perform CompositeServiceCommand command = " + command);
                params = command.perform(params);
            }
        }
        return params;
    }
}