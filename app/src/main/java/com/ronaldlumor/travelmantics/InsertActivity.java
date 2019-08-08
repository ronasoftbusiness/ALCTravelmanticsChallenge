package com.ronaldlumor.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

public class InsertActivity extends AppCompatActivity {
    private static final int IMAGE_RESULT = 33;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    EditText txtTitle, txtPrice, txtDescription;
    ImageView photoView;
    Button mImageButton;
    TravelDeal deal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        mImageButton = findViewById(R.id.selectImageButton);
        photoView = findViewById(R.id.photoView);


        deal = (TravelDeal) getIntent().getSerializableExtra("travelDeal");
        if(deal == null){
            deal = new TravelDeal();
        }
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_RESULT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if(requestCode == IMAGE_RESULT && resultCode == RESULT_OK){
            final StorageReference ref = FirebaseUtil.mStorageReference.child(data.getData().getLastPathSegment());

            UploadTask uploadTask = ref.putFile(data.getData());
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downUri = task.getResult();
                        deal.setImageUrl(downUri.toString());
                        showImage(downUri.toString());
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.insert_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete_menu).setVisible(true);
            enableEditText(true);
            mImageButton.setEnabled(true);
        }else{
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_menu).setVisible(false);
            enableEditText(false);
            mImageButton.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved successfully.", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;

            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal deleted successfully.", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;

            case R.id.logout_menu:
                AuthUI.getInstance().signOut(InsertActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUtil.attachAuthListener();
                    }
                });
                FirebaseUtil.detachAuthListener();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveDeal(){
        deal.setTitle(txtTitle.getText().toString());
        deal.setPrice(txtPrice.getText().toString());
        deal.setDescription(txtDescription.getText().toString());

        if(deal.getId() == null){
            mDatabaseReference.push().setValue(deal);
        }else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
        Toast.makeText(this, "Fill all fields!", Toast.LENGTH_LONG).show();
    }

    private void deleteDeal(){
        if(deal == null){
            Toast.makeText(this, "Please save the deal before deleting.", Toast.LENGTH_LONG).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        if(deal.getImageName() != null && !deal.getImageName().isEmpty()){
            StorageReference picRef = FirebaseUtil.mFirebaseStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void backToList(){
        onBackPressed();
    }

    private void clean(){
        txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        txtTitle.requestFocus();
    }

    private void enableEditText(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
    }

    private void showImage(String url){
        if(url != null) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;

            Picasso.with(this)
                    .load(url)
                    .resize(160, 160)
                    //.resize(width, (width * 2) / 3)
                    .centerCrop()
                    .into(photoView);

        }
    }
}