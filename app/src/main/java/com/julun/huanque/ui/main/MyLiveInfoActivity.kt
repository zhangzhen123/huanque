package com.julun.huanque.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.react.bridge.Arguments
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.ui.homepage.EditInfoActivity
import com.julun.huanque.ui.safe.AccountAndSecurityActivity
import com.julun.huanque.viewmodel.MineViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.act_my_live_info.*
import org.jetbrains.anko.startActivity

/**
 *@创建者   dong
 *@创建时间 2021/2/4 9:15
 *@描述 我的直播页面
 */
class MyLiveInfoActivity : BaseActivity() {
    companion object {
        const val UserLevel = "UserLevel"
        const val AnchorLevel = "AnchorLevel"
        const val RoyalLevel = "CaiFuLevel"
        const val RoyalPic = "RoyalPic"
        fun newInstance(act: Activity, userLevel: Int, anchorLevel: Int, royalLevel: Int, royalPic: String) {
            val intent = Intent(act, MyLiveInfoActivity::class.java)
            val bundle = Bundle()
            bundle.putInt(UserLevel, userLevel)
            bundle.putInt(AnchorLevel, anchorLevel)
            bundle.putInt(RoyalLevel, royalLevel)
            bundle.putString(RoyalPic, royalPic)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtras(bundle)
                act.startActivity(intent)
            }

        }
    }

    private val mViewModel: MineViewModel by viewModels()
    override fun getLayoutId() = R.layout.act_my_live_info

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_page.textTitle.text = "我的直播"
        initViewModel()
        val userLevel = intent?.getIntExtra(UserLevel, 0) ?: 0
        val anchorLevel = intent?.getIntExtra(AnchorLevel, 0) ?: 0
        val royalLevel = intent?.getIntExtra(RoyalLevel, 0) ?: 0
        val royalPic = intent?.getStringExtra(RoyalPic) ?: ""

        if (userLevel > 0) {
            sdv_wealth.show()
            tv_wealth_privilege.hide()
            val wealthAddrss = GlobalUtils.getString(R.string.wealth_address)
            sdv_wealth.loadImage(String.format(wealthAddrss, userLevel), 55f, 16f)
        } else {
            sdv_wealth.hide()
            tv_wealth_privilege.show()
        }

        if (royalLevel > 0) {
            tv_royal_privilege.hide()
            sdv_royal_level.show()
            sdv_royal_level.loadImage(royalPic, 55f, 16f)
        } else {
            tv_royal_privilege.show()
            sdv_royal_level.hide()
        }

        if (anchorLevel > 0) {
            sdv_author_level.show()
            tv_author_privilege.hide()
            val wealthAddrss = GlobalUtils.getString(R.string.anchor_address)
//            sdv_author_level.loadImage(String.format(wealthAddrss, info.userBasic.anchorLevel), 55f, 16f)
            ImageUtils.loadImageWithHeight_2(sdv_author_level, String.format(wealthAddrss, anchorLevel), dp2px(16))

        } else {
            tv_author_privilege.show()
            sdv_author_level.hide()
        }
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew { finish() }
        con_caifu.onClickNew {
            RNPageActivity.start(this, RnConstant.WEALTH_LEVEL_PAGE)
        }
        con_royal.onClickNew {
            RNPageActivity.start(this, RnConstant.ROYAL_PAGE)
        }
        con_anchor_level.onClickNew {
            RNPageActivity.start(this, RnConstant.ANCHOR_LEVEL_PAGE)
        }
        con_be_anchor.onClickNew {
            mViewModel.checkToAnchor()
        }
        con_car.onClickNew {
            RNPageActivity.start(this, RnConstant.MY_CAR_PAGE)
        }
        con_bubble.onClickNew {
            RNPageActivity.start(this, RnConstant.CHAT_BUBBLE_PAGE)
        }
    }

    private fun initViewModel() {
        mViewModel.checkAuthorResult.observe(this, Observer {
            if (it.isSuccess()) {
                RNPageActivity.start(this, RnConstant.ANCHOR_CERT_PAGE)
            } else if (it.state == NetStateType.ERROR) {
                val error = it.error ?: return@Observer
                ToastUtils.show(error.busiMessage)
                when (error.busiCode) {
                    ErrorCodes.NOT_INFO_COMPLETE -> {
//                        RNPageActivity.start(requireActivity(), RnConstant.EDIT_MINE_HOMEPAGE)
                        startActivity<EditInfoActivity>()
                    }
                    ErrorCodes.NOT_BIND_WECHAT -> {
                        //账号与安全
                        val intent = Intent(this, AccountAndSecurityActivity::class.java)
                        if (ForceUtils.activityMatch(intent)) {
                            startActivity(intent)
                        }
                    }
                    ErrorCodes.NOT_REAL_NAME -> {
                        ARouter.getInstance().build(ARouterConstant.REAL_NAME_MAIN_ACTIVITY)
                            .navigation()
                    }
                }
            }
        })
    }

}