package com.z1vvs.extendedcontacts;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.z1vvs.extendedcontacts.viewmodel.ContactViewModel;
import com.z1vvs.extendedcontacts.ui.contacts.ContactAdapter;

import dagger.hilt.android.AndroidEntryPoint;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ContactViewModel viewModel;
    private ContactAdapter adapter;
    private DrawerLayout drawerLayout;

    private final ActivityResultLauncher<String> createDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("text/x-vcard"), uri -> {
                if (uri != null) {
                    viewModel.exportContacts(uri);
                    Toast.makeText(this, R.string.exporting, Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    viewModel.importFromVcf(uri);
                    Toast.makeText(this, R.string.importing_vcf, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
            } else if (id == R.id.nav_groups) {
                startActivity(new Intent(this, GroupsActivity.class));
            } else if (id == R.id.nav_import) {
                showImportOptions();
            } else if (id == R.id.nav_export) {
                createDocumentLauncher.launch("contacts.vcf");
            }
            drawerLayout.closeDrawers();
            return true;
        });

        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });

        RecyclerView recycler = findViewById(R.id.recyclerView);
        adapter = new ContactAdapter(
                this,
                contact -> viewModel.toggleFavorite(contact)
        );
        recycler.setAdapter(adapter);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        viewModel.getContacts().observe(this, contacts -> adapter.setContacts(contacts));

        findViewById(R.id.fab).setOnClickListener(v -> startActivity(new Intent(this, AddContactActivity.class)));
    }

    private void showImportOptions() {
        String[] options = {
                getString(R.string.import_from_phone),
                getString(R.string.import_from_vcf)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.select_import_source)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkPermissionAndImport();
                    } else {
                        openDocumentLauncher.launch(new String[]{"text/x-vcard", "text/vcard"});
                    }
                })
                .show();
    }

    private void checkPermissionAndImport() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{android.Manifest.permission.READ_CONTACTS}, 100);
        } else {
            viewModel.importContacts();
            Toast.makeText(this, R.string.import_started, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.importContacts();
        }
    }
}