package cc.xfl12345.android.droidcloudsms.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper  extends SQLiteOpenHelper {

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notification_log (\n" +
            "    id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
            "    utc_time_stamp   BIGINT DEFAULT NULL,\n" +
            "    time             NCHAR(50) DEFAULT NULL,\n" +
            "    tag              NCHAR(32) DEFAULT NULL,\n" +
            "    log_level        INTEGER DEFAULT NULL,\n" +
            "    content          TEXT DEFAULT NULL\n" +
            " )");
        db.execSQL(" CREATE TABLE sms_log (\n" +
            "    id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
            "    utc_time_stamp   BIGINT DEFAULT NULL,\n" +
            "    time             NCHAR(50) DEFAULT NULL,\n" +
            "    phone_number     NCHAR(32) DEFAULT NULL,\n" +
            "    validation_code  NCHAR(20) DEFAULT NULL,\n" +
            "    content          TEXT DEFAULT NULL,\n" +
            "    sms_result_code  INTEGER DEFAULT NULL\n" +
            " )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
