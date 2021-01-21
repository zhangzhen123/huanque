package com.julun.huanque.core.ui.homepage

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.CustomListener
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.bigkoo.pickerview.view.TimePickerView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.EditCityBean
import com.julun.huanque.common.bean.beans.EducationBean
import com.julun.huanque.common.bean.beans.SchoolInfo
import com.julun.huanque.common.bean.beans.SingleSchool
import com.julun.huanque.common.bean.forms.SaveSchoolForm
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.SchoolResultAdapter
import com.julun.huanque.core.utils.EditUtils
import com.julun.huanque.core.viewmodel.SchoolViewModel
import kotlinx.android.synthetic.main.act_figure.*
import kotlinx.android.synthetic.main.act_home_town.*
import kotlinx.android.synthetic.main.act_school.*
import kotlinx.android.synthetic.main.act_school.con_progress
import kotlinx.android.synthetic.main.act_school.frame
import kotlinx.android.synthetic.main.act_school.header_page
import kotlinx.android.synthetic.main.act_school.progressBar
import kotlinx.android.synthetic.main.act_school.tv_save
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.textColor
import java.lang.Exception
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2021/1/5 19:34
 *@描述 学校页面
 */
@Route(path = ARouterConstant.SchoolActivity)
class SchoolActivity : BaseActivity() {

    companion object {
        const val SchoolInfo = "SchoolInfo"
        fun newInstance(act: Activity, schoolInfo: SchoolInfo) {
            val intent = Intent(act, SchoolActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                val bundle = Bundle()
                bundle.putSerializable(SchoolInfo, schoolInfo)
                intent.putExtras(bundle)
                act.startActivity(intent)
            }
        }
    }

    private val mViewModel: SchoolViewModel by viewModels()

    //入学年份
    private var pvTime: TimePickerView? = null

    //学历
    private var pvOptions: OptionsPickerView<EducationBean>? = null

    private val mAdapter = SchoolResultAdapter()

    override fun getLayoutId() = R.layout.act_school

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        var index = intent?.getIntExtra(ParamConstant.Index, -1) ?: -1

        if (index == -1) {
            con_progress.hide()
        } else {
            con_progress.show()
            header_page.textOperation.show()
//            if (index == 4) {
//                header_page.textOperation.text = "完成"
//            } else {
            header_page.textOperation.text = "跳过"
//            }
            progressBar.progress = (100 / 5) * (index + 1)
        }
        mViewModel.index = index

        mViewModel.originalSchoolInfo = intent?.getSerializableExtra(SchoolInfo) as? SchoolInfo
        mViewModel.selectSchool = SingleSchool().apply {
            schoolName = mViewModel.originalSchoolInfo?.school ?: ""
        }
        header_page.textTitle.text = "学校"
        initRecyclerView()
        initViewModel()
        initTimePicker()
        pvTime?.show()
        mViewModel.querySchool()

    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
        if (mViewModel.index >= 0) {
            header_page.textOperation.onClickNew {
                //跳过
                EditUtils.goToNext(this, mViewModel.index)
            }
        }
        tv_education_title.onClickNew {
            //学历
            val configList = mViewModel.schoolData.value?.configList ?: return@onClickNew
            if (configList.isEmpty()) {
                return@onClickNew
            }
            if (pvOptions == null) {
                initOptionPicker()
            }
            val educationCode = mViewModel.schoolData.value?.education
            if (educationCode?.isNotEmpty() == true) {
                configList.forEachIndexed { index, singleData ->
                    if (singleData.educationCode == educationCode) {
                        pvOptions?.setSelectOptions(index)
                        return@forEachIndexed
                    }
                }
            } else {
                pvOptions?.setSelectOptions(0)
            }
            pvOptions?.show()
        }
        phone_num_clear.onClickNew {
            et.setText("")
        }

