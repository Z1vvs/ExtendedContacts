package com.z1vvs.extendedcontacts.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.z1vvs.extendedcontacts.data.local.Contact;
import com.z1vvs.extendedcontacts.data.repository.ContactRepository;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ContactViewModel extends AndroidViewModel {

    private final ContactRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<Contact>> contacts;

    @Inject
    public ContactViewModel(@NonNull Application application, ContactRepository repository) {
        super(application);
        this.repository = repository;
        this.contacts = Transformations.switchMap(
                searchQuery,
                query -> {
                    if (query == null || query.isEmpty()) {
                        return repository.getAllContacts();
                    } else {
                        return repository.search(query);
                    }
                }
        );
    }

    public LiveData<List<Contact>> getContacts() {
        return contacts;
    }

    public LiveData<List<Contact>> getFavoriteContacts() {
        return repository.getFavorites();
    }

    public LiveData<List<String>> getGroups() {
        return repository.getAllGroups();
    }

    public LiveData<List<Contact>> getContactsByGroup(String group) {
        return repository.getContactsByGroup(group);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void addContact(Contact contact) {
        repository.insert(contact);
    }

    public void updateContact(Contact contact) {
        repository.update(contact);
    }

    public LiveData<Contact> getContactById(int id) {
        return repository.getContactById(id);
    }

    public void deleteContact(Contact contact) {
        repository.delete(contact);
    }

    public void toggleFavorite(Contact contact) {
        contact.isFavorite = !contact.isFavorite;
        repository.update(contact);
    }

    public void importContacts() {
        repository.importSystemContacts(getApplication());
    }

    public void importFromVcf(android.net.Uri uri) {
        repository.importFromVcf(getApplication(), uri);
    }

    public void exportContacts(android.net.Uri uri) {
        List<Contact> currentContacts = contacts.getValue();
        if (currentContacts != null) {
            repository.exportContacts(getApplication(), uri, currentContacts);
        }
    }
}