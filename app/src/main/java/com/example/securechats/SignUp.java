package com.example.securechats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private EditText username, email, password;
    private Button register_button;
    TextView haveacc;
    ProgressDialog progressDialog;

    FirebaseAuth auth;
    DatabaseReference reference;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        register_button = findViewById(R.id.registerButton);
        haveacc = findViewById(R.id.already_account);

        progressDialog = new ProgressDialog(SignUp.this);
        progressDialog.setTitle("SignUP");
        progressDialog.setMessage("Creating your account");

        haveacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_username = username.getText().toString();
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                if(str_username .isEmpty())
                {
                    username.setError("enter username");
                    return;
                }
                if(str_email .isEmpty())
                {
                    email.setError("enter your email");
                    return;
                }
                if(str_password .isEmpty())
                {
                    password.setError("enter a password");
                    return;
                }

                progressDialog.show();

                if (TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {
                    Toast.makeText(SignUp.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (str_password.length() < 8 ) {
                    Toast.makeText(SignUp.this, "Password should be 8 characters or more", Toast.LENGTH_SHORT).show();
                } else {
                    createAccount(str_username, str_email, str_password);
                }
            }
        });

        if(auth.getCurrentUser() != null)
        {

            Intent intent = new Intent(SignUp.this,MainActivity.class);
            startActivity(intent);

        }

    }

    private void createAccount(final String uname, String emailaddr, String pswd) {
        auth.createUserWithEmailAndPassword(emailaddr, pswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userId = firebaseUser.getUid();
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userId);
                    hashMap.put("username", uname);
                    hashMap.put("imgURL", "default");

                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Intent intent = new Intent(SignUp.this, MainActivity.class);

                                // using flags to control the start and finish of activity
                                // flags are numbers used to denote how activity will finish....
                                // TODO: read more about flags

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(SignUp.this, "Some error occurred...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}