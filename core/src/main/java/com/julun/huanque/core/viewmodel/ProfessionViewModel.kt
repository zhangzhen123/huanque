package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.EditProfessionBean
import com.julun.huanque.common.bean.beans.ProfessionInfo
import com.julun.huanque.common.bean.beans.SingleProfessionFeatureConfig
import com.julun.huanque.common.bean.beans.UserProcessBean
import com.julun.huanque.common.bean.forms.SaveProfessionForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2021/1/6 15:35
 *@描述 职业ViewModel
 */
class ProfessionViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    //原始职业数据
    var originalProfession: ProfessionInfo? = null

    //原始收入ID
    var oriIncomeCode = ""

    //当前行业名称
    var nameH = ""

    //当前职业名称
    var nameZ = ""

    //原始职业特点列表
    var oriFeatureTypes = mutableListOf<String>()

    //职业特性，最大选择数量
    var maxFeatureCount = 0

    //当前选择的职业数量
    var curFeatureCount = 0

    //职业数据
    val professionData: MutableLiveData<EditProfessionBean> by lazy { MutableLiveData<EditProfessionBean>() }

    //职业特点数据
    val featureData: MutableLiveData<MutableList<SingleProfessionFeatureConfig>> by lazy { MutableLiveData<MutableList<SingleProfessionFeatureConfig>>() }

    //资料完善度数据
    val processData: MutableLiveData<UserProcessBean> by lazy { MutableLiveData<UserProcessBean>() }

    /**
     * 获取职业数据
     */
    fun initProfession() {
        viewModelScope.launch {
            request({
                val editProfessionBean = userService.initProfession().dataConvert()
                maxFeatureCount = editProfessionBean.maxFeature
                professionData.value = editProfessionBean
                oriIncomeCode = editProfessionBean.incomeCode
                editProfessionBean.myFeatureList.forEach {
                    oriFeatureTypes.add(it.professionFeatureCode)
                }
                oriFeatureTypes.sort()
            }, {})
        }
    }


    /**
     * 保存职业
     */
    fun saveProfession(form: SaveProfessionForm) {
        viewModelScope.launch {
            request({
                processData.value = userService.saveProfession(form).dataConvert()
                if (form.professionId != originalProfession?.professionId) {
                    //职业ID有变动,发送事件
                    val info = ProfessionInfo()
                    info.professionId = form.professionId ?: return@request
                    info.professionTypeText = nameH
                    info.professionName = nameZ
                    EventBus.getDefault().post(info)
                }
            }, {})
        }
    }

}