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
import com.quickblox.q_municate.utils.listeners.CounterChangedListener;
import com.quickblox.q_municate.utils.listeners.UserOperationListener;
import com.quickblox.q_municate_core.models.InviteFriend;

import java.util.ArrayList;
import java.util.List;

public class ImportFriendAdapter extends RecyclerView.Adapter<ImportFriendAdapter.ImportFriendViewHolder> {

    private final Context context;
    private final List<InviteFriend> inviteFriendsList;
    private CounterChangedListener counterChangedListener;
    private final UserOperationListener userOperationListener;
    private List<Integer> selectedFriends = new ArrayList<>();

    public ImportFriendAdapter(Context context, List<InviteFriend> inviteFriendsList, UserOperationListener userOperationListener) {
        this.context = context;
        this.inviteFriendsList = inviteFriendsList;
        this.userOperationListener = userOperationListener;
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

        initListeners(holder, inviteFriend.getQbId());

        if (selectedFriends.isEmpty()){
            holder.addToFriendsImageView.setVisibility(View.VISIBLE);
        } else {
            holder.addToFriendsImageView.setVisibility(View.INVISIBLE);
        }

        if (selectedFriends.contains(inviteFriend.getQbId())){
            holder.getMainView().setBackgroundColor(context.getResources().getColor(R.color.light_gray));
        } else {
            holder.getMainView().setBackgroundColor(context.getResources().getColor(R.color.bg_main));
        }
    }

    private void initListeners(final ImportFriendViewHolder holder, final Integer userId) {
        holder.addToFriendsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userOperationListener.onAddUserClicked(userId);
            }
        });

            holder.getMainView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (selectedFriends.isEmpty()) {
                        selectedFriends.add(userId);
                        counterChangedListener.onCounterContactsChanged(selectedFriends.size());
                        holder.getMainView().setBackgroundColor(context.getResources().getColor(R.color.light_gray));
                        notifyDataSetChanged();
                    }
                    return true;
                }
            });
            holder.getMainView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedFriends.isEmpty()){
                        return;
                    }

                    if (selectedFriends.contains(userId)){
                        selectedFriends.remove(userId);
//                        holder.addToFriendsImageView.setVisibility(View.VISIBLE);
                        holder.getMainView().setBackgroundColor(context.getResources().getColor(R.color.bg_main));
                    } else {
                        selectedFriends.add(userId);
//                        holder.addToFriendsImageView.setVisibility(View.INVISIBLE);
                        holder.getMainView().setBackgroundColor(context.getResources().getColor(R.color.light_gray));
                    }

                    counterChangedListener.onCounterContactsChanged(selectedFriends.size());
                }
            });
    }


    @Override
    public int getItemCount() {
        return inviteFriendsList.size();
    }

    public List<Integer> getSelectedFriends(){
        return selectedFriends;
    }

    public void selectAllFriends(){
        for (InviteFriend inviteFriend : inviteFriendsList){
            if (!selectedFriends.contains(inviteFriend.getQbId())){
                selectedFriends.add(inviteFriend.getQbId());
            }
        }
        notifyDataSetChanged();
    }

    public void setCounterChangedListener(CounterChangedListener counterChangedListener) {
        this.counterChangedListener = counterChangedListener;
    }

    protected static class ImportFriendViewHolder extends RecyclerView.ViewHolder{

        private final View mainView;
        public RoundedImageView contactsAvatarImageView;
        public RoundedImageView qmAvatarImageView;
        public ImageView addToFriendsImageView;
        public TextView contactsNameTextView;
        public TextView qmNameTextView;
        public TextView qmIdentificatorTextView;


        public ImportFriendViewHolder(View itemView) {
            super(itemView);
            this.mainView = itemView;
            contactsAvatarImageView = (RoundedImageView) itemView.findViewById(R.id.contacts_avatar);
            qmAvatarImageView = (RoundedImageView) itemView.findViewById(R.id.qm_user_avatar);
            addToFriendsImageView = (ImageView) itemView.findViewById(R.id.add_to_friends);
            contactsNameTextView = (TextView) itemView.findViewById(R.id.contacts_name);
            qmNameTextView = (TextView) itemView.findViewById(R.id.qm_name);
            qmIdentificatorTextView = (TextView) itemView.findViewById(R.id.via_value);
        }

        public View getMainView(){
            return mainView;
        }
    }
}
