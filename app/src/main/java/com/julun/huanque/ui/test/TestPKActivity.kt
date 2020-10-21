package com.julun.huanque.ui.test

import android.os.Bundle
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import kotlinx.android.synthetic.main.act_test_pk.*


/**
 *@创建者   dong
 *@创建时间 2020/10/20 10:12
 *@描述 测试PK动画的Activity
 */
class TestPKActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_test_pk

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        pk_start_view.setLeftViewData("","扣税的计划分")
        pk_start_view.setRightViewData("","基督教快乐和")
        tv_start.onClickNew {
            //开始动画
            pk_start_view.startAnimation()
        }

        tv_win.onClickNew {
            //胜利动画
            pk_start_view.startWinAnimation()
        }
    }
}