package com.julun.huanque.common.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.database.dao.BalanceDao
import com.julun.huanque.common.database.dao.ChatUserDao
import com.julun.huanque.common.database.dao.LoginStatusDao
import com.julun.huanque.common.database.table.Balance
import com.julun.huanque.common.database.table.LoginStatus
import com.julun.huanque.common.init.CommonInit

/**
 *@创建者   dong
 *@创建时间 2020/7/8 10:39
 *@描述 欢鹊数据库
 */
@Database(entities = [ChatUser::class, Balance::class, LoginStatus::class], version = 2, exportSchema = false)
abstract class HuanQueDatabase : RoomDatabase() {
    abstract fun chatUserDao(): ChatUserDao
    abstract fun balanceDao(): BalanceDao
    abstract fun loginStatusDao(): LoginStatusDao

    companion object {

        @Volatile
        private var INSTANCE: HuanQueDatabase? = null

        fun getInstance(): HuanQueDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase().also { INSTANCE = it }
            }

        private fun buildDatabase() =
            Room.databaseBuilder(CommonInit.getInstance().getApp(), HuanQueDatabase::class.java, "HuanQue.db")
                .addMigrations(MIGRATION_1_2)
                .build()


        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //抛弃之前消息中心所有数据
                database.execSQL("ALTER TABLE ChatUser " + " ADD COLUMN userType TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}