package com.julun.huanque.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.viewmodel.MainViewModel

import com.julun.rnlib.RNPageActivity
import com.tencent.bugly.crashreport.CrashReport

import kotlinx.android.synthetic.main.main_fragment.*

class MakeFriendsFragment : BaseFragment() {

    companion object {
        fun newInstance() = MakeFriendsFragment()
    }

    override fun getLayoutId(): Int {
        return R.layout.main_fragment
    }

    private val viewModel: MainViewModel by activityViewModels()

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        viewModel.userInfo.observe(viewLifecycleOwner, Observer {
            println("我是用户信息：=$it")
        })
        viewModel.userLevelInfo.observe(viewLifecycleOwner, Observer {
            println("我是用户等级信息：=$it")
        })
        viewModel.loadState.observe(viewLifecycleOwner, Observer {
            println("我请求状态信息：=$it")
        })
        test_net.onClickNew {
            viewModel.getInfo()
        }
        test_rxjava.onClickNew {
//            viewModel.getUserLevelByRx()
            viewModel.getUserLevelByRx2()

        }

        test_rn.onClickNew {
            logger.info("测试rn跳转")
            RNPageActivity.start(requireActivity(), "HomePage");
            viewModel.getInfo()
        }

        message.onClickNew {
            CrashReport.testJavaCrash();
//            "kjhd".toInt()
        }


    }
}