package com.julun.huanque.core.ui.homepage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.viewModels
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.CustomListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ConstellationUtils
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.TimeUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.UpdateBirthdayViewModel
import kotlinx.android.synthetic.main.act_birthday.*
import org.greenrobot.eventbus.EventBus
import java.sql.Time
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2021/1/5 11:33
 *@描述 更新年龄页面
 */
class UpdateBirthdayActivity : BaseActivity() {

    companion object {
        /**
         * @param birthday 生日
         */
        fun newInstance(act: Activity, birthday: String) {
            val intent = Intent(act, UpdateBirthdayActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.Birthday, birthday)
                act.startActivity(intent)
            }
        }
    }

    private val mViewModel: UpdateBirthdayViewModel by viewModels()

    private var pvTime: TimePickerView? = null

    override fun getLayoutId() = R.layout.act_birthday

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val birthday = intent?.getStringExtra(ParamConstant.Birthday) ?: ""

        mViewModel.originalDate = TimeUtils.string2Date("yyyy-MM-dd", birthday)
        mViewModel.birthdayData.value = mViewModel.originalDate

        header_page.textTitle.text = "出生日期"
        initTimePicker()
        pvTime?.show()
        initViewModel()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
        tv_constellation.onClickNew {
            //点击星座
            val constellType = ConstellationUtils.getConstellation(mViewModel.originalDate ?: return@onClickNew).type
            if (constellType.isNotEmpty()) {
                //星座弹窗
                ConstellationFragment.newInstance(constellType).show(supportFragmentManager, "ConstellationFragment")
            }

        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.birthdayData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                val age = TimeUtils.getAgeByDate(it)
                tv_age.text = "${age}岁"

                val constellationName = ConstellationUtils.getConstellation(it)
                tv_constellation.text = constellationName.name
            }
        })

        mViewModel.processData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                EventBus.getDefault().post(it)
                finish()
            }
        })
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
        startDate.set(currentYear - 80, currentMonth, currentDay)

        val endDate = Calendar.getInstance()
        endDate.set(currentYear - 18, currentMonth, currentDay)
        val selectedDate = Calendar.getInstance() //系统当前时间

        val selTime = mViewModel.originalDate
        if (selTime != null) {
            selectedDate.time = selTime
        }


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
            .setLayoutRes(R.layout.view_timer_update, object : CustomListener {
                override fun customLayout(v: View?) {
                    if (v == null) {
                        return
                    }
                    val tv_certain = v.findViewById<View>(R.id.tv_certain)
                    tv_certain.onClickNew {
                        //保存按钮
                        val originDate = mViewModel.originalDate ?: return@onClickNew
                        val curDate = mViewModel.birthdayData.value ?: return@onClickNew
                        if (originDate.time != curDate.time) {
                            //生日数据有变化
                            val birthday = TimeUtils.formatTime(curDate.time, TimeUtils.TIME_FORMAT_YEAR_2)
                            mViewModel.updateBirthday(birthday)
                        }
                    }

                }
            })
            .setDecorView(frame)
            .setSubmitText("完成")
            .setTitleText("选择生日")
            .setTitleSize(16)
            .setSubCalSize(14)
            .setTitleColor(GlobalUtils.getColor(R.color.black_333))
            .setTimeSelectChangeListener { Log.i("pvTime", "onTimeSelectChanged") }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .isDialog(false) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .addOnCancelClickListener { Log.i("pvTime", "onCancelClickListener") }
            .setItemVisibleCount(7) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(4.0f)
            .setTimeSelectChangeListener { date ->
                mViewModel.birthdayData.postValue(date)

            }
            .isAlphaGradient(true)
            .setDividerColor(0x00000000)
            .setOutSideColor(0x00000000) //设置外部遮罩颜色
            .build()

        pvTime?.setKeyBackCancelable(false)

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp2px(350),
            Gravity.BOTTOM
        )
        params.leftMargin = 0
        params.rightMargin = 0
        pvTime?.dialogContainerLayout?.layoutParams = params
    }
}