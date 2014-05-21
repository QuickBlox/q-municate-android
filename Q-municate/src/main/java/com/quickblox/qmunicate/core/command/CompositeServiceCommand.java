package com.quickblox.qmunicate.core.command;

import android.content.Context;
import android.os.Bundle;

import java.util.LinkedList;
import java.util.List;

public abstract class CompositeServiceCommand extends ServiceCommand {

    private List<ServiceCommand> commands = new LinkedList<ServiceCommand>();

    public CompositeServiceCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public void addCommand(ServiceCommand command) {
        commands.add(command);
    }

    public void removeCommand(ServiceCommand command) {
        commands.remove(command);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        Bundle params = extras;
        for (ServiceCommand command : commands) {
            params = command.perform(params);
        } return null;
    }

    @Override
    public void execute(Bundle bundle) {
        for (Map.Entry<Command, Bundle> commandBundleEntry : commands.entrySet()) {
            Command command = commandBundleEntry.getKey();
            try {
                command.execute(commandBundleEntry.getValue());
            } catch (Exception e) {
                break;
            }
        }
        commands.clear();
    }
}
