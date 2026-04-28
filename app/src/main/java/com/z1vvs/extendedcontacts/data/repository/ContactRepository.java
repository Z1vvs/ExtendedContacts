package com.z1vvs.extendedcontacts.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import androidx.lifecycle.LiveData;
import com.z1vvs.extendedcontacts.data.local.Contact;
import com.z1vvs.extendedcontacts.data.local.ContactDao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import javax.inject.Inject;

public class ContactRepository {

    private final ContactDao dao;

    @Inject
    public ContactRepository(ContactDao dao) {
        this.dao = dao;
    }

    public LiveData<List<Contact>> getAllContacts() {
        return dao.getAll();
    }

    public void insert(Contact contact) {
        new Thread(() -> dao.insert(contact)).start();
    }

    public void update(Contact contact) {
        new Thread(() -> dao.update(contact)).start();
    }

    public void delete(Contact contact) {
        new Thread(() -> dao.delete(contact)).start();
    }

    public LiveData<List<Contact>> search(String query) {
        return dao.search(query);
    }

    public LiveData<List<Contact>> getFavorites() {
        return dao.getFavorites();
    }

    public LiveData<List<String>> getAllGroups() {
        return dao.getAllGroups();
    }

    public LiveData<Contact> getContactById(int id) {
        return dao.getById(id);
    }

    public LiveData<List<Contact>> getContactsByGroup(String group) {
        return dao.getByGroup(group);
    }

    public void importSystemContacts(Context context) {
        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try {
                ContentResolver resolver = appContext.getContentResolver();
                try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, null, null, null)) {

                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                            if (nameIndex != -1 && phoneIndex != -1) {
                                String name = cursor.getString(nameIndex);
                                String phone = normalizePhone(cursor.getString(phoneIndex));

                                if (name != null && !name.isEmpty() && !phone.isEmpty()) {
                                    // Prevent duplicates
                                    if (dao.getByNameAndPhoneSync(name, phone) == null) {
                                        dao.insert(new Contact(name, phone, "", null, "Imported"));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Error importing contacts
            }
        }).start();
    }

    public void exportContacts(Context context, Uri uri, List<Contact> contacts) {
        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try (OutputStream os = appContext.getContentResolver().openOutputStream(uri);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {

                if (os == null) return;
                
                for (Contact contact : contacts) {
                    writer.write("BEGIN:VCARD\n");
                    writer.write("VERSION:3.0\n");
                    writer.write("FN:" + (contact.name != null ? contact.name : "") + "\n");
                    writer.write("TEL;TYPE=CELL:" + (contact.phone != null ? contact.phone : "") + "\n");
                    if (contact.email != null && !contact.email.isEmpty()) {
                        writer.write("EMAIL:" + contact.email + "\n");
                    }
                    writer.write("END:VCARD\n");
                }
                writer.flush();
            } catch (IOException e) {
                // Error exporting contacts
            }
        }).start();
    }

    public void importFromVcf(Context context, Uri uri) {
        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try (java.io.InputStream is = appContext.getContentResolver().openInputStream(uri);
                 java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
                
                if (is == null) return;

                String line;
                String name = null;
                String phone = null;
                String email = null;
                
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("FN:")) {
                        name = line.substring(3).trim();
                    } else if (line.startsWith("TEL")) {
                        int colonIndex = line.indexOf(':');
                        if (colonIndex != -1) {
                            phone = normalizePhone(line.substring(colonIndex + 1).trim());
                        }
                    } else if (line.startsWith("EMAIL:")) {
                        email = line.substring(6).trim();
                    } else if (line.startsWith("END:VCARD")) {
                        if (name != null && !name.isEmpty() && phone != null && !phone.isEmpty()) {
                            // Prevent duplicates
                            if (dao.getByNameAndPhoneSync(name, phone) == null) {
                                dao.insert(new Contact(name, phone, email != null ? email : "", null, "VCF Import"));
                            }
                        }
                        name = null;
                        phone = null;
                        email = null;
                    }
                }
            } catch (IOException e) {
                // Error importing from VCF
            }
        }).start();
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9+]", "");
    }
}
