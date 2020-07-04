package com.julun.huanque.ui.test

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.R
import com.julun.huanque.activity.LoginActivity
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.suger.onClickNew
import com.julun.rnlib.RNPageActivity
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.android.synthetic.main.activity_test.*
import org.jetbrains.anko.startActivity

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 19:32
 *
 *@Description: 测试页面供 测试各种功能使用
 *
 */
@Route(path = ARouterConstant.TEST_ACTIVITY)
class TestActivity : BaseActivity() {

    private val viewModel: TestViewModel by viewModels()

    override fun getLayoutId() = R.layout.activity_test

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        viewModel.userInfo.observe(this, Observer {
            println("我是用户信息：=$it")
        })
        viewModel.userLevelInfo.observe(this, Observer {
            println("我是用户等级信息：=$it")
        })
        viewModel.loadState.observe(this, Observer {
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
            RNPageActivity.start(this, "HomePage");
            viewModel.getInfo()
        }

        crash.onClickNew {
            CrashReport.testJavaCrash();
//            "kjhd".toInt()
        }
        login.onClickNew {
            startActivity<LoginActivity>()
            finish()
        }


    }
}