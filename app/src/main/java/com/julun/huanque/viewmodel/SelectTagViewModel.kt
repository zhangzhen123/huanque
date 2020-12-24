package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.LoginTagInfo
import com.julun.huanque.common.bean.beans.UpdateSexBean
import com.julun.huanque.common.bean.forms.TagIdsForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch

/**
 *@创建者   dong
 *@创建时间 2020/12/23 16:11
 *@描述 选择标签ViewModel
 */
class SelectTagViewModel : BaseViewModel() {
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //性别接口返回的数据
    val loginTagData: MutableLiveData<LoginTagInfo> by lazy { MutableLiveData<LoginTagInfo>() }

    //修改外界数量标识位
    val updateSelectCountFlag: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //标签类型的数量
    var totalTypeTabCount = 0

    //当前选中的标签下标
    val currentSelectIndex: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    /**
     * 保存标签数据
     */
    fun saveTags(ids: String) {
        viewModelScope.launch {
            request({
                socialService.initLikeTag(TagIdsForm(ids))
                    .dataConvert()
            }, {})
        }
    }

    /**
     * 获取标签数据
     */
    fun getTagInfo() {
        viewModelScope.launch {
            request({
                loginTagData.value = socialService.socialType().dataConvert()
            }, {})
        }
    }

}