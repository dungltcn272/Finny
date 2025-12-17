package com.ltcn272.finny.data.local

import androidx.room.TypeConverter
import com.ltcn272.finny.data.local.entities.SyncState

class SyncStateConverter {
    @TypeConverter
    fun toSyncState(value: String?): SyncState? {
        return value?.let { SyncState.valueOf(it) }
    }

    @TypeConverter
    fun fromSyncState(syncState: SyncState?): String? {
        return syncState?.name
    }
}