package com.z1vvs.extendedcontacts;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.z1vvs.extendedcontacts.ui.groups.GroupAdapter;
import com.z1vvs.extendedcontacts.viewmodel.ContactViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GroupsActivity extends AppCompatActivity {

    private GroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        Toolbar toolbar = findViewById(R.id.toolbarGroups);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GroupAdapter(groupName -> {
            Intent intent = new Intent(this, GroupContactsActivity.class);
            intent.putExtra("group_name", groupName);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        ContactViewModel viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        viewModel.getGroups().observe(this, groups -> adapter.setGroups(groups));
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