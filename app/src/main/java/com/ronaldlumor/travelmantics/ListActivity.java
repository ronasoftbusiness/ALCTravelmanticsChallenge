package com.ronaldlumor.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        MenuItem item = menu.findItem(R.id.new_deal_menu);
        if(FirebaseUtil.isAdmin){
            item.setVisible(true);
        }else{
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.new_deal_menu:
                startActivity(new Intent(ListActivity.this, InsertActivity.class));
                return true;

            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getApplicationContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
                            }
                        });
                FirebaseUtil.mFirebaseAuth.signOut();
                FirebaseUtil.detachAuthListener();
                startActivity(new Intent(ListActivity.this, PermissionActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showMenu(){
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachAuthListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUtil.openFbReference("traveldeals", ListActivity.this);

        RecyclerView rv = findViewById(R.id.recyclerView);
        final DealAdapter dealAdapter = new DealAdapter();
        rv.setAdapter(dealAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(new DividerItemDecoration(rv.getContext(), LinearLayoutManager.VERTICAL));
        FirebaseUtil.attachAuthListener();
        invalidateOptionsMenu();
    }
}
