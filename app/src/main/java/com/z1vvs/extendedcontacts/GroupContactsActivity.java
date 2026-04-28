package com.z1vvs.extendedcontacts;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.z1vvs.extendedcontacts.ui.contacts.ContactAdapter;
import com.z1vvs.extendedcontacts.viewmodel.ContactViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GroupContactsActivity extends AppCompatActivity {

    private ContactViewModel viewModel;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_contacts);

        String groupName = getIntent().getStringExtra("group_name");

        Toolbar toolbar = findViewById(R.id.toolbarGroupContacts);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(groupName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewGroupContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ContactAdapter(this,
                contact -> viewModel.toggleFavorite(contact));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        viewModel.getContactsByGroup(groupName).observe(this, contacts -> adapter.setContacts(contacts));
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