        et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    phone_num_clear.show()
                    val education = mViewModel.schoolData.value?.education
                    if (mViewModel.selectSchool?.schoolName != s.toString()) {
                        if (education == "") {
                            mViewModel.searchSchool(s.toString(), null)
                        } else {
                            mViewModel.searchSchool(s.toString(), education)
                        }
                        recycler_view.show()
                    } else {
                        recycler_view.hide()
                        mAdapter.setList(null)
                    }
                } else {
                    mViewModel.selectSchool = null
                    recycler_view.hide()
                    mAdapter.setList(null)
                    phone_num_clear.hide()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        tv_save.onClickNew {
            val form = SaveSchoolForm()
            //学历Code
            val educationCode = mViewModel.schoolData.value?.education
            val schoolId = mViewModel.selectSchool?.schoolId
            val schoolName = mViewModel.selectSchool?.schoolName
            val currentDate = mViewModel.currentDate
            //原始数据
            val originalData = mViewModel.originalSchoolInfo
            if (originalData?.educationCode != educationCode) {
                //学历发生变化
                form.education = educationCode
            }
//            if (schoolId != null && schoolName != null && schoolName != originalData?.school) {
            //学校发生变化
            form.schoolId = schoolId
//            }

            if (currentDate != null) {
                val dateStr = TimeUtils.formatTime(currentDate.time, TimeUtils.TIME_FORMAT_YEAR_4)
                if (dateStr != originalData?.startYear) {
                    //入学日期发生变化
                    form.startYear = dateStr
                }
            }


            if (form.education == null && schoolName == originalData?.school && form.startYear == null) {
                //数据没有变化，直接返回
                EditUtils.goToNext(this, mViewModel.index)
            } else {
                mViewModel.saveSchool(form, schoolName ?: "", mViewModel.schoolData.value?.educationText ?: "")
            }
        }
        showViewByData(mViewModel.originalSchoolInfo ?: return)
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = mAdapter
        val tv = TextView(this).apply {
            text = "*请根据搜索结果选择学校"
            gravity = Gravity.CENTER_VERTICAL
            textSize = 12f
            textColor = GlobalUtils.getColor(R.color.black_999)
        }
        tv.setPadding(dp2px(15), 0, 0, 0)
        mAdapter.addHeaderView(tv)
        val params = tv.layoutParams
        params.height = dp2px(37)
        tv.layoutParams = params
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val tempSchool = adapter.getItem(position) as? SingleSchool ?: return@setOnItemClickListener
            val schoolName = mViewModel.selectSchool?.schoolName ?: ""
//            if (tempSchool.schoolName != schoolName) {
            //选择新的学校
            mViewModel.selectSchool = tempSchool
            et.setText(tempSchool.schoolName)
            et.setSelection(et.text.length)
//            }
            //隐藏输入法
            ScreenUtils.hideSoftInput(this)
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.schoolData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                if (it.education.isEmpty()) {
                    //没有学历数据
                    tv_education_title.performClick()
                }
            }
        })
        mViewModel.searchShoolResult.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                mAdapter.setList(it.schoolList)
            }
        })
        mViewModel.processData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                EventBus.getDefault().post(it)
                EditUtils.goToNext(this, mViewModel.index)
            }
        })
    }

    /**
     * 显示学校数据
     */
    private fun showViewByData(info: SchoolInfo) {
        tv_education.text = info.education
        et.setText(info.school)
        //显示入学年份
        if (info.startYear.isNotEmpty()) {
            try {
                val selectedDate = Calendar.getInstance() //系统当前时间
                selectedDate.set(info.startYear.toInt(), 1, 1)
                pvTime?.setDate(selectedDate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun initTimePicker() { //Dialog 模式下，在底部弹出
        /**
         * @description
         *
         * 注意事项：
         * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
         * 具体可参考demo 里面的两个自定义layout布局。
         * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
         */

        val currentDate = Calendar.getInstance() //系统当前时间
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

        val startDate = Calendar.getInstance()
        startDate.set(1980, currentMonth, currentDay)

        val endDate = Calendar.getInstance()
        endDate.set(currentYear, currentMonth, currentDay)

        val selectedDate = Calendar.getInstance() //系统当前时间
        selectedDate.set(currentYear - 4, currentMonth, currentDay)


//        val startDate = Calendar.getInstance()
//        startDate.set(2014, 1, 23)
//        val endDate = Calendar.getInstance()
//        endDate.set(2027, 2, 28)

        pvTime = TimePickerBuilder(this, OnTimeSelectListener { date, v ->
//            mViewModel?.birthdayData = date
//            tv_bir.text = getTime(date)
//            judgeNextEnable()
        })
            .setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setSubmitColor(GlobalUtils.getColor(R.color.black_333))
            .setCancelColor(GlobalUtils.getColor(R.color.black_333))
            .setOutSideCancelable(false)
            .setLayoutRes(R.layout.view_school_update, object : CustomListener {
                override fun customLayout(v: View?) {
                    if (v == null) {
                        return
                    }
                }
            })
            .setDecorView(frame)
            .setSubmitText("完成")
            .setTitleText("选择生日")
            .setTitleSize(16)
            .setSubCalSize(14)
            .setContentTextSize(20)
            .setTitleColor(GlobalUtils.getColor(R.color.black_333))
            .setTimeSelectChangeListener { Log.i("pvTime", "onTimeSelectChanged") }
            .setType(booleanArrayOf(true, false, false, false, false, false))
            .isDialog(false) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .addOnCancelClickListener { Log.i("pvTime", "onCancelClickListener") }
            .setItemVisibleCount(5) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(2.5f)
            .setLabel("", "", "", "", "", "")
            .setTimeSelectChangeListener { date ->
//                mViewModel.birthdayData.postValue(date)
                mViewModel.currentDate = date
                logger.info("School Date = ${TimeUtils.formatTime(date.time, TimeUtils.TIME_FORMAT_YEAR_2)}")
            }
            .isAlphaGradient(true)
            .setDividerColor(0x00000000)
            .setOutSideColor(0x00000000) //设置外部遮罩颜色
            .build()
        mViewModel.currentDate = selectedDate.time
        pvTime?.setKeyBackCancelable(false)

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp2px(306),
            Gravity.BOTTOM
        )
        params.leftMargin = 0
        params.rightMargin = 0
        pvTime?.dialogContainerLayout?.layoutParams = params
//        val mDialog: Dialog = pvTime?.dialog ?: return
//        val dialogWindow = mDialog.window ?: return
//        dialogWindow.setWindowAnimations(R.style.picker_view_slide_right_anim) //修改动画样式
//        dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
//        dialogWindow.setDimAmount(0.3f)
    }

    /**
     * 初始化操作按钮
     */
    private fun initOptionPicker() { //Dialog 模式下，在底部弹出
        val educationData = mViewModel.schoolData.value?.configList ?: return
        /**
         * @description
         *
         * 注意事项：
         * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
         * 具体可参考demo 里面的两个自定义layout布局。
         * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
         */

        pvOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            //返回的分别是三个级别的选中位置
            if (ForceUtils.isIndexNotOutOfBounds(options1, educationData)) {
                val educationBean = educationData[options1]
                tv_education.text = educationBean.educationText
                mViewModel.schoolData.value?.let {
                    it.education = educationBean.educationCode
                    it.educationText = educationBean.educationText
                }
            }
        })
            .setTitleText("城市选择")
            .setContentTextSize(20)//设置滚轮文字大小
            .setDividerColor(Color.LTGRAY)//设置分割线的颜色
            .setBgColor(Color.WHITE)
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .setTitleBgColor(Color.DKGRAY)
            .setTitleColor(Color.LTGRAY)
            .setCancelColor(Color.YELLOW)
            .setSubmitColor(Color.YELLOW)
            .setTextColorCenter(GlobalUtils.getColor(R.color.black_333))
            .setTextColorOut(GlobalUtils.getColor(R.color.black_999))
            .setLayoutRes(R.layout.view_edit_education, CustomListener { view ->
                val tvSubmit = view.findViewById<View>(R.id.tv_certain)
                tvSubmit.setOnClickListener {
                    pvOptions?.returnData()
                    pvOptions?.dismiss()
                }
            })
            .setItemVisibleCount(7) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(2.0f)
            .isAlphaGradient(true)
            .setDividerColor(0x00000000)
            .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setOutSideColor(0x33000000) //设置外部遮罩颜色
            .build()
        pvOptions?.setPicker(educationData) //一级选择器

        val mDialog: Dialog = pvOptions?.dialog ?: return
        val params = FrameLayout.LayoutParams(
            ScreenUtils.getScreenWidth(), dp2px(320f), Gravity.BOTTOM
        )
        params.leftMargin = 0
        params.rightMargin = 0
        pvOptions?.dialogContainerLayout?.layoutParams = params
        val dialogWindow = mDialog.window ?: return
        dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //修改动画样式
        dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
        dialogWindow.setDimAmount(0.3f)

    }
}