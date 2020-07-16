package com.example.madcampweek2.ui.contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madcampweek2.R;

import java.util.ArrayList;


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

    // Data(Contact class) list for adapter
    private ArrayList<Contact> listData = new ArrayList<>();

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate contact.xml via LayoutInflater
        // return ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item,
                parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        // RecyclerView Item number
        return listData.size();
    }

    void addItem(Contact data) {
        listData.add(data);
    }

    // ViewHolder of RecyclerView
    // Setting subViews consisting the ItemView
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView textView1;
        private TextView textView2;
        private ImageView imageView;

        ItemViewHolder(View itemView) {
            super(itemView);

            textView1 = itemView.findViewById(R.id.name);
            textView2 = itemView.findViewById(R.id.phone);
            imageView = itemView.findViewById(R.id.profile);
        }

        void onBind(Contact data) {
            textView1.setText(data.getName());
            textView2.setText(data.getPhoneNumber());
            imageView.setImageResource(data.getProfile());
        }
    }
}