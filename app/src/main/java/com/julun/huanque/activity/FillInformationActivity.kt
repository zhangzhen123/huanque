package com.julun.huanque.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.viewmodel.FillInformationViewModel
import kotlinx.android.synthetic.main.act_fill_information.*
import org.jetbrains.anko.sdk23.listeners.textChangedListener
import java.text.SimpleDateFormat
import java.util.*


/**
 *@创建者   dong
 *@创建时间 2020/7/3 16:47
 *@描述 填写资料页面
 */
class FillInformationActivity : BaseActivity() {

    companion object {
        fun newInstance(activity: Activity) {
            val intent = Intent(activity, FillInformationActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private var mViewModel: FillInformationViewModel? = null

    private var pvTime: TimePickerView? = null

    override fun getLayoutId() = R.layout.act_fill_information

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        findViewById<TextView>(R.id.tvTitle).text = "消息设置"
        initTimePicker()
        mViewModel?.currentStatus?.value = FillInformationViewModel.FIRST
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel = ViewModelProvider(this).get(FillInformationViewModel::class.java)
        mViewModel?.currentStatus?.observe(this, Observer {
            if (it != null) {
                when (it) {
                    FillInformationViewModel.FIRST -> {
                        //显示第一步
                        con_first.show()
                        con_second.hide()
                    }
                    FillInformationViewModel.SECOND -> {
                        //显示第二步
                        con_first.hide()
                        con_second.show()
                    }
                    else -> {

                    }
                }
            }
        })
    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew { finish() }
        et_nickname.textChangedListener {
            afterTextChanged { judgeNextEnable() }
        }

        tv_next.onClickNew {
            mViewModel?.currentStatus?.value = FillInformationViewModel.SECOND
        }
        con_root.onClickNew {
            closeKeyBoard()
        }
        iv_male.onClickNew {
            //选中男
            showSex(true)
        }
        iv_female.onClickNew {
            //选中女
            showSex(false)
        }
        tv_bir.onClickNew {
            pvTime?.show(tv_bir)
        }
    }

    /**
     * 选中性别
     */
    private fun showSex(male: Boolean) {
        iv_male.isSelected = male
        iv_female.isSelected = !male
        judgeNextEnable()
    }

    /**
     * 判断下一步是否可用
     */
    private fun judgeNextEnable() {
        val sexEnable = iv_male.isSelected || iv_female.isSelected
        val nicknameEnable = et_nickname.text.toString().isNotEmpty()
        val birEnable = mViewModel?.birthdayData != null

        tv_next.isEnabled = sexEnable && nicknameEnable && birEnable
    }

    fun closeKeyBoard() {
        ScreenUtils.hideSoftInput(this)
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
        startDate.set(currentYear - 110, currentMonth, currentDay)

        val endDate = Calendar.getInstance()
        endDate.set(currentYear - 18, currentMonth, currentDay)


        val selectedDate = Calendar.getInstance() //系统当前时间
        selectedDate.set(1995, 0, 1)

//        val startDate = Calendar.getInstance()
//        startDate.set(2014, 1, 23)
//        val endDate = Calendar.getInstance()
//        endDate.set(2027, 2, 28)

        pvTime = TimePickerBuilder(this, OnTimeSelectListener { date, v ->
            mViewModel?.birthdayData = date
            tv_bir.text = getTime(date)
            judgeNextEnable()
        })
            .setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setSubmitColor(GlobalUtils.getColor(R.color.black_333))
            .setCancelColor(GlobalUtils.getColor(R.color.black_333))
            .setSubmitText("完成")
            .setTitleText("选择生日")
            .setTitleSize(16)
            .setSubCalSize(14)
            .setTitleColor(GlobalUtils.getColor(R.color.black_333))
            .setTimeSelectChangeListener { Log.i("pvTime", "onTimeSelectChanged") }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .addOnCancelClickListener { Log.i("pvTime", "onCancelClickListener") }
            .setItemVisibleCount(7) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(4.0f)
            .isAlphaGradient(true)
            .setDividerColor(0x00000000)
            .build()
        val mDialog: Dialog = pvTime?.dialog ?: return
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )
        params.leftMargin = 0
        params.rightMargin = 0
        pvTime?.dialogContainerLayout?.layoutParams = params
        val dialogWindow = mDialog.window ?: return
        dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //修改动画样式
        dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
        dialogWindow.setDimAmount(0.3f)
    }

    private fun getTime(date: Date): String? { //可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.time)
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }
}