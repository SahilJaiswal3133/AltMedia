package com.memeland.altmedia.ui.notifications;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.memeland.altmedia.MainActivity;
import com.memeland.altmedia.R;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import static android.app.Activity.RESULT_OK;
//import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class NotificationsFragment extends Fragment {

   FirebaseAuth firebaseAuth;
   FirebaseUser currentuser;
   FirebaseDatabase firebaseDatabase;
   DatabaseReference databaseReference;
   ImageView avatartv,covertv;
   TextView nametv,signaturenametv;
   ProgressDialog pd;
   private FloatingActionButton floatingactionbtn;
    String uid;
    StorageReference storageReference;
    String storagePath="Users_Profile_Cover_Imgs/";
    private  static  final int CAMERA_REQUEST_CODE=100;
    private  static  final int STORAGE_REQUEST_CODE=200;
    private  static  final int IMAGE_PICK_CAMERA_CODE=400;
    private  static  final int IMAGE_PICK_GALLEY_CODE=300;
    String cameraPermissions[];
    String storagePermissions[];
    Uri image_uri;
    String profileorCoverPhoto;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        //currentuser=firebaseAuth.getCurrentUser();
        currentuser = FirebaseAuth.getInstance().getCurrentUser();

        firebaseAuth = FirebaseAuth.getInstance();
        pd=new ProgressDialog(getContext());
       // storageReference = getInstance().getReference();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");

        avatartv=view.findViewById(R.id.avatarTv);
        nametv=view.findViewById(R.id.nametv);
        signaturenametv=view.findViewById(R.id.signaturetv);
        covertv=view.findViewById(R.id.covertv);
        floatingactionbtn =view.findViewById(R.id.floatingprofileupdate);
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Query query=databaseReference.orderByChild("email").equalTo(currentuser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String signaturename=""+ds.child("Signaturename").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("covertv").getValue();
                     uid=""+ds.child("uid").getValue();


                    nametv.setText(name);
                    signaturenametv.setText(signaturename);
                    try {
                        Picasso.get().load(image).into(avatartv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_baseline_person_24).into(avatartv);

                    }
                    try {
                        Picasso.get().load(cover).into(covertv);
                    }catch (Exception e2){
                        Picasso.get().load(R.drawable.bggradient).into(covertv);
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        floatingactionbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
                //checkUserStatus();
            }
        });



        return view;
    }

    private void showEditProfileDialog() {

            String options[]={"Edit Name" , "Edit Signature Name" , "Edit Profile Picture","Edit Cover Picture"};
            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
            builder.setTitle("Choose Action");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which==0){
                        pd.setMessage("Updating Name");
                        showNameDialog("name");

                    }
                    else if(which==1){
                        pd.setMessage("Updating Signature Name");
                        showNameDialog("Signaturename");

                    }
                    else if(which==2){
                        pd.setMessage("Updating Profile Picture");
                        profileorCoverPhoto="image";
                        showImagePicDialog();

                    }
                    else if(which==3){
                        pd.setMessage("Updating Cover Picture");
                        profileorCoverPhoto="cover";
                        showImagePicDialog();

                    }
                }
            });
            builder.create().show();
        }

    private void showImagePicDialog() {
        String options[]={"Camera" , "Gallery" };
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                }
                else if(which==1){
                    if(!checkStoragePermissio()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent galleryintent =new Intent(Intent.ACTION_PICK);
        galleryintent.setType("image/*");
        startActivityForResult(galleryintent,IMAGE_PICK_GALLEY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descriptions");
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraintent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraintent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraintent,IMAGE_PICK_CAMERA_CODE);
    }

    private void showNameDialog(String Key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update" + "" + Key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter" + Key);
        linearLayout.addView(editText);
        linearLayout.setPadding(10, 10, 10, 10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Updated", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value=editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object>result=new HashMap<>();
                    result.put(Key,value);
                    databaseReference.child(currentuser.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), "Please enter"+Key, Toast.LENGTH_SHORT).show();
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
    private  boolean checkStoragePermissio() {
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return  result;
    }
    private void  requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(),storagePermissions,STORAGE_REQUEST_CODE);
    }
    private  boolean checkCameraPermission() {
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return  result && result1;
    }
    private void  requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(),cameraPermissions,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case  CAMERA_REQUEST_CODE:
            {
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean WRITEsTORAGEAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && WRITEsTORAGEAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean WRITEsTORAGEAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(WRITEsTORAGEAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //image_uri=data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_GALLEY_CODE){
                image_uri=data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        pd.show();
        String filePathAndName=storagePath+""+profileorCoverPhoto+"_"+currentuser.getUid();
        StorageReference storageReference2nd=storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        final Uri downloaduri = uriTask.getResult();
                        if(uriTask.isSuccessful()) {
                            HashMap<Object,String> hashMap=new HashMap<>();
                            HashMap<String,Object> results=new HashMap<>();
                            results.put(profileorCoverPhoto,downloaduri.toString());
                              databaseReference.child(currentuser.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                                  @Override
                                  public void onSuccess(Void aVoid) {
                                      pd.dismiss();
                                      Toast.makeText(getActivity(), "updated", Toast.LENGTH_SHORT).show();

                                  }
                              }).addOnFailureListener(new OnFailureListener() {
                                  @Override
                                  public void onFailure(@NonNull Exception e) {
                                      pd.dismiss();
                                      Toast.makeText(getActivity(), "some error occured", Toast.LENGTH_SHORT).show();
                                  }
                              });
                        }
                        else {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "some error eccored" , Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), "thats error", Toast.LENGTH_SHORT).show();
                //Toast.makeText(getActivity(), "Error"+e, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
