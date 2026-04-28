package com.z1vvs.extendedcontacts.di;

import android.content.Context;

import androidx.room.Room;

import com.z1vvs.extendedcontacts.data.local.AppDatabase;
import com.z1vvs.extendedcontacts.data.local.ContactDao;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                AppDatabase.class,
                "contacts_db"
        ).fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public ContactDao provideContactDao(AppDatabase db) {
        return db.contactDao();
    }
}