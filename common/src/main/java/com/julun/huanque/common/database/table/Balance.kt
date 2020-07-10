package com.julun.huanque.common.database.table

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by dong on 2018/8/8.
 */
@Entity
data class Balance(
        @PrimaryKey
        var userId: Long = 0,
        var balance: Long = 0) : Serializable {
    @Ignore
    constructor() : this(0)
}