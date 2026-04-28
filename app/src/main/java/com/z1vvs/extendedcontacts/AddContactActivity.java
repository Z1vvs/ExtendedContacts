package com.z1vvs.extendedcontacts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.z1vvs.extendedcontacts.data.local.Contact;
import com.z1vvs.extendedcontacts.viewmodel.ContactViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddContactActivity extends AppCompatActivity {

    private EditText etName, etPhone, etEmail;
    private AutoCompleteTextView etGroup;
    private ImageView imagePreview;
    private Uri selectedImageUri;
    private Uri cameraImageUri;

    private ContactViewModel viewModel;
    private Contact existingContact;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            });

    private final ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    startCrop(cameraImageUri);
                }
            });

    private final ActivityResultLauncher<Intent> cropImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = UCrop.getOutput(result.getData());
                    imagePreview.setImageURI(selectedImageUri);
                } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                    assert result.getData() != null;
                    Throwable cropError = UCrop.getError(result.getData());
                    if (cropError != null) {
                        Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        existingContact = (Contact) getIntent().getSerializableExtra("contact");

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etGroup = findViewById(R.id.etGroup);

        viewModel.getGroups().observe(this, groups -> {
            if (groups != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, groups);
                etGroup.setAdapter(adapter);
            }
        });
        imagePreview = findViewById(R.id.imagePreview);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            int titleRes = existingContact != null ? R.string.edit_contact : R.string.add_contact;
            getSupportActionBar().setTitle(titleRes);
        }

        FloatingActionButton btnPick = findViewById(R.id.btnPickPhoto);
        Button btnSave = findViewById(R.id.btnSave);

        View.OnClickListener photoClickListener = v -> showPhotoOptions();
        btnPick.setOnClickListener(photoClickListener);
        imagePreview.setOnClickListener(photoClickListener);

        if (existingContact != null) {
            etName.setText(existingContact.name);
            etPhone.setText(existingContact.phone);
            etEmail.setText(existingContact.email);
            etGroup.setText(existingContact.groupName);

            if (existingContact.photoUri != null) {
                selectedImageUri = Uri.parse(existingContact.photoUri);
                imagePreview.setImageURI(selectedImageUri);
            }
        }

        btnSave.setOnClickListener(v -> saveContact());
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void showPhotoOptions() {
        String[] options = {
                getString(R.string.take_photo),
                getString(R.string.choose_photo),
                getString(R.string.remove_photo)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.pick_photo)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            launchCamera();
                            break;
                        case 1:
                            pickImageLauncher.launch("image/*");
                            break;
                        case 2:
                            removePhoto();
                            break;
                    }
                })
                .show();
    }

    private void launchCamera() {
        File photoFile = new File(getCacheDir(), "camera_photo_" + UUID.randomUUID().toString() + ".jpg");
        cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
        takePhotoLauncher.launch(cameraImageUri);
    }

    private void startCrop(Uri uri) {
        String destinationFileName = "cropped_image_" + UUID.randomUUID().toString() + ".jpg";
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));

        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        options.setToolbarTitle(getString(R.string.edit_contact));

        // Sync with App Theme colors
        int colorPrimary = com.google.android.material.color.MaterialColors.getColor(this,
                androidx.appcompat.R.attr.colorPrimary,
                android.graphics.Color.BLUE);
        int colorOnPrimary = com.google.android.material.color.MaterialColors.getColor(this,
                android.R.attr.textColorPrimary,
                android.graphics.Color.WHITE);
        int colorBackground = com.google.android.material.color.MaterialColors.getColor(this,
                android.R.attr.colorBackground,
                android.graphics.Color.WHITE);

        options.setToolbarColor(colorPrimary);
        options.setToolbarWidgetColor(colorOnPrimary);
        options.setActiveControlsWidgetColor(colorPrimary);
        options.setRootViewBackgroundColor(colorBackground);

        Intent cropIntent = UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1000, 1000)
                .withOptions(options)
                .getIntent(this);
        
        cropImageLauncher.launch(cropIntent);
    }

    private void removePhoto() {
        selectedImageUri = null;
        imagePreview.setImageResource(R.drawable.ic_person);
    }

    private void saveContact() {
        String name = etName.getText().toString().trim();
        String rawPhone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String group = etGroup.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError(getString(R.string.name_hint));
            return;
        }

        if (!isValidPhone(rawPhone)) {
            etPhone.setError(getString(R.string.phone_hint));
            return;
        }

        String phone = normalizePhone(rawPhone);
        String emailHint = getString(R.string.email_hint);
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(emailHint);
            return;
        }

        String photoUriString = saveImageToInternalStorage(selectedImageUri);

        if (existingContact != null) {
            existingContact.name = name;
            existingContact.phone = phone;
            existingContact.email = email;
            existingContact.groupName = group;
            existingContact.photoUri = photoUriString;
            viewModel.updateContact(existingContact);
        } else {
            Contact contact = new Contact(
                    name,
                    phone,
                    email,
                    photoUriString,
                    group
            );
            viewModel.addContact(contact);
        }

        finish();
    }

    private String saveImageToInternalStorage(Uri uri) {
        if (uri == null) return null;
        
        if (uri.toString().contains(getFilesDir().getPath())) {
            return uri.toString();
        }

        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;

            String fileName = "contact_" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            OutputStream os = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            os.close();
            is.close();
            return Uri.fromFile(file).toString();
        } catch (Exception e) {
            // Error saving image
            return null;
        }
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String normalized = normalizePhone(phone);
        return normalized.matches("^\\+?[0-9]{7,15}$");
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9+]", "");
    }
}
