package com.memeland.altmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.memeland.altmedia.ui.home.HomeFragment;

import java.util.HashMap;

public class Registered extends AppCompatActivity {

    private Button btnsigin;
    private EditText editpass;
    private EditText editemail;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;
    private CheckBox checkBox;
    private EditText username;
    FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);
        TextView txtloginpage=findViewById(R.id.txtloginpage);
        editemail = findViewById(R.id.editregemail);
        editpass = findViewById(R.id.regeditpass);
        username = findViewById(R.id.regeditname);
        btnsigin = findViewById(R.id.btnsignup);
        progressBar = findViewById(R.id.regiprogreebar);
        checkBox = findViewById(R.id.checkBox2);
        mAuth = FirebaseAuth.getInstance();
        txtloginpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Registered.this,LoginPage.class);
                startActivity(intent);
                finish();
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if (b) {
                    editpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                } else {
                    editpass.setTransformationMethod(PasswordTransformationMethod.getInstance());

                }
            }
        });
        btnsigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String  email = editemail.getText().toString().trim();
                String username1 =username.getText().toString().trim();
                String password = editpass.getText().toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editemail.setError("Invalid Email");
                    editemail.setFocusable(true);
                } else if (password.length() < 6) {
                    editpass.setError("Password length atleast 6 characters");
                    editpass.setFocusable(true);
                }else   if (username.length()<2){
                    username.setError("Enter Name");
                    username.setFocusable(true);

                }
                else{
                    registeruser(email, password);
                }
            }
        });
    }

    private void registeruser(String email, String password) {
progressBar.setVisibility(View.VISIBLE);
mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if(task.isSuccessful()){
            FirebaseUser user=mAuth.getCurrentUser();
            String email=user.getEmail();
            String uid=user.getUid();
          //  String username1=user.getDisplayName();
            HashMap<Object ,String> hashMap=new HashMap<>();
            hashMap.put("email",email);
            hashMap.put("uid",uid);
            hashMap.put("name","");
            hashMap.put("image","");
            hashMap.put("cover","");
            hashMap.put("signaturename","");
            FirebaseDatabase database =FirebaseDatabase.getInstance();
            DatabaseReference reference=database.getReference("Users");
            reference.child(uid).setValue(hashMap);
            sendtomain();
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(Registered.this, "Welcome", Toast.LENGTH_SHORT).show();
        }
           updateUI(currentUser);
    }
});
    }

    private void sendtomain() {
        Intent sendintent=new Intent(Registered.this,AltHome.class);
        startActivity(sendintent);
        finish();

    }
    private void updateUI(FirebaseUser currentUser) {

        if (currentUser != null) {
            startActivity(new Intent(Registered.this, AltHome.class));
            finish();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(currentUser);
    }
}