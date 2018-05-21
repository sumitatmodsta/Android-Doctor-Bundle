package com.modastadoc.doctors.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.modastadoc.doctors.App;

/**
 * Created by kunasi on 10/08/17.
 */

public class LocalDataManager {
    private static LocalDataManager instance;

    private SharedPreferences mPreference;

    /**
     * Returns an instance of {@link com.modastadoc.doctors.database.LocalDataManager}
     *
     * @return {@link com.modastadoc.doctors.database.LocalDataManager} singleton
     */
    public static LocalDataManager getInstance() {
        if(instance == null)
            instance = new LocalDataManager();

        return instance;
    }

    private LocalDataManager() {
        initialize(App.getAppContext());
    }

    /**
     * Initializes the LocalDataManager with a {@link android.content.Context}
     *
     * @param context {@link android.content.Context} to initialize the LocalDataManager with
     * @return an initialized {@link com.modastadoc.doctors.database.LocalDataManager}
     */
    private void initialize(Context context) {
        mPreference = context.getSharedPreferences("modasta_doc_app_data", Context.MODE_PRIVATE);
        //mPreference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Sets a String value to a key in {@link android.content.SharedPreferences}
     *
     * @param key   key to add
     * @param value value to apply to provided key
     */
    public void set(String key, String value) {
        mPreference.edit().putString(key, value).commit();
    }

    /**
     * Sets a int value to a key in {@link android.content.SharedPreferences}
     *
     * @param key   key to add
     * @param value value to apply to provided key
     */
    public void set(String key, int value) {
        mPreference.edit().putInt(key, value).commit();
    }

    /**
     * Sets a int value to a key in {@link android.content.SharedPreferences}
     *
     * @param key   key to add
     * @param value value to apply to provided key
     */
    public void set(String key, long value) {
        mPreference.edit().putLong(key, value).commit();
    }

    /**
     * Sets a boolean value to a key in {@link android.content.SharedPreferences}
     *
     * @param key   key to add
     * @param value value to apply to provided key
     */
    public void set(String key, boolean value) {
        mPreference.edit().putBoolean(key, value).commit();
    }

    /**
     * Gets a {@link java.lang.String} for the given {@link java.lang.String} key
     *
     * @param key {@link java.lang.String} key
     * @return the {@link java.lang.String} value for the given key, or returns the passed in default
     */
    public String get(String key) {
        return get(key, "");
    }

    /**
     * Gets a {@link java.lang.String} for the given {@link java.lang.String} key
     *
     * @param key {@link java.lang.String} key
     * @param defValue {@link java.lang.String} default string to return if key has no value
     * @return the {@link java.lang.String} value for the given key, or returns the passed in default
     */
    public String get(String key, String defValue) {
        return mPreference.getString(key, defValue);
    }

    /**
     * Gets a int for the given {@link java.lang.String} key
     *
     * @param key {@link java.lang.String} key
     * @param defValue default int to return if key has no value
     * @return the int value for the given key, or returns the passed in default
     */
    public int get(String key, int defValue) {
        return mPreference.getInt(key, defValue);
    }

    /**
     * Gets a long for the given {@link java.lang.String} key
     *
     * @param key {@link java.lang.String} key
     * @param defValue default long to return if key has no value
     * @return the long value for the given key, or returns the passed in default
     */
    public long get(String key, long defValue) {
        return mPreference.getLong(key, defValue);
    }

    /**
     * Gets a boolean for the given {@link java.lang.String} key
     *
     * @param key {@link java.lang.String} key
     * @param defValue default boolean to return if key has no value
     * @return the boolean value for the given key, or returns the passed in default
     */
    public boolean get(String key, boolean defValue) {
        return mPreference.getBoolean(key, defValue);
    }

    /**
     * Removes the key from {@link android.content.SharedPreferences}
     *
     * @param key  Key to remove
     */
    public void remove(String key) {
        mPreference.edit().remove(key).commit();
    }
}
