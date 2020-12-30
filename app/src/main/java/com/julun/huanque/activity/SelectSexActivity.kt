package com.julun.huanque.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.events.FinishToLoginEvent
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.fragment.ConfirmSexFragment
import com.julun.huanque.fragment.PersonalInformationProtectionFragment
import com.julun.huanque.viewmodel.SelectSexViewModel
import kotlinx.android.synthetic.main.act_select_sex.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 *@创建者   dong
 *@创建时间 2020/9/17 21:20
 *@描述 选择性别页面
 */
class SelectSexActivity : BaseActivity() {

    companion object {
        //初始化性别接口返回的数据
        const val LoginTagInfo = "LoginTagInfo"
        fun newInstance(activity: Activity) {
            val intent = Intent(activity, SelectSexActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val mViewModel: SelectSexViewModel by viewModels()
    private val mPersonalInformationProtectionFragment =
        PersonalInformationProtectionFragment.newInstance(PersonalInformationProtectionFragment.SelectActivity)

    private val mSelectSexFragment = ConfirmSexFragment()

    override fun getLayoutId() = R.layout.act_select_sex

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        StatusBarUtil.setColor(this, GlobalUtils.formatColor("#00FFFFFF"))
        initViewModel()
        //隐私协议弹窗
        mPersonalInformationProtectionFragment.show(
            supportFragmentManager,
            "PersonalInformationProtectionFragment"
        )
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel?.infoBean?.observe(this, Observer {
            if (it != null) {
                val intent = Intent(this, SelectTagActivity::class.java)
                if (ForceUtils.activityMatch(intent)) {
                    val bundle = Bundle().apply {
                        putSerializable(LoginTagInfo, it.tagInfo)
                    }
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
            }
        })
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
//        ivback.onClickNew {
//            finish()
//        }

        iv_male.onClickNew {
//            selectSex(true)
            mViewModel.sexData.value = Sex.MALE
            mSelectSexFragment.show(supportFragmentManager, "ConfirmSexFragment")
        }

        iv_female.onClickNew {
//            selectSex(false)
            mViewModel.sexData.value = Sex.FEMALE
            mSelectSexFragment.show(supportFragmentManager, "ConfirmSexFragment")
        }

//        tv_go.onClickNew {
//            if (!iv_male.isSelected && !iv_female.isSelected) {
//                ToastUtils.show("请先选择你的性别")
//                return@onClickNew
//            }
//            mViewModel?.updateSex(iv_male.isSelected)
//        }
    }

    /**
     * 选中性别
     */
    private fun selectSex(male: Boolean) {
//        if (male) {
//            //男
//            iv_male.isSelected = true
//            iv_female.isSelected = false
//
//            iv_male_select.show()
//            iv_female_select.hide()
//        } else {
//            //女
//            iv_male.isSelected = false
//            iv_female.isSelected = true
//
//            iv_male_select.hide()
//            iv_female_select.show()
//        }
//        tv_go.isSelected = true
    }

    override fun isRegisterEventBus() = true

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun toLoginEvent(event: FinishToLoginEvent) {
        finish()
    }

    override fun onBackPressed() {
        //禁用返回键
//        super.onBackPressed()
    }

}