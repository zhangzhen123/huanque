package com.julun.huanque.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseVMDialogFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.UserCardShareInfo
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImageNoResize
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.ui.share.UserCardShareViewModel
import kotlinx.android.synthetic.main.dialog_user_card.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2021/2/3 13:40
 *
 *@Description: 用户卡片弹窗
 *
 * 此弹窗不复用
 *
 */
class UserCardDialogFragment : BaseVMDialogFragment<UserCardShareViewModel>() {

    companion object {
        fun newInstance(userId: Long): UserCardDialogFragment {
            val args = Bundle()
            args.putLong(IntentParamKey.USER_ID.name, userId)
            val fragment = UserCardDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId(): Int = R.layout.dialog_user_card


    override fun configDialog() {
        setDialogSize(gravity = Gravity.CENTER, marginWidth = 20, height = ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun initViews() {
        val userId = arguments?.getLong(IntentParamKey.USER_ID.name) ?: return
        initViewModel()

        okText.onClickNew {
            val bundle = Bundle().apply {
                putLong(ParamConstant.UserId, userId)
            }
            ARouter.getInstance().build(ARouterConstant.HOME_PAGE_ACTIVITY).with(bundle).navigation()
            dismiss()
        }
        cancelText.onClickNew {
            dismiss()
        }


        mViewModel.queryCardInfo(userId)

    }

    override fun order(): Int {
        return DialogOrderNumber.USER_CARD_SHARE_FRAGMENT
    }

    private fun initViewModel() {
        mViewModel.userCardInfo.observe(this, Observer {
            if (it.isSuccess()) {
                loadDataSuccess(it.requireT())
            }
        })
    }

    private fun loadDataSuccess(userInfo: UserCardShareInfo) {
        card_img.loadImageNoResize(userInfo.headPic)
        when {
            userInfo.realName -> {
                iv_auth.show()
                iv_auth.setImageResource(R.mipmap.icon_real_name_home_page)
            }
            userInfo.headRealPeople -> {
                iv_auth.show()
                iv_auth.setImageResource(R.mipmap.icon_real_people_home_page)
            }
            else -> {
                iv_auth.hide()
            }
        }
        tv_user_name.text = userInfo.nickname

        if (userInfo.area.isNotEmpty()) {
            tv_area.text = userInfo.area
        } else {
            val starList = mutableListOf<String>("金星", "木星", "水星", "火星", "土星")
            val currentStar = starList.random()
            tv_area.text = currentStar
        }

        val strAgeSex = if (userInfo.sexType == Sex.MALE) {
            okText.text = "立即和他互动"
            if (userInfo.age == 0) {
                " / 男"
            } else {
                " / ${userInfo.age}岁 男"
            }

        } else {
            okText.text = "立即和她互动"
            if (userInfo.age == 0) {
                " / 女"
            } else {
                " / ${userInfo.age}岁 女"
            }
        }
        tv_age.text = strAgeSex

    }

    override fun setWindowAnimations() {
        dialog?.window?.setWindowAnimations(com.julun.huanque.common.R.style.dialog_center_open_ani)
    }

    override fun showLoadState(state: NetState) {

    }

}