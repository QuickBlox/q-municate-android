package com.quickblox.q_municate_core.core.command;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.core.exception.QBResponseException;

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
    protected Bundle perform(Bundle extras) throws QBResponseException {
        Bundle params = extras;
        for (ServiceCommand command : commandList) {
            params = command.perform(params);
        }
        return params;
    }
}
