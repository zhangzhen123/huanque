package com.julun.huanque.core.ui.main.dynamic_square

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.HomeDynamicListInfo
import com.julun.huanque.common.bean.forms.HomePostForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.DynamicService
import com.julun.huanque.common.suger.convertListError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date 2019/7/16 19:29
 *
 *@Description 查询直播间关注列表
 *
 */
class DynamicTabViewModel : BaseViewModel() {

    private val service: DynamicService by lazy { Requests.create(DynamicService::class.java) }
    val dataList: MutableLiveData<ReactiveData<HomeDynamicListInfo>> by lazy { MutableLiveData<ReactiveData<HomeDynamicListInfo>>() }


    private var offsetHot = 0
    fun requestPostList(queryType: QueryType, postType: String?) {

        viewModelScope.launch {
            if (queryType == QueryType.REFRESH) {
                offsetHot = 0
            }

            request({
                val result =
                    service.queryHomePost(HomePostForm(offset = offsetHot, postType = postType)).dataConvert()
//                todo
//                val result = DynamicListInfo<DynamicItemBean>().apply {
//                    this.list = mutableListOf(
//                        DynamicItemBean(
//                            postId = 1,
//                            sex = "Male",
//                            age = 18,
//                            nickname = "张三",
//                            content = "我是内容 英伟达首席执行官黄仁勋：反垄断监管有利于英伟达收购Arm,我是内容 英伟达首席执行官黄仁勋：反垄断监管有利于英伟达收购Arm" +
//                                    "英伟达首席执行官黄仁勋：反垄断监管有利于英伟达收购Arm 英伟达首席执行官黄仁勋：反垄断监管有利于英伟达收购Arm"
//                        ),
//                        DynamicItemBean(
//                            postId = 2,
//                            sex = "Male",
//                            age = 18,
//                            nickname = "张三",
//                            content = "我是长图",
//                            pics = mutableListOf(
//                                "user/cover/e6b5c1e7b7ac7d252b7ea8c8ef6ebfee.jpg?w=400&h=800"
//                            )
//                        ),
//                        DynamicItemBean(
//                            postId = 22,
//                            sex = "Male",
//                            age = 18,
//                            nickname = "张三",
//                            content = "我是很的长图",
//                            pics = mutableListOf(
//                                "user/cover/e6b5c1e7b7ac7d252b7ea8c8ef6ebfee.jpg?w=400&h=1800"
//                            )
//                        ),
//                        DynamicItemBean(
//                            postId = 3,
//                            sex = "Male",
//                            age = 18,
//                            nickname = "张三",
//                            content = "我宽图",
//                            pics = mutableListOf(
//                                "user/cover/e6b5c1e7b7ac7d252b7ea8c8ef6ebfee.jpg?w=800&h=400"
//                            )
//                        ),
//                        DynamicItemBean(
//                            postId = 33,
//                            sex = "Male",
//                            age = 18,
//                            nickname = "张三",
//                            content = "我很的宽图",
//                            pics = mutableListOf(
//                                "user/cover/e6b5c1e7b7ac7d252b7ea8c8ef6ebfee.jpg?w=1800&h=400"
//                            )
//                        ),
//                        DynamicItemBean(
//                            postId = 4,
//                            sex = "Male",
//                            age = 18,
//                            city = "杭州",
//                            nickname = "张三",
//                            content = "如果不是笔者的办公电脑偶尔会出现一些蓝屏，就连我自己也早已忽视这台配备第二代酷睿i5处理器的电脑如今已经服务超过8年之久。而就在我更换了一块SSD硬盘并重新安装了操作系统之后，看到它满血复活的状态，我又放弃了换电脑的想法，再战三年又何妨。",
//                            pics = mutableListOf(
//                                "user/cover/d566bacc72323d05088b704068152704.jpg",
//                                "user/cover/e6b5c1e7b7ac7d252b7ea8c8ef6ebfee.jpg"
//                            )
//                        )
//                        , DynamicItemBean(
//                            postId = 5,
//                            sex = "Male",
//                            age = 18,
//                            city = "杭州",
//                            nickname = "张三",
//                            content = "如果不是笔者的办公电脑偶尔会出现一些蓝屏，就连我自己也早已忽视这台配备第二代酷睿i5处理器的电脑如今已经服务超过8年之久。而就在我更换了一块SSD硬盘并重新安装了操作系统之后，看到它满血复活的状态，我又放弃了换电脑的想法，再战三年又何妨。",
//                            pics = mutableListOf(
//                                "user/cover/6bd97b60ed85b72af3592aa5ed23e92f.jpg",
//                                "user/cover/d566bacc72323d05088b704068152704.jpg",
//                                "user/cover/e6b5c1e7b7ac7d252b7ea8c8ef6ebfee.jpg"
//                            )
//                        ), DynamicItemBean(
//                            postId = 6,
//                            sex = "Male",
//                            age = 18,
//                            city = "杭州",
//                            nickname = "张三",
//                            content = "我是内容 英伟达首席执行官黄仁勋：反垄断监管有利于英伟达收购Arm",
//                            pics = mutableListOf(
//                                "user/head/2019110608363175169.jpg",
//                                "user/cover/6bd97b60ed85b72af3592aa5ed23e92f.jpg",
//                                "user/cover/d566bacc72323d05088b704068152704.jpg",
//                                "user/cover/e6b5c1e7b7ac7d252b7ea8c8ef6ebfee.jpg"
//                            )
//                        )
//
//                    )
//                }
                result.hasMore = false
                //
                offsetHot += result.postList.size
                result.isPull = queryType != QueryType.LOAD_MORE
                dataList.value = result.convertRtData()
            }, error = {
                dataList.value = it.convertListError(queryType = queryType)
            }, needLoadState = queryType == QueryType.INIT)
        }
    }

}