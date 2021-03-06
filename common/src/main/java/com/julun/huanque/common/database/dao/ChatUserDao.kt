package com.julun.huanque.common.database.dao

import androidx.room.*
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.utils.SessionUtils
import io.reactivex.rxjava3.core.Flowable

/**
 *
 *@author zhangzhen
 *@data 2019/3/27
 *
 **/
@Dao
interface ChatUserDao {
//    @Query("SELECT * FROM ChatUser where status = 'Normal' and mineUserId = :mineId order by pinyin")
//    fun queryAllFriends(mineId: Long = SessionUtils.getUserId()): Flowable<List<ChatUser>>

    //    @Query("SELECT * FROM ChatUser where status = 'Normal' and mineUserId = :mineId order by pinyin")
    @Query("SELECT * FROM ChatUser where  mineUserId = :mineId ")
    fun queryAllFriendsByList(mineId: Long = SessionUtils.getUserId()): List<ChatUser>

    /**
     * 查询单个聊天用户
     */
    @Query("SELECT * FROM ChatUser where userId = :uId and mineUserId = :mineId LIMIT 1")
    fun querySingleUser(uId: Long, mineId: Long = SessionUtils.getUserId()): ChatUser?

    /**
     * 查询多个用户
     */
    @Query("SELECT * FROM ChatUser where userId in (:idList) and mineUserId = :mineId ")
    fun queryUsers(idList: MutableList<Long>, mineId: Long = SessionUtils.getUserId()): List<ChatUser>?

    /**
     * 存入数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(beans: List<ChatUser>)

    /**
     * 存入单个数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(beans: ChatUser)

    /**
     * 清空表
     */
    @Query("DELETE FROM ChatUser")
    fun clearChatUser()

    /**
     * 删除单个chatuser
     */
    @Query("DELETE FROM ChatUser where userId = :uId")
    fun removeSingleChatUser(uId: Long)
}