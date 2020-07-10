package com.julun.huanque.ui.main

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.SessionUtils
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_mine.*

/**
 *@创建者   dong
 *@创建时间 2020/6/29 19:21
 *@描述 我的
 */
class MineFragment : BaseFragment() {

    companion object {
        fun newInstance() = MineFragment()
    }

    override fun getLayoutId() = R.layout.fragment_mine

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

    }

    override fun initEvents(rootView: View) {
        test_rn.onClickNew {
            RNPageActivity.start(requireActivity(), RnConstant.MINE_HOMEPAGE);
        }
        tv_clear_session.onClickNew {
            SessionUtils.clearSession()
        }
        test_real.onClickNew {
            //实名认证界面
            ARouter.getInstance().build(ARouterConstant.REALNAME_MAIN_ACTIVITY).navigation()
        }
    }
}