package com.julun.huanque.activity

import android.os.Bundle
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import kotlinx.android.synthetic.main.act_select_sex.*


/**
 *@创建者   dong
 *@创建时间 2020/9/17 21:20
 *@描述 选择性别页面
 */
class SelectSexActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_select_sex

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        ivback.onClickNew {
            finish()
        }
    }
}