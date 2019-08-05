package com.ronaldlumor.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

public class PermissionActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            requestPermission();
        }else{
            startActivity(new Intent(PermissionActivity.this, ListActivity.class));
        }
    }

    private void requestPermission(){
        String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSIONS_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
                startActivity(new Intent(PermissionActivity.this, ListActivity.class));
            }
        }else{
            requestPermission();
        }
    }
}
