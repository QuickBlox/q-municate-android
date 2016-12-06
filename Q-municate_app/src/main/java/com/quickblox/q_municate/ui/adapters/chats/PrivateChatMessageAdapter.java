package com.quickblox.q_municate.ui.adapters.chats;


import android.content.Context;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.ui.kit.chatmessage.adapter.QBMessagesAdapter;

import java.util.List;

public class PrivateChatMessageAdapter extends QBMessagesAdapter {
    private static final String TAG = PrivateChatMessageAdapter.class.getSimpleName();

    public PrivateChatMessageAdapter(Context context, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
    }
}