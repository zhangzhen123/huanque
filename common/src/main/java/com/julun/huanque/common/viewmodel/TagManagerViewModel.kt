package com.julun.huanque.common.viewmodel

import androidx.lifecycle.*
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.bean.beans.ManagerTagTabBean
import com.julun.huanque.common.bean.forms.TagForm
import com.julun.huanque.common.bean.forms.TagListForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.TagService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.launch


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/30 16:33
 *
 *@Description: TagManagerViewModel 由于标签的改动很多地方都有 这里做成全局单一对象
 *
 */
class TagManagerViewModel : BaseViewModel() {

    private val service: TagService by lazy { Requests.create(TagService::class.java) }

    /**
     * 标记又没tag变动
     */
    val tagChange: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val currentTagList = arrayListOf<UserTagBean>()

    /**
     * 喜欢或者取消的回调
     */
    val tagChangeStatus: MutableLiveData<ReactiveData<UserTagBean>> by lazy { MutableLiveData<ReactiveData<UserTagBean>>() }


    val tagGroupRemove: MutableLiveData<ReactiveData<UserTagBean>> by lazy { MutableLiveData<ReactiveData<UserTagBean>>() }

    val saveTagList: MutableLiveData<ReactiveData<VoidResult>> by lazy { MutableLiveData<ReactiveData<VoidResult>>() }

    //    val managerTabList: MutableLiveData<List<ManagerTagBean>> by lazy { MutableLiveData<List<ManagerTagBean>>() }
    val tabList: LiveData<ReactiveData<List<ManagerTagTabBean>>> = queryState.switchMap {
        liveData {
            request({

                val result = service.manageList().dataConvert()
                emit(result.tagList.convertRtData())
                currentTagList.clear()
                currentTagList.addAll(result.manageList)
                tagChange.value = true
            }, error = { e ->
                logger("报错了：$e")
                emit(e.convertError())
            }, final = {
                logger("最终返回")
            }, needLoadState = true)
        }

    }

    fun tagLike(tag: UserTagBean/*, parentTag: ManagerTagTabBean*/) {
        viewModelScope.launch {

            request({
                val result = service.tagLike(TagForm(tag.tagId)).dataConvert()
                tag.like = true
                tagChangeStatus.value = tag.convertRtData()
                //这里创建一个用于发送标签管理的对象
                val tagBean = UserTagBean(
                    tagId = result.parentTagId,
                    likeCnt = 1,
                    tagName = result.parentTagName
//                    tagIcon = parentTag.tagIcon,
//                    tagPic = parentTag.tagPic
                )
                addTag(tagBean)
            }, error = {
                tagChangeStatus.value = it.convertError()
            })
        }

    }

    fun tagCancelLike(tag: UserTagBean/*, parentTag: ManagerTagTabBean*/) {
        viewModelScope.launch {

            request({
                val result = service.tagCancelLike(TagForm(tag.tagId)).dataConvert()
                tag.like = false
                tagChangeStatus.value = tag.convertRtData()
                //这里创建一个用于发送标签管理的对象
                val tagBean = UserTagBean(
                    tagId = result.parentTagId,
                    likeCnt = 1,
                    tagName = result.parentTagName
//                    tagIcon = parentTag.tagIcon,
//                    tagPic = parentTag.tagPic
                )
                removeTag(tagBean)

            }, error = {
                tagChangeStatus.value = it.convertError()
            })
        }

    }


    /**
     *这里的ManagerTagBean代表一级标签 子级的标签不会传到这里
     */
    fun addTag(itemBean: UserTagBean) {
        if (currentTagList.isEmpty()) {
            logger("当前的全局标签列表数据为空！！")
            return
        }
        tagHasChange = true
        var isExist = false
        currentTagList.forEach {
            if (it.tagId == itemBean.tagId) {
                it.likeCnt = it.likeCnt + itemBean.likeCnt
                isExist = true
            }
        }
        if (!isExist) {
            currentTagList.add(itemBean)
        }
        tagChange.value = true
    }

    fun removeTag(itemBean: UserTagBean) {
        if (currentTagList.isEmpty()) {
            logger("当前的全局标签列表数据为空！！")
            return
        }
        tagHasChange = true
        val iterator = currentTagList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.tagId == itemBean.tagId) {
                item.likeCnt = item.likeCnt - itemBean.likeCnt
                if (item.likeCnt <= 0) {
                    iterator.remove()
                }
            }
        }
        tagChange.value = true
    }


    fun tagCancelGroupLike(tag: UserTagBean) {
        tagHasChange = true
        viewModelScope.launch {

            request({
                val result = service.tagCancelLike(TagForm(tag.tagId)).dataConvert()
                tag.like = false
                tagGroupRemove.value = tag.convertRtData()
                removeTag(tag)

            }, error = {
                tagGroupRemove.value = it.convertError()
            })
        }

    }

    //标签总体有没有变动 用于关闭返回时是否通知数据更新
    var tagHasChange = false

    //标记是否有标签顺序变动 用于保存顺序
    var tagSequenceHasChange = false

    /**
     * 保存标签的顺序
     */
    fun saveTagList() {
        if (!tagSequenceHasChange) {
            return
        }
        viewModelScope.launch {

            request({
                val tagsBuilder = StringBuilder()
                currentTagList.forEachIndexed { index, managerTagBean ->
                    if (index == currentTagList.size - 1) {
                        tagsBuilder.append("${managerTagBean.tagId}")
                    } else {
                        tagsBuilder.append("${managerTagBean.tagId},")
                    }
                }
                val tags = tagsBuilder.toString()
                val result = service.saveTagManage(TagListForm(tags)).dataConvert()
                saveTagList.value = result.convertRtData()
                tagSequenceHasChange = false
            }, error = {
                saveTagList.value = it.convertError()
            })
        }

    }
}