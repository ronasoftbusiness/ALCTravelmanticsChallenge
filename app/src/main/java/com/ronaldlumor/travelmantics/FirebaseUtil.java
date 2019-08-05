package com.ronaldlumor.travelmantics;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    private static final int RC_SIGN_IN = 113;

    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseAuth.AuthStateListener mAuthStateListener;

    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;

    public static FirebaseStorage mFirebaseStorage;
    public static StorageReference mStorageReference;

    public static ChildEventListener mChildEventListener;
    private static FirebaseUtil firebaseUtil;
    public static ArrayList<TravelDeal> mDeals;

    public static ListActivity caller;
    public static boolean isAdmin = false;

    private FirebaseUtil(){}

    public static void openFbReference(String ref, final ListActivity callerActivity){
        if(firebaseUtil == null){
            firebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();

            caller = callerActivity;
            mFirebaseAuth = FirebaseAuth.getInstance();
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(mFirebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    }else{
                        String userId = mFirebaseAuth.getUid();
                        checkAdmin(userId);
                        Toast.makeText(callerActivity.getBaseContext(), "Welcome User!", Toast.LENGTH_LONG).show();
                    }
                }
            };
        }
        mDeals = new ArrayList<>();
        mDatabaseReference = mFirebaseDatabase.getReference().child("traveldeals");
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
        connectStorage();
    }

    public static void connectStorage(){
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("traveldeals_photos");
    }

    private static void signIn(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    private static void checkAdmin(String uid){
        FirebaseUtil.isAdmin = false;
        DatabaseReference ref = mFirebaseDatabase.getReference().child("administrators")
                .child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin = true;
                caller.showMenu();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(listener);
    }

    private static void signOut(){
        mFirebaseAuth.signOut();
    }

    public static void attachAuthListener(){
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    public static void detachAuthListener(){
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
}
