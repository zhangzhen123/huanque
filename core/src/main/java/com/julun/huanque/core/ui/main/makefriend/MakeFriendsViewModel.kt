package com.julun.huanque.core.ui.main.makefriend

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.CoverRemind
import com.julun.huanque.common.bean.beans.HeadNavigateInfo
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.forms.RecomListForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.suger.*


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:07
 *
 *@Description: MakeFriendsViewModel 交友逻辑
 *
 */
class MakeFriendsViewModel : BaseViewModel() {

    companion object {
        //添加tag引导的位置
        const val GUIDE_INDEX_01 = 14
        //添加资料完善引导的位置
        const val GUIDE_INDEX_02 = 29
    }

    private val service: HomeService by lazy {
        Requests.create(HomeService::class.java)
    }
    private var offset = 0
    private var coverRemind: CoverRemind? = null
    private var tagRemind: Boolean = false
    private var needGuide1 = true
    private var needGuide2 = true

    //记录全部的列表
    private var totalList = mutableListOf<HomeItemBean>()

    val stateList: LiveData<ReactiveData<RootListData<HomeItemBean>>> = queryState.switchMap { type ->
        liveData {
            if (type != QueryType.LOAD_MORE) {
                totalList.clear()
                offset = 0
            }

            request({
                val homeListData = service.homeRecom(RecomListForm(offset)).dataConvert()
                val list = arrayListOf<HomeItemBean>()

                if (offset == 0) {
                    val headNavigateInfo = HeadNavigateInfo().apply {
                        moduleList = homeListData.moduleList
                    }
                    list.add(HomeItemBean(HomeItemBean.HEADER, headNavigateInfo))

                    homeListData.list.forEach {
//                        //todo test 图片测试
//                        it.headPic="user/head/7c927139-bd25-48ab-837a-603a1ff3e78e.jpg"
//                        val test1=it.copy()
//                        test1.coverPicList= arrayListOf()
//                        test1.introduceVoice=""
//                        list.add(HomeItemBean(HomeItemBean.NORMAL, test1))
//                        val test2=it.copy()
//                        test2.coverPicList= arrayListOf()
//                        list.add(HomeItemBean(HomeItemBean.NORMAL, test2))
//                        val listPic= arrayListOf<String>()
//                        repeat(4){
//                            listPic.add("user/head/20000008/bf058b70-22e7-41db-a72b-a42701a3ddf8.jpg")
//                        }
//                        it.coverPicList=listPic
//                        //todo 上面都是测试代码
                        list.add(HomeItemBean(HomeItemBean.NORMAL, it))

                    }
                    //记录第一次返回的引导参数
                    tagRemind = homeListData.tagRemind
                    coverRemind = homeListData.coverRemind

                } else {
                    homeListData.list.forEach {
                        list.add(HomeItemBean(HomeItemBean.NORMAL, it))
                    }
                }
                totalList.addAll(list)
                //处理引导插入
                if (totalList.size >= GUIDE_INDEX_01 && homeListData.tagRemind) {
                    if (needGuide1) {
                        needGuide1 = false
                        logger("添加tag引导")
                        val index = GUIDE_INDEX_01 - (totalList.size - list.size)
                        list.add(index, HomeItemBean(HomeItemBean.GUIDE_TO_ADD_TAG, Any()))
                    }

                }
                if (totalList.size >= GUIDE_INDEX_02 && coverRemind != null) {
                    if (needGuide2) {
                        needGuide2 = false
                        logger("添加完善引导")
                        val index = GUIDE_INDEX_02 - (totalList.size - list.size)
                        list.add(index, HomeItemBean(HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION, coverRemind!!))
                    }
                }
                val rList = RootListData(isPull = type != QueryType.LOAD_MORE, list = list, hasMore = homeListData.hasMore)
                offset += list.size
                emit(ReactiveData(NetStateType.SUCCESS, rList))
            }, error = { e ->
                logger("报错了：$e")
//                emit(ReactiveData(NetStateType.ERROR, error = e.coverError()))
                emit(e.coverError())
            }, final = {
                logger("最终返回")
            }, needLoadState = type == QueryType.INIT)

        }

    }

}

