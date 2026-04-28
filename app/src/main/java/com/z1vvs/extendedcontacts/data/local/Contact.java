package com.z1vvs.extendedcontacts.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "contacts")
public class Contact implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String phone;
    public String email;
    public String photoUri;
    public String groupName;
    public boolean isFavorite;

    public Contact(String name, String phone, String email, String photoUri, String groupName) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.photoUri = photoUri;
        this.groupName = groupName;
    }
}