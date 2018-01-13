package io.github.louistsaitszho.weartest

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.hardware.SensorEvent
import java.util.*

/**
 * Created by louis on 13.01.18.
 */
@Entity(tableName = "raw_data")
data class RawDataEntity(
        @PrimaryKey(autoGenerate = true) val id: Long,
        @ColumnInfo(name = "timestamp") val timestamp: Long,
        @ColumnInfo(name = "accuracy") val accuracy: Int,
        @ColumnInfo(name = "sensor") val sensor: String,
        @ColumnInfo(name = "values") val values: String
) {
    constructor(event: SensorEvent) : this(0, event.timestamp, event.accuracy, event.sensor.toString(), Arrays.toString(event.values))
}