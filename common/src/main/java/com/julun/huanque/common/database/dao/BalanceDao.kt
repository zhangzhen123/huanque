package com.julun.huanque.common.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.julun.huanque.common.database.table.Balance
import io.reactivex.rxjava3.core.Flowable

/**
 * Created by dong on 2018/8/8.
 */
@Dao
interface BalanceDao {
    @Query("SELECT * FROM Balance WHERE userId = :userId LIMIT 1")
    fun getBalance(userId: Long): LiveData<Balance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bean: Balance): Long

    @Query("DELETE FROM Balance")
    fun deleteBalance()
}