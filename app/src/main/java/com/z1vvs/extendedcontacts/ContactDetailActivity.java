package com.z1vvs.extendedcontacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.z1vvs.extendedcontacts.data.local.Contact;
import com.z1vvs.extendedcontacts.viewmodel.ContactViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ContactDetailActivity extends AppCompatActivity {

    private Contact contact;
    private ContactViewModel viewModel;

    private ImageView ivPhoto, btnFavorite;
    private TextView tvName, tvPhone, tvEmail, tvGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        contact = (Contact) getIntent().getSerializableExtra("contact");

        if (contact == null) {
            finish();
            return;
        }

        initViews();
        setupData();

        // Observe contact changes to update UI automatically
        viewModel.getContactById(contact.id).observe(this, updatedContact -> {
            if (updatedContact != null) {
                this.contact = updatedContact;
                setupData();
            }
        });
    }

    private void initViews() {
        ivPhoto = findViewById(R.id.ivContactPhotoLarge);
        tvName = findViewById(R.id.tvDetailName);
        tvPhone = findViewById(R.id.tvDetailPhone);
        tvEmail = findViewById(R.id.tvDetailEmail);
        tvGroup = findViewById(R.id.tvDetailGroup);
        btnFavorite = findViewById(R.id.btnFavorite);
        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        findViewById(R.id.btnCallAction).setOnClickListener(v -> makeCall());
        findViewById(R.id.btnMessageAction).setOnClickListener(v -> sendMessage());
        findViewById(R.id.btnEmailAction).setOnClickListener(v -> sendEmail());
        findViewById(R.id.btnDeleteContact).setOnClickListener(v -> confirmDelete());
        findViewById(R.id.fabEdit).setOnClickListener(v -> editContact());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void setupData() {
        tvName.setText(contact.name);
        tvPhone.setText(contact.phone);
        
        if (contact.email != null && !contact.email.isEmpty()) {
            tvEmail.setText(contact.email);
            findViewById(R.id.btnEmailAction).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.btnEmailAction).setVisibility(View.GONE);
        }

        if (contact.groupName != null && !contact.groupName.isEmpty()) {
            tvGroup.setText(contact.groupName);
            tvGroup.setVisibility(View.VISIBLE);
        } else {
            tvGroup.setVisibility(View.GONE);
        }

        if (contact.photoUri != null) {
            Glide.with(this)
                    .load(Uri.parse(contact.photoUri))
                    .placeholder(R.drawable.ic_person)
                    .centerCrop()
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.ic_person);
        }

        btnFavorite.setImageResource(contact.isFavorite
                ? R.drawable.ic_star_filled
                : R.drawable.ic_star_border);
    }

    private void makeCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + contact.phone));
        startActivity(intent);
    }

    private void sendMessage() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + contact.phone));
        startActivity(intent);
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + contact.email));
        startActivity(Intent.createChooser(intent, "Send email"));
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_contact)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteContact(contact);
                    Toast.makeText(this, R.string.contact_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void editContact() {
        Intent intent = new Intent(this, AddContactActivity.class);
        intent.putExtra("contact", contact);
        startActivity(intent);
        // Removed finish() to keep detail screen in the back stack
    }

    private void toggleFavorite() {
        boolean willBeFavorite = !contact.isFavorite;
        viewModel.toggleFavorite(contact);
        int messageResId = willBeFavorite ? R.string.added_to_favorites : R.string.removed_from_favorites;
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}