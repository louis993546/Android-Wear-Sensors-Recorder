package io.github.louistsaitszho.weartest

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Created by louis on 13.01.18.
 */
@Database(entities = [(RawDataEntity::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rawDataDao(): RawDataDao
}