package edu.put.inf151825

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.util.Log
import androidx.core.database.getStringOrNull

class MainDatabase(
    var id: Int,
    var title: String?,
    var org_title: String?,
    var year_pub: Int,
    var rank_pos: Int,
    var pic: String?,
    var photo: String?,
    var error: String?,
    exp: Int
) {
    var expansion: Int = exp
}

enum class MainDatabaseOrders {
    _ID, TITLE, YEAR_PUB, RANK_POS
}

class MainDatabaseHelper(
    context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int
) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "games.db"
        val TABLE_RECORDS = "games"
        val COLUMN_ID = "_id"
        val COLUMN_TITLE = "title"
        val COLUMN_ORIGINAL_TITLE = "original_title"
        val COLUMN_YEAR_PUB = "year_pub"
        val COLUMN_RANK_POS = "rank_pos"
        val COLUMN_THUMBNAIL = "pic"
        val COLUMN_IMAGE = "photo"
        val COLUMN_MESSAGE = "error"
        val COLUMN_EXPANSION = "expansion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_GAMES_TABLE =
            ("CREATE TABLE $TABLE_RECORDS($COLUMN_ID INTEGER PRIMARY KEY,$COLUMN_TITLE TEXT,$COLUMN_ORIGINAL_TITLE TEXT,$COLUMN_YEAR_PUB INTEGER,$COLUMN_RANK_POS INTEGER,$COLUMN_THUMBNAIL TEXT, $COLUMN_IMAGE TEXT, $COLUMN_MESSAGE TEXT, $COLUMN_EXPANSION INTEGER)")
        db.execSQL(CREATE_GAMES_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
        onCreate(db)
    }

    fun clear() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
        val CREATE_GAMES_TABLE =
            ("CREATE TABLE $TABLE_RECORDS($COLUMN_ID INTEGER PRIMARY KEY,$COLUMN_TITLE TEXT,$COLUMN_ORIGINAL_TITLE TEXT,$COLUMN_YEAR_PUB INTEGER,$COLUMN_RANK_POS INTEGER,$COLUMN_THUMBNAIL TEXT, $COLUMN_IMAGE TEXT, $COLUMN_MESSAGE TEXT, $COLUMN_EXPANSION INTEGER)")
        db.execSQL(CREATE_GAMES_TABLE)
    }

    fun addRecord(record: MainDatabase) {
        val values = ContentValues()
        values.put(COLUMN_ID, record.id)
        values.put(COLUMN_TITLE, record.title)
        values.put(COLUMN_ORIGINAL_TITLE, record.org_title)
        values.put(COLUMN_YEAR_PUB, record.year_pub)
        values.put(COLUMN_RANK_POS, record.rank_pos)
        values.put(COLUMN_THUMBNAIL, record.pic)
        values.put(COLUMN_IMAGE, record.photo)
        values.put(COLUMN_MESSAGE, record.error)
        values.put(COLUMN_EXPANSION, record.expansion)
        val db = this.writableDatabase
        try {
            db.insertOrThrow(TABLE_RECORDS, null, values)
        } catch (_: java.lang.Exception) {
            Log.d("Exception", "Caught e in DB insert. ${record.title}")
        }
        db.close()
    }

    fun findRecord(title: String?): MainDatabase? {
        val query = "SELECT * FROM $TABLE_RECORDS WHERE $COLUMN_TITLE LIKE \"$title\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var rec: MainDatabase? = null

        if (cursor.moveToFirst()) {
            val id = Integer.parseInt(cursor.getString(0))
            val title = cursor.getString(1)
            val org_title = cursor.getString(2)
            val year_pub = cursor.getInt(3)
            val rank_pos = cursor.getInt(4)
            val thumb = cursor.getStringOrNull(5)
            val photos = cursor.getStringOrNull(6)
            val errors = cursor.getStringOrNull(7)
            val exp = cursor.getInt(8)

            rec = MainDatabase(id, title, org_title, year_pub, rank_pos, thumb, photos, errors, exp)
            cursor.close()
        }

        db.close()
        return rec
    }

    fun findRecord(id: Int): MainDatabase? {
        val query = "SELECT * FROM $TABLE_RECORDS WHERE $COLUMN_ID = $id"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var rec: MainDatabase? = null

        if (cursor.moveToFirst()) {
            val id = Integer.parseInt(cursor.getString(0))
            val title = cursor.getString(1)
            val org_title = cursor.getString(2)
            val year_pub = cursor.getInt(3)
            val rank_pos = cursor.getInt(4)
            val thumb = cursor.getStringOrNull(5)
            val photos = cursor.getStringOrNull(6)
            val errors = cursor.getStringOrNull(7)
            val exp = cursor.getInt(8)

            rec = MainDatabase(id, title, org_title, year_pub, rank_pos, thumb, photos, errors, exp)
            cursor.close()
        }

        db.close()
        return rec
    }

    fun countGames(): Int {
        var ans = 0
        val query = "SELECT COUNT(*) FROM $TABLE_RECORDS WHERE $COLUMN_EXPANSION = 0"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            ans = Integer.parseInt(cursor.getString(0))
            cursor.close()
        }
        db.close()
        return ans
    }

    fun countAddons(): Int {
        var ans = 0
        val query = "SELECT COUNT(*) FROM $TABLE_RECORDS WHERE $COLUMN_EXPANSION = 1"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            ans = Integer.parseInt(cursor.getString(0))
            cursor.close()
        }
        db.close()
        return ans
    }

    fun returnError(): String {
        var ans = ""
        val query = "SELECT $COLUMN_MESSAGE FROM $TABLE_RECORDS WHERE $COLUMN_EXPANSION !=null"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            ans = cursor.getString(0)
            cursor.close()
        }
        db.close()
        return ans
    }

    fun getValues(
        addOns: Int,
        ord: MainDatabaseOrders = MainDatabaseOrders._ID,
        desc: Boolean = false
    ): List<MainDatabase> {
        val mList: MutableList<MainDatabase> = ArrayList()
        val d = if (desc) "DESC" else ""
        val o = ord.toString()
        val query = "SELECT * FROM $TABLE_RECORDS WHERE $COLUMN_EXPANSION = $addOns ORDER BY $o $d"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.count == 0) {
            return mList.toList()
        }
        cursor.moveToFirst()
        cursor.moveToFirst()
        do {
            var rec: MainDatabase? = null
            val id = Integer.parseInt(cursor.getString(0))
            val title = cursor.getString(1)
            val org_title = cursor.getString(2)
            val year_pub = cursor.getInt(3)
            val rank_pos = cursor.getInt(4)
            val thumb = cursor.getStringOrNull(5)
            val photos = cursor.getStringOrNull(6)
            val errors = cursor.getStringOrNull(7)
            rec = MainDatabase(
                id,
                title,
                org_title,
                year_pub,
                rank_pos,
                thumb,
                photos,
                errors,
                addOns
            )
            mList.add(rec)
        } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return mList
    }
}

