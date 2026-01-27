package com.emergency.alert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EmergencyAlert.db";
    private static final int DATABASE_VERSION = 1;

    // User Table
    private static final String TABLE_USER = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_BG_COLOR = "bg_color";
    private static final String COL_FONT_SIZE = "font_size";

    // Emergency Contacts Table
    private static final String TABLE_CONTACTS = "emergency_contacts";
    private static final String COL_CONTACT_ID = "id";
    private static final String COL_CONTACT_NAME = "name";
    private static final String COL_CONTACT_PHONE = "phone";
    private static final String COL_CONTACT_RELATION = "relation";

    // Emergency Events Table
    private static final String TABLE_EVENTS = "emergency_events";
    private static final String COL_EVENT_ID = "id";
    private static final String COL_EVENT_TYPE = "event_type";
    private static final String COL_EVENT_DATE = "event_date";
    private static final String COL_EVENT_LOCATION = "location";
    private static final String COL_EVENT_NOTES = "notes";

    // Safety Tips Table
    private static final String TABLE_TIPS = "safety_tips";
    private static final String COL_TIP_ID = "id";
    private static final String COL_TIP_CATEGORY = "category";
    private static final String COL_TIP_TITLE = "title";
    private static final String COL_TIP_CONTENT = "content";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create User Table
        String createUserTable = "CREATE TABLE " + TABLE_USER + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_BG_COLOR + " TEXT DEFAULT '#FFFFFF', " +
                COL_FONT_SIZE + " INTEGER DEFAULT 16)";
        db.execSQL(createUserTable);

        // Create Emergency Contacts Table
        String createContactsTable = "CREATE TABLE " + TABLE_CONTACTS + " (" +
                COL_CONTACT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CONTACT_NAME + " TEXT, " +
                COL_CONTACT_PHONE + " TEXT, " +
                COL_CONTACT_RELATION + " TEXT)";
        db.execSQL(createContactsTable);

        // Create Emergency Events Table
        String createEventsTable = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EVENT_TYPE + " TEXT, " +
                COL_EVENT_DATE + " TEXT, " +
                COL_EVENT_LOCATION + " TEXT, " +
                COL_EVENT_NOTES + " TEXT)";
        db.execSQL(createEventsTable);

        // Create Safety Tips Table
        String createTipsTable = "CREATE TABLE " + TABLE_TIPS + " (" +
                COL_TIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TIP_CATEGORY + " TEXT, " +
                COL_TIP_TITLE + " TEXT, " +
                COL_TIP_CONTENT + " TEXT)";
        db.execSQL(createTipsTable);

        // Insert default safety tips
        insertDefaultSafetyTips(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIPS);
        onCreate(db);
    }

    // User Methods
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USER, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null,
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean updateUserTheme(String username, String bgColor, int fontSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BG_COLOR, bgColor);
        values.put(COL_FONT_SIZE, fontSize);

        int result = db.update(TABLE_USER, values, COL_USERNAME + "=?", new String[]{username});
        return result > 0;
    }

    public UserProfile getUserProfile(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null, COL_USERNAME + "=?",
                new String[]{username}, null, null, null);

        UserProfile profile = null;
        if (cursor.moveToFirst()) {
            profile = new UserProfile();
            profile.username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME));
            profile.bgColor = cursor.getString(cursor.getColumnIndexOrThrow(COL_BG_COLOR));
            profile.fontSize = cursor.getInt(cursor.getColumnIndexOrThrow(COL_FONT_SIZE));
        }
        cursor.close();
        return profile;
    }

    // Emergency Contacts Methods
    public long addEmergencyContact(String name, String phone, String relation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CONTACT_NAME, name);
        values.put(COL_CONTACT_PHONE, phone);
        values.put(COL_CONTACT_RELATION, relation);

        return db.insert(TABLE_CONTACTS, null, values);
    }

    public List<EmergencyContact> getAllEmergencyContacts() {
        List<EmergencyContact> contacts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                EmergencyContact contact = new EmergencyContact();
                contact.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CONTACT_ID));
                contact.name = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_NAME));
                contact.phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_PHONE));
                contact.relation = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTACT_RELATION));
                contacts.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contacts;
    }

    public boolean deleteEmergencyContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CONTACTS, COL_CONTACT_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    // Emergency Events Methods
    public long addEmergencyEvent(String eventType, String location, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        values.put(COL_EVENT_TYPE, eventType);
        values.put(COL_EVENT_DATE, currentDate);
        values.put(COL_EVENT_LOCATION, location);
        values.put(COL_EVENT_NOTES, notes);

        return db.insert(TABLE_EVENTS, null, values);
    }

    public List<EmergencyEvent> getAllEmergencyEvents() {
        List<EmergencyEvent> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, null, null, null, null, null, COL_EVENT_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                EmergencyEvent event = new EmergencyEvent();
                event.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_ID));
                event.eventType = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TYPE));
                event.eventDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_DATE));
                event.location = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_LOCATION));
                event.notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_NOTES));
                events.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    // Safety Tips Methods
    private void insertDefaultSafetyTips(SQLiteDatabase db) {
        insertSafetyTip(db, "Medical", "CPR Steps",
                "1. Check responsiveness\n2. Call emergency services\n3. 30 chest compressions\n4. 2 rescue breaths\n5. Repeat until help arrives");

        insertSafetyTip(db, "Medical", "Choking Aid",
                "1. Encourage coughing\n2. Give 5 back blows\n3. Give 5 abdominal thrusts\n4. Repeat until object is expelled");

        insertSafetyTip(db, "Safety", "Self Defense Tips",
                "1. Be aware of surroundings\n2. Trust your instincts\n3. Make noise and attract attention\n4. Target vulnerable areas (eyes, nose, throat)\n5. Run to safety when possible");

        insertSafetyTip(db, "Disaster", "Earthquake Safety",
                "1. DROP, COVER, and HOLD ON\n2. Stay away from windows\n3. If outdoors, move to open area\n4. After shaking stops, evacuate if safe\n5. Check for injuries");

        insertSafetyTip(db, "Disaster", "Flood Safety",
                "1. Move to higher ground immediately\n2. Avoid walking/driving through water\n3. Turn off utilities if told to do so\n4. Listen to emergency broadcasts\n5. Don't return until authorities say it's safe");
    }

    private void insertSafetyTip(SQLiteDatabase db, String category, String title, String content) {
        ContentValues values = new ContentValues();
        values.put(COL_TIP_CATEGORY, category);
        values.put(COL_TIP_TITLE, title);
        values.put(COL_TIP_CONTENT, content);
        db.insert(TABLE_TIPS, null, values);
    }

    public List<SafetyTip> getAllSafetyTips() {
        List<SafetyTip> tips = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TIPS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                SafetyTip tip = new SafetyTip();
                tip.id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TIP_ID));
                tip.category = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIP_CATEGORY));
                tip.title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIP_TITLE));
                tip.content = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIP_CONTENT));
                tips.add(tip);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tips;
    }

    // Data Models
    public static class UserProfile {
        public String username;
        public String bgColor;
        public int fontSize;
    }

    public static class EmergencyContact {
        public int id;
        public String name;
        public String phone;
        public String relation;
    }

    public static class EmergencyEvent {
        public int id;
        public String eventType;
        public String eventDate;
        public String location;
        public String notes;
    }

    public static class SafetyTip {
        public int id;
        public String category;
        public String title;
        public String content;
    }
}
