package com.z1vvs.extendedcontacts;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.z1vvs.extendedcontacts.ui.contacts.ContactAdapter;
import com.z1vvs.extendedcontacts.viewmodel.ContactViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import androidx.appcompat.widget.Toolbar;

@AndroidEntryPoint
public class FavoritesActivity extends AppCompatActivity {

    private ContactViewModel viewModel;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        Toolbar toolbar = findViewById(R.id.toolbarFavorites);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ContactAdapter(this,
                contact -> viewModel.toggleFavorite(contact)
        );
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        viewModel.getFavoriteContacts().observe(this, contacts -> adapter.setContacts(contacts));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}