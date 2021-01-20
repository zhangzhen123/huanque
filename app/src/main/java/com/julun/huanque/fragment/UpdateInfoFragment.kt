package com.julun.huanque.fragment

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.forms.UpdateInformationForm
import com.julun.huanque.common.constant.DialogOrderNumber
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.ChannelCodeHelper
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.suger.inVisible
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.viewmodel.FillInformationViewModel
import kotlinx.android.synthetic.main.act_fill_information.*
import kotlinx.android.synthetic.main.fragment_update_info.*
import kotlinx.android.synthetic.main.fragment_update_info.con_root
import kotlinx.android.synthetic.main.fragment_update_info.et_invitation_code
import kotlinx.android.synthetic.main.fragment_update_info.et_nickname
import kotlinx.android.synthetic.main.fragment_update_info.tv_bir
import kotlinx.android.synthetic.main.fragment_update_info.tv_next
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2020/9/18 16:57
 *@描述 更新用户信息的弹窗
 */
class UpdateInfoFragment : BaseDialogFragment(), DialogInterface.OnKeyListener {
    companion object {
        fun newInstance(birthday: String): UpdateInfoFragment {
            val fragment = UpdateInfoFragment()
            return fragment
        }
    }

    private val mViewModel: FillInformationViewModel by activityViewModels()

    //生日数据(intent 里面携带的)
    private var mBirthday = ""


    //邀请码 标识位
//    private val CODE_TEMPLATE = "-code-"

    private var pvTime: TimePickerView? = null

    override fun getLayoutId() = R.layout.fragment_update_info

    override fun initViews() {
        logger.info("Main UpdateInfoFragment act = ${requireActivity()}")
        initEvents()
        initTimePicker()
        initViewModel()

        et_nickname.setText(SessionUtils.getNickName())
        //邀请码
//        val extraCode = ChannelCodeHelper.getExternalChannel() ?: ""
//        if (extraCode.contains(CODE_TEMPLATE)) {
//            val strings = extraCode.split(CODE_TEMPLATE)
//            val code = strings.getOrNull(1)
//            if (code?.isNotEmpty() == true) {
        et_invitation_code.setText(SessionUtils.getInviteCode())
//            }
//        }

        mBirthday = SessionUtils.getBirthday()
        SessionUtils.setBirthday("")
        tv_bir.text = mBirthday

        mViewModel.nicknameChange = false
        mViewModel.nicknameEnable.value = true
        mViewModel.headerPicData.value = SessionUtils.getHeaderPic()

        try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd") //注意月份是MM
            mViewModel.birthdayData = simpleDateFormat.parse(mBirthday)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun initViewModel() {
        mViewModel.nicknameEnable.observe(this, androidx.lifecycle.Observer {
            if (it == true) {
                judgeNextEnable()
            }
        })
        mViewModel.updateSuccessFlag.observe(this, androidx.lifecycle.Observer {
            if (it == true) {
                mViewModel.updateSuccessFlag.value = null
                dismiss()
            }
        })

        mViewModel.headerPicData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                if (it.contains("http")) {
                    sdv_header.setImageURI(it)
                } else {
                    sdv_header.loadImage(it, 90f, 90f)
                }
                judgeNextEnable()
            }
        })
    }

    private fun initEvents() {
        et_nickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mViewModel.nicknameEnable.value = false
                mViewModel.nicknameChange = true
                val contentLength = s?.length ?: 0
                if (contentLength > 0) {
                    iv_clear_nickname.show()
                } else {
                    iv_clear_nickname.inVisible()
                }
                judgeNextEnable()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        et_invitation_code.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val contentLength = s?.length ?: 0
                if (contentLength > 0) {
                    iv_invitation_code.show()
                } else {
                    iv_invitation_code.inVisible()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        tv_bir.onClickNew {
            //选择生日
            pvTime?.show(tv_bir)
        }

        iv_clear_nickname.onClickNew {
            //清空昵称
            et_nickname.setText("")
        }

        iv_invitation_code.onClickNew {
            //清空邀请码
            et_invitation_code.setText("")
        }
        con_root.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                //昵称有变化再请求，没有变化 无需请求
                if (ev?.action == MotionEvent.ACTION_DOWN && mViewModel.nicknameChange) {
                    val nickname = et_nickname.text.toString()
                    if (mViewModel.nicknameChange && mViewModel.nicknameEnable.value != true && nickname.isNotEmpty()) {
                        mViewModel.checkNickName(nickname)
                    }
                }
            }
        }

        tv_next.onClickNew {
            //下一步
            if (!tv_next.isSelected) {
                ToastUtils.show("请完善信息")
                return@onClickNew
            }
            //判断数据是否和之前一致
            val birthday =
                getTime(mViewModel.birthdayData ?: return@onClickNew) ?: return@onClickNew
            val nickname = et_nickname.text.toString()
            val headerPic = mViewModel.headerPicData.value ?: ""
            val code = et_invitation_code.text.toString().trim()
            val form = UpdateInformationForm()

            if (headerPic != SessionUtils.getHeaderPic()) {
                //头像有变化
                form.headPic = headerPic
            }
            if (nickname != SessionUtils.getNickName()) {
                //昵称有变化
                form.nickname = nickname
            }
            if (birthday != mBirthday) {
                //生日有变化
                form.birthday = birthday
            }
            form.invitationCode = code

            if (form.headPic?.isNotEmpty() == true || form.nickname?.isNotEmpty() == true || form.birthday?.isNotEmpty() == true || form.invitationCode?.isNotEmpty() == true) {
                //需要更新
                mViewModel.updateCard(form)
            } else {
                //直接隐藏
                dismiss()
            }
        }

        sdv_header.onClickNew {
            //上传头像
            mViewModel.openPicFlag.value = true
        }

    }


    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener(this)
    }

    override fun configDialog() {
        setDialogSize(Gravity.CENTER,  width = ViewGroup.LayoutParams.MATCH_PARENT,padding = 30)
    }
    /**
     * 判断下一步是否可用
     */
    private fun judgeNextEnable() {
        val nicknameEnable = mViewModel.nicknameEnable.value == true
        val birEnable = mViewModel?.birthdayData != null

        tv_next.isSelected = nicknameEnable && birEnable
    }

    fun closeKeyBoard() {
        ScreenUtils.hideSoftInput(requireActivity())
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

        pvTime = TimePickerBuilder(requireActivity(), OnTimeSelectListener { date, v ->
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

    override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
        logger.info("Message keyCode = $keyCode")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            //这里注意当不是返回键时需将事件扩散，否则无法处理其他点击事件
            return false;
        }
    }

    override fun order() = 200

}