package com.julun.huanque.common.database.table

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 *@创建者   dong
 *@创建时间 2020/7/28 17:19
 *@描述 保存登录状态
 */
@Entity
data class LoginStatus(
    @NonNull
    @PrimaryKey
    var userId: Long = 0,
    var login: Boolean = false
) : Serializable {
    @Ignore
    constructor() : this(0)
}