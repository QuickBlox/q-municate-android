package com.quickblox.q_municate_core.legacy.core.command;

import android.os.Bundle;

public interface Command {

    void execute(Bundle bundle) throws Exception;
}