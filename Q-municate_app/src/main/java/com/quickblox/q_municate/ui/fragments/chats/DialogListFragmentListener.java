package com.quickblox.q_municate.ui.fragments.chats;

import com.quickblox.q_municate.ui.activities.base.BaseActivity;

/**
 * Created by roman on 4/18/17.
 */

public interface DialogListFragmentListener {

    void setLoadChatsSuccessActionCallback(BaseActivity.LoadChatsSuccessActionCallback loadChatsSuccessActionCallback);

    void removeLoadChatsSuccessActionCallback(BaseActivity.LoadChatsSuccessActionCallback loadChatsSuccessActionCallback);
}