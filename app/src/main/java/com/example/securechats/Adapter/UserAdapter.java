package com.example.securechats.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.securechats.ChatDetailActivity;
import com.example.securechats.Model.Chat;
import com.example.securechats.Model.User;
import com.example.securechats.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    String lastMesaage;

    public UserAdapter(Context mContext, List<User>mUsers, boolean ischat) {
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_demo, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());

        if (user.getImgURL().equals("default")) {
            holder.profile_picture.setImageResource(R.drawable.ic_user);
        } else {
            Glide.with(mContext).load(user.getImgURL()).into(holder.profile_picture);
       }




        if (ischat) {
            checkLastMessage(user.getId(), holder.last_message);
        } else {
            holder.last_message.setVisibility(View.GONE);
        }





        if (ischat) {
            if (user.getStatus().equals("online")) {
                holder.online.setVisibility(View.VISIBLE);
                holder.offline.setVisibility(View.GONE);
            } else {
                holder.online.setVisibility(View.GONE);
                holder.offline.setVisibility(View.GONE);
            }
        } else {
            holder.online.setVisibility(View.GONE);
            holder.offline.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatDetailActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView profile_picture;
        private ImageView online;
        private ImageView offline;
        private  TextView last_message;

        public ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.list_username);
            profile_picture = itemView.findViewById(R.id.user_list_icon);
            online = itemView.findViewById(R.id.online_token);
            offline = itemView.findViewById(R.id.offline_token);
            last_message = itemView.findViewById(R.id.last_message);
        }
    }

    public void checkLastMessage(final String userid, final TextView last_message) {
        lastMesaage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");




        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Chat chat = snap.getValue(Chat.class);


                    if (chat.getReceiver().equals(FirebaseAuth.getInstance().getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(FirebaseAuth.getInstance().getUid())) {
                        lastMesaage = chat.getMessage();

                    }
                }

                switch (lastMesaage) {
                    case "default" :
                        last_message.setText("");
                        break;

                    default:
                        last_message.setText(lastMesaage);
                        break;
                }


                lastMesaage = "default";
             }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
