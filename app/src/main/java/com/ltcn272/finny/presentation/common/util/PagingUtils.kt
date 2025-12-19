package com.ltcn272.finny.presentation.common.util

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

fun <T : Any> Flow<PagingData<T>>.asSnapshotList(): Flow<List<T>> {
    return this.transform { pagingData ->
        val list = mutableListOf<T>()
        pagingData.map { item ->
            list.add(item)
        }
        emit(list)
    }
}