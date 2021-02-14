package com.memeland.altmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    private Button btnregistered;
    private EditText edemailsigin;
    private EditText edpasssignin;
    private Button btnsigin;
    FirebaseAuth mAuth;
    private ProgressBar progressBarsignin;
    private CheckBox checkBoxsign1;
    private TextView txtforget;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        btnregistered = findViewById(R.id.btnregistered);
        edemailsigin = findViewById(R.id.emailsigninet);
        edpasssignin = findViewById(R.id.passsigninet);
        btnsigin = findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        txtforget = findViewById(R.id.txtforget);
        progressBarsignin = findViewById(R.id.regiprogreebarsignin);
        checkBoxsign1 = findViewById(R.id.checkBoxsiginin);
        btnregistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPage.this, Registered.class);
                startActivity(intent);
                finish();
            }
        });
        checkBoxsign1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if (b) {
                    edpasssignin.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                } else {
                    edpasssignin.setTransformationMethod(PasswordTransformationMethod.getInstance());

                }
            }
        });
        btnsigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edemailsigin.getText().toString().trim();
                String pass = edpasssignin.getText().toString().trim();
                if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(pass)) {
                    progressBarsignin.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendtomain();
                                progressBarsignin.setVisibility(View.INVISIBLE);
                            } else {
                                String error=task.getException().getMessage();
                                Toast.makeText(LoginPage.this, "Error:"+error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginPage.this, "Error" + e, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LoginPage.this, "Empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
   txtforget.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
           showRecoverPasswordDialog();
       }
   });
    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout=new LinearLayout(this);
        final EditText Reditemail=new EditText(this);
        Reditemail.setHint("Email");
        Reditemail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(Reditemail);
        Reditemail.setMinEms(16);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton( "Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final      String Remail=Reditemail.getText().toString();
                if(!Patterns.EMAIL_ADDRESS.matcher(Remail).matches()){
                    Reditemail.setError("Invalid Email");
                    Reditemail.setFocusable(true);
                }
                else {

                    beginRecovery(Remail);

                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();



    }

    private void beginRecovery(String email) {
        progressDialog.setMessage("Sending Email");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(LoginPage.this, "Email Sent", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(LoginPage.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginPage .this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void sendtomain() {
             Intent sendintent=new Intent(LoginPage.this,AltHome.class);
             startActivity(sendintent);
             finish();
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
       updateUI(currentUser);

    }
    private void updateUI(FirebaseUser currentUser) {

        if (currentUser != null) {
            startActivity(new Intent(LoginPage.this, AltHome.class));
            finish();

        }
    }

}

