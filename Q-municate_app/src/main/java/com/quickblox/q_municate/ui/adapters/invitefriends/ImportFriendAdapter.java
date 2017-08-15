package com.quickblox.q_municate.ui.adapters.invitefriends;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.models.InviteFriend;

import java.util.List;

public class ImportFriendAdapter extends RecyclerView.Adapter<ImportFriendAdapter.ImportFriendViewHolder> {

    private final Context context;
    private final List<InviteFriend> inviteFriendsList;

    public ImportFriendAdapter(Context context, List<InviteFriend> inviteFriendsList) {
        this.context = context;
        this.inviteFriendsList = inviteFriendsList;

    }

    @Override
    public ImportFriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImportFriendViewHolder viewHolder = new ImportFriendViewHolder(LayoutInflater.from(context).inflate(R.layout.item_import_contact, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ImportFriendViewHolder holder, int position) {
        InviteFriend inviteFriend = inviteFriendsList.get(position);
        holder.contactsNameTextView.setText(inviteFriend.getName());
        holder.qmNameTextView.setText(inviteFriend.getQbName());
        holder.qmIdentificatorTextView.setText(inviteFriend.getId());

        ImageLoaderUtils.displayAvatarImageByUri(context, inviteFriend.getUri(), holder.contactsAvatarImageView);
        ImageLoaderUtils.displayAvatarImageByLink(context, inviteFriend.getQbAvatarUrl(), holder.qmAvatarImageView);
    }


    @Override
    public int getItemCount() {
        return inviteFriendsList.size();
    }

    protected static class ImportFriendViewHolder extends RecyclerView.ViewHolder{

        public RoundedImageView contactsAvatarImageView;
        public RoundedImageView qmAvatarImageView;
        public ImageView addToFriendsImageView;
        public TextView contactsNameTextView;
        public TextView qmNameTextView;
        public TextView qmIdentificatorTextView;


        public ImportFriendViewHolder(View itemView) {
            super(itemView);
            contactsAvatarImageView = (RoundedImageView) itemView.findViewById(R.id.contacts_avatar);
            qmAvatarImageView = (RoundedImageView) itemView.findViewById(R.id.qm_user_avatar);
            addToFriendsImageView = (ImageView) itemView.findViewById(R.id.add_to_friends);
            contactsNameTextView = (TextView) itemView.findViewById(R.id.contacts_name);
            qmNameTextView = (TextView) itemView.findViewById(R.id.qm_name);
            qmIdentificatorTextView = (TextView) itemView.findViewById(R.id.via_value);
        }
    }
}
