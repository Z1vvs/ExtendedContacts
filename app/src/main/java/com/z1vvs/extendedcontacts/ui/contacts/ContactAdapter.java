package com.z1vvs.extendedcontacts.ui.contacts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.z1vvs.extendedcontacts.R;
import com.z1vvs.extendedcontacts.data.local.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Contact> contacts = new ArrayList<>();
    private final Context context;
    private final OnFavoriteClickListener favoriteListener;

    public ContactAdapter(Context context, OnFavoriteClickListener favoriteListener) {
        this.context = context;
        this.favoriteListener = favoriteListener;
    }
    
    @SuppressLint("NotifyDataSetChanged")
    public void setContacts(List<Contact> list) {
        contacts = list;
        notifyDataSetChanged();
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Contact contact);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone, group;
        ImageView favoriteBtn, photo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            phone = itemView.findViewById(R.id.tvPhone);
            group = itemView.findViewById(R.id.tvGroup);
            favoriteBtn = itemView.findViewById(R.id.btnFavorite);
            photo = itemView.findViewById(R.id.ivContactPhoto);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact c = contacts.get(position);

        holder.name.setText(c.name);
        holder.phone.setText(c.phone);

        if (c.groupName != null && !c.groupName.isEmpty()) {
            holder.group.setText(c.groupName);
            holder.group.setVisibility(View.VISIBLE);
        } else {
            holder.group.setVisibility(View.GONE);
        }

        if (c.photoUri != null) {
            Glide.with(context)
                    .load(Uri.parse(c.photoUri))
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(holder.photo);
        } else {
            holder.photo.setImageResource(R.drawable.ic_person);
        }

        holder.favoriteBtn.setImageResource(c.isFavorite 
                ? R.drawable.ic_star_filled 
                : R.drawable.ic_star_border);

        holder.favoriteBtn.setOnClickListener(v -> {
            if (favoriteListener != null) {
                favoriteListener.onFavoriteClick(c);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.z1vvs.extendedcontacts.ContactDetailActivity.class);
            intent.putExtra("contact", c);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}