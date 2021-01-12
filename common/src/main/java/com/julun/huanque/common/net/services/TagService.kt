package com.julun.huanque.common.net.services

import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidForm
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
import com.julun.huanque.common.database.table.Session
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/30 16:27
 *
 *@Description: TagService 所有的标签相关的接口
 *
 */
interface TagService {


    /**
     * 管理标签数据拉取
     */
    @POST("social/user/tag/manageList")
    suspend fun manageList(@Body body: EmptyForm = EmptyForm()): Root<ManagerListData>

    /**
     * 管理标签标记喜欢
     */
    @POST("social/user/tag/like")
    suspend fun tagLike(@Body body: TagForm): Root<ManagerParentTagBean>

    /**
     * 管理标签标记取消喜欢
     */
    @POST("social/user/tag/cancelLike")
    suspend fun tagCancelLike(@Body body: TagForm): Root<ManagerParentTagBean>

    /**
     * 保存标签顺序
     */
    @POST("social/user/tag/saveManage")
    suspend fun saveTagManage(@Body body: TagListForm): Root<VoidResult>

    /**
     * 标签详情页图片
     */
    @POST("social/user/tag/likeDetail")
    suspend fun tagDetail(@Body body: TagDetailForm): Root<TagDetailBean>
    /**
     * 标签认证图片
     */
    @POST("social/user/tag/authDetail")
    suspend fun authDetail(@Body body: TagForm): Root<AuthTagPicInfo>

    /**
     * 标签认证图片
     */
    @POST("social/user/tag/apply")
    suspend fun tagApply(@Body body: TagApplyForm): Root<AuthPic>

    /**
     * 删除标签认证图片
     */
    @POST("social/user/tag/deleteAuthPic")
    suspend fun deleteAuthPic(@Body body: LogForm): Root<VoidResult>

    /**
     * 标签用户图片列表
     */
    @POST("social/user/tag/authPic")
    suspend fun tagAuthPic(@Body body: TagUserForm): Root<TagUserPicListBean>

    /**
     * 标签用户图片列表
     */
    @POST("social/user/tag/praise")
    suspend fun tagPraise(@Body body: TagUserForm): Root<VoidResult>

    /**
     * 标签用户图片列表
     */
    @POST("social/user/tag/cancelPraise")
    suspend fun tagCancelPraise(@Body body: TagUserForm): Root<VoidResult>

    /**
     * 查询筛选配置
     */
    @POST("social/friend/home/searchConfig")
    suspend fun searchConfig(@Body body: VoidForm = VoidForm()): Root<FilterTagBean>

    /**
     * 保持筛选配置
     */
    @POST("social/friend/home/saveSearchConfig")
    suspend fun saveSearchConfig(@Body body: SaveSearchConfigForm): Root<VoidResult>

}