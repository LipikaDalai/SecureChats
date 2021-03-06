package com.example.securechats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.securechats.Adapter.MessageAdapter;
import com.example.securechats.Aes.AesAlgo;
import com.example.securechats.Fragments.APIService;
import com.example.securechats.Model.Chat;
import com.example.securechats.Model.User;
import com.example.securechats.Notifications.RetrofitInstance;
import com.example.securechats.Notifications.Data;
import com.example.securechats.Notifications.Sender;
import com.example.securechats.Notifications.OnlineCheck;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    ImageView profile_picture;
    TextView username;
    ImageView send_button;
    EditText message;
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView;
    Intent intent;
    ValueEventListener seenListener;
    APIService apiService;
    boolean notify = false;

    FirebaseUser firebaseUser;
    DatabaseReference reference;



    AesAlgo aes = new AesAlgo("lv39eptlvuhaqqsr");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatdetail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                finish();
                startActivity(new Intent(ChatDetailActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });


        apiService = RetrofitInstance.getRetrofit("https:/fcm.googleapis.com/").create(APIService.class);


        recyclerView = findViewById(R.id.msg_recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        profile_picture = findViewById(R.id.profile_pic);
        username = findViewById(R.id.username);

        send_button = findViewById(R.id.send_button);
        message = findViewById(R.id.message_box);

        // https://www.scaledrone.com/blog/android-chat-tutorial/#chatbubblesentbyus
        // implementing a drawing resource for look and feel of chat bubble
        // is it necessary ?


        intent = getIntent();
        final String userId = intent.getStringExtra("userId");



        System.out.println("userid in oncreate - " + userId);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                notify = true;

                String msg = message.getText().toString();
                if(!msg.equals("")) {
                    sendMessage(firebaseUser.getUid(), userId, msg);
                } else {
                    Toast.makeText(ChatDetailActivity.this, "Empty message", Toast.LENGTH_SHORT).show();
                }
                message.setText("");
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());

                if (user.getImgURL().equals("default")) {
                    profile_picture.setImageResource(R.drawable.ic_user);
                } else {
                    Glide.with(ChatDetailActivity.this).load(user.getImgURL()).into(profile_picture);
                }


                readMessage(firebaseUser.getUid(), userId, user.getImgURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        System.out.println(userId);
        seenMessage(userId);

    }



    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Chat chat = snap.getValue(Chat.class);

                    assert chat != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        snap.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    private void sendMessage(String sender, final String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        final String userId = intent.getStringExtra("userId");
        System.out.println("========================= GET STRING EXTRA in SENDMESSAGE - " + userId);

//        final String userid = intent.getStringExtra("userid"); //----> YES
        Date date = new Date();

        String encryptedMessage = null;

        try {
            encryptedMessage = aes.encrypt(message);
        } catch (Exception e) {
//            Logger.getLogger(AESCrypt.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        }

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", encryptedMessage);
//        hashMap.put("isSeen", false);
//        hashMap.put("timestamp", date.toString());

        reference.child("Chats").push().setValue(hashMap);

//        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid()).child(receiver);
        assert userId != null;
        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid()).child(userId);

        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
//                    chatReference.child("id").setValue(receiver);
                    chatReference.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }

                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(String receiver, final String username, String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");

        final String userId = intent.getStringExtra("userId");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    OnlineCheck token = snap.getValue(OnlineCheck.class);
                    Data data = new Data(firebaseUser.getUid(), R.drawable.ic_user, username + ": " + message, "New Encrypted Message", userId);

                    Sender sender = new Sender(data, token.getToken());

                   /* apiService.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Toast.makeText(ChatDetailActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    }); */

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void readMessage(final String myid, final String userId, final String imgURL) {
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for(DataSnapshot snap : snapshot.getChildren()) {
                    Chat chat = snap.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(myid)) {
                        mChat.add(chat);
                    }
                    messageAdapter = new MessageAdapter(ChatDetailActivity.this, mChat, imgURL);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }
}