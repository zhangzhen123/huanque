package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.SaveSchoolForm
import com.julun.huanque.common.bean.forms.SchoolForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.UserService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.request
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2021/1/6 9:19
 *@描述 学校ViewModel
 */
class SchoolViewModel : BaseViewModel() {
    private val userService: UserService by lazy { Requests.create(UserService::class.java) }

    var index = -1

    //最初的学校数据
    var originalSchoolInfo: SchoolInfo? = null

    //学校数据
    val schoolData: MutableLiveData<SchoolBean> by lazy { MutableLiveData<SchoolBean>() }

    //搜索的学校数据
    val searchShoolResult: MutableLiveData<QuerySchoolBean> by lazy { MutableLiveData<QuerySchoolBean>() }

    //当前入学时间
    var currentDate: Date? = null

    //当前选择的学校
    var selectSchool: SingleSchool? = null

    //资料完善度数据
    val processData: MutableLiveData<UserProcessBean> by lazy { MutableLiveData<UserProcessBean>() }

    /**
     * 获取学校数据
     */
    fun querySchool() {
        viewModelScope.launch {
            request({
                schoolData.value = userService.initSchool().dataConvert()
            }, {})
        }
    }

    /**
     * 搜索学校数据
     */
    fun searchSchool(schoolStr: String) {
        viewModelScope.launch {
            request({
                searchShoolResult.value = userService.getSchool(SchoolForm(schoolStr)).dataConvert()
            }, {})
        }
    }

    /**
     * 保存学校数据
     */
    fun saveSchool(form: SaveSchoolForm, schoolName: String, educationName: String) {
        if (form.startYear == null && form.schoolId == null && form.education == null) {
            return
        }
        if(form.schoolId == 0){
            form.schoolId = null
        }
        viewModelScope.launch {
            request({
                processData.value = userService.saveSchool(form).dataConvert()
                val schoolInfo = SchoolInfo()
                schoolInfo.school = schoolName
                schoolInfo.education = educationName
                schoolInfo.educationCode = form.education ?: ""
                schoolInfo.startYear = form.startYear ?: ""
                EventBus.getDefault().post(schoolInfo)
            }, {})
        }
    }


}