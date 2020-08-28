package com.julun.huanque.common.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.julun.huanque.common.database.table.LoginStatus

/**
 *@创建者   dong
 *@创建时间 2020/7/28 17:40
 *@描述 登录状态 dao
 */
@Dao
interface LoginStatusDao {
    @Query("SELECT * FROM LoginStatus where userId = :userId  LIMIT 1")
    fun getLoginStatus(userId: Long): LiveData<LoginStatus>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bean: LoginStatus): Long

    @Query("DELETE FROM LoginStatus")
    fun deleteLoginStatus()
}