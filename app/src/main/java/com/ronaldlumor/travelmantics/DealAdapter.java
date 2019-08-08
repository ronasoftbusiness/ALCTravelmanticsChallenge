package com.ronaldlumor.travelmantics;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder>{
    private ImageView mImageView;

    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    ChildEventListener mChildEventListener;
    ArrayList<TravelDeal> deals;

    public DealAdapter() {
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        deals = FirebaseUtil.mDeals;
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal deal = dataSnapshot.getValue(TravelDeal.class);
                deal.setId(dataSnapshot.getKey());
                deals.add(deal);
                notifyItemInserted(deals.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                TravelDeal deal = dataSnapshot.getValue(TravelDeal.class);
                deals.remove(deal);
                notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                notifyDataSetChanged();
            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deal, null, false);

        return new DealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        holder.bind(deals.get(position));
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtTitle;
        TextView txtDescription;
        TextView txtPrice;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            mImageView = itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal){
            txtTitle.setText(deal.getTitle());
            txtDescription.setText(deal.getDescription());
            txtPrice.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            TravelDeal travelDeal = deals.get(position);
            Intent intent = new Intent(v.getContext(), InsertActivity.class);
            intent.putExtra("travelDeal", travelDeal);
            v.getContext().startActivity(intent);
        }

        private void showImage(String url){
            if(url != null && !url.isEmpty()){

                Picasso.with(mImageView.getContext())
                        .load(url)
                        .resize(90, 90)
                        .centerCrop()
                        .into(mImageView);
            }
        }
    }
}
