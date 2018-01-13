package io.github.louistsaitszho.weartest

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

/**
 * Created by louis on 13.01.18.
 */
@Dao
interface RawDataDao {
    @Query("SELECT * FROM raw_data")
    fun getAll(): List<RawDataEntity>

    @Insert
    fun insertRawDatas(rawDatas: List<RawDataEntity>)

    @Query("DELETE FROM raw_data")
    fun nuke()
}