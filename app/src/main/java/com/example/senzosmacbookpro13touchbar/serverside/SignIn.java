package com.example.senzosmacbookpro13touchbar.serverside;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import Common.Common;
import Model.User;

public class SignIn extends AppCompatActivity {
    EditText edtPhone,edtPassword;
    Button btnSignIn;

    FirebaseDatabase db;
    DatabaseReference users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword=(EditText) findViewById(R.id.password_editText);
        edtPhone=(EditText) findViewById(R.id.username_editText);

        btnSignIn=(Button)findViewById(R.id.loginbutton);

        //firebase
        db=FirebaseDatabase.getInstance();
        users=db.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser(edtPhone.getText().toString(),edtPassword.getText().toString());
            }
        });


    }

    private void signInUser(String phone, String password) {
        final ProgressDialog mDialog=new ProgressDialog(SignIn.this);
        mDialog.setMessage("Please wait...");
        mDialog.show();

        final String localPhone= phone;
        final String localPassword= password;
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(localPhone).exists()) {
                    mDialog.dismiss();
                    User user = dataSnapshot.child(localPhone).getValue(User.class);
                    user.setPhone(localPhone);
                    if (Boolean.parseBoolean(user.getIsStaff())) {
                        if (user.getPassword().equals(localPassword)) {
                            //login ok

                            Intent login=new Intent(SignIn.this,Home.class);
                            Common.currentUser=user;
                            startActivity(login);
                            finish();
                        } else
                            Toast.makeText(SignIn.this, "Wrong password", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(SignIn.this, "Please use client account", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mDialog.dismiss();
                    Toast.makeText(SignIn.this,"admin account does not exist",Toast.LENGTH_SHORT).show();



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
