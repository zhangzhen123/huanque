package com.julun.huanque.fragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.UserInfoInRoom
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_paidan.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2020/9/21 16:01
 *@描述 派单弹窗
 */
@Route(path = ARouterConstant.FATE_QUICK_MATCH_FRAGMENT)
class FateQuickMatchFragment : BaseDialogFragment() {

    private val mHuanQueViewModel = HuanViewModelManager.huanQueViewModel
    override fun getLayoutId() = R.layout.fragment_paidan


    override fun initViews() {
        iv_close.onClickNew {
            //关闭弹窗
            mHuanQueViewModel.clearFateData()
        }
        tv_chat.onClickNew {
            val fateBean = mHuanQueViewModel.fateQuickMatchData.value ?: return@onClickNew
            val userInfo = fateBean.userInfo
            val bundle = Bundle()
            bundle.putLong(ParamConstant.TARGET_USER_ID, userInfo.userId)
            bundle.putString(ParamConstant.NICKNAME, userInfo.nickname)
            bundle.putBoolean(ParamConstant.FROM, false)
            bundle.putString(ParamConstant.HeaderPic, userInfo.headPic)
            bundle.putString(ParamConstant.FATE_ID, fateBean.fateId)
            ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle)
                .navigation()
            mHuanQueViewModel.clearFateData()
        }

        sdv_header.onClickNew {
            RNPageActivity.start(
                requireActivity(),
                RnConstant.PERSONAL_HOMEPAGE,
                Bundle().apply { putLong("userId", mHuanQueViewModel.fateQuickMatchData.value?.userInfo?.userId ?: return@onClickNew) })
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHuanQueViewModel.fateQuickMatchData.observe(this, Observer {
            if (it != null) {
                showViewByData(it.userInfo)
            } else {
                dismiss()
            }
        })
        mHuanQueViewModel.fateQuickMatchTime.observe(this, Observer {
            if (it != null) {
                showTime(it)
            }
        })
    }


    /**
     * 根据数据显示页面
     */
    private fun showViewByData(bean: UserInfoInRoom) {
        val sex = bean.sex
        var sexDrawable: Drawable? = null
        //性别
        when (sex) {
            Sex.MALE -> {
                tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_male
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_male)
                tv_sex.textColor = Color.parseColor("#58CEFF")
            }
            Sex.FEMALE -> {
                tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_female
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_female)
                tv_sex.textColor = Color.parseColor("#FF9BC5")
            }
            else -> sexDrawable = null
        }
        if (sexDrawable != null) {
            sexDrawable.setBounds(0, 0, sexDrawable.minimumWidth, sexDrawable.minimumHeight)
            tv_sex.setCompoundDrawables(sexDrawable, null, null, null)
        } else {
            tv_sex.setCompoundDrawables(null, null, null, null)
        }
        tv_sex.text = "${bean.age}"

        val city = bean.city
        if (city.isEmpty()) {
            tv_location.hide()
        } else {
            tv_location.show()
            tv_location.text = city
        }

        sdv_header.loadImage(bean.headPic, 80f, 80f)
        tv_nickname.text = bean.nickname
        val wealthAddrss = GlobalUtils.getString(R.string.wealth_address)
        sdv_caifu_level.loadImage(String.format(wealthAddrss, bean.userLevel), 40f, 20f)
        sdv_royal_level.loadImage(bean.royalPic, 55f, 16f)
    }

    /**
     * 显示当前倒计时
     */
    private fun showTime(time: Long) {
        tv_time.text = "缘分来了(${time}s)"
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.TOP, 0)

        val params = dialog?.window?.attributes
        params?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; //这条就是控制点击背景的时候  如果被覆盖的view有点击事件那么就会直接触发(dialog消失并且触发背景下面view的点击事件)
        dialog?.window?.attributes = params
    }

    override fun setWindowAnimations() {
        dialog?.window?.setWindowAnimations(com.julun.huanque.common.R.style.dialog_top_top_style)
    }

    override fun onResume() {
        super.onResume()
        initViewModel()
    }
}