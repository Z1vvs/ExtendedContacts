package com.z1vvs.extendedcontacts.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    LiveData<List<Contact>> getAll();

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR groupName LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<Contact>> search(String query);

    @Query("SELECT * FROM contacts WHERE groupName = :group ORDER BY name ASC")
    LiveData<List<Contact>> getByGroup(String group);

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY name ASC")
    LiveData<List<Contact>> getFavorites();

    @Query("SELECT DISTINCT groupName FROM contacts WHERE groupName IS NOT NULL AND groupName != '' ORDER BY groupName ASC")
    LiveData<List<String>> getAllGroups();

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    LiveData<Contact> getById(int contactId);

    @Query("SELECT * FROM contacts WHERE name = :name AND phone = :phone LIMIT 1")
    Contact getByNameAndPhoneSync(String name, String phone);
}