class PhotoDatabase(
    var id: Int,
    var photo: String?
) {}

enum class PhotoDatabaseOrders {
    ID, PHOTO
}

class PhotoDatabaseHelper(
    context: Context,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "images.db"
        const val TABLE_RECORDS = "images"
        val COLUMN_ID = "id"
        val COLUMN_IMAGE = "photo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_IMAGES_TABLE = ("CREATE TABLE " +
                TABLE_RECORDS + "(" +
                COLUMN_ID + " INTEGER," +
                COLUMN_IMAGE + " TEXT " +
                ")")
        db.execSQL(CREATE_IMAGES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${MainDatabaseHelper.TABLE_RECORDS}")
        onCreate(db)
    }

    fun clear() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORDS")
        val CREATE_IMAGES_TABLE = ("CREATE TABLE " +
                TABLE_RECORDS + "(" +
                COLUMN_ID + " INTEGER," +
                COLUMN_IMAGE + " TEXT " +
                ")")
        db.execSQL(CREATE_IMAGES_TABLE)
    }

    fun addRecord(record: PhotoDatabase) {
        val values = ContentValues()
        values.put(COLUMN_ID, record.id)
        values.put(COLUMN_IMAGE, record.photo)
        val db = this.writableDatabase
        db.insert(TABLE_RECORDS, null, values)
        db.close()
    }

    fun deletePhoto(id:Int, photo: String){
        val db = this.writableDatabase
        db.delete(TABLE_RECORDS, "$COLUMN_IMAGE = '$photo'", null)
        db.close()
    }
    fun countPhoto(id:Int):Int{
        var res=0
        val query =
            "SELECT COUNT(*) FROM ${PhotoDatabaseHelper.TABLE_RECORDS} WHERE ${PhotoDatabaseHelper.COLUMN_ID} = $id"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            res = Integer.parseInt(cursor.getString(0))
            cursor.close()
        }
        return res
    }
    fun getUri(id: Int, i: Int): Uri {
        val mList: MutableList<PhotoDatabase> = ArrayList()
        val query =
            "SELECT ${PhotoDatabaseHelper.COLUMN_IMAGE} FROM ${PhotoDatabaseHelper.TABLE_RECORDS} WHERE ${PhotoDatabaseHelper.COLUMN_ID} = $id"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.count == 0)
            cursor.moveToFirst()
        cursor.moveToFirst()
        do {
            var rec: PhotoDatabase? = null
            val photos = cursor.getStringOrNull(0)
            rec = PhotoDatabase(id, photos)
            mList.add(rec)
        } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return Uri.parse(mList[i].photo)


    }
    fun getValues(
        addOns: Int,
        ord: PhotoDatabaseOrders = PhotoDatabaseOrders.ID,
        desc: Boolean = false
    ): List<PhotoDatabase> {
        val mList: MutableList<PhotoDatabase> = ArrayList()
        val query =
            "SELECT ${PhotoDatabaseHelper.COLUMN_IMAGE} FROM ${PhotoDatabaseHelper.TABLE_RECORDS} WHERE ${PhotoDatabaseHelper.COLUMN_ID} = $addOns"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.count == 0) {
            return mList.toList()
        }
        cursor.moveToFirst()
        cursor.moveToFirst()
        do {
            var rec: PhotoDatabase? = null
            val photos = cursor.getStringOrNull(0)
            rec = PhotoDatabase(0, photos)
            mList.add(rec)
        } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return mList
    }
}
