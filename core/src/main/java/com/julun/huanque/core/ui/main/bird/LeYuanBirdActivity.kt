package com.julun.huanque.core.ui.main.bird

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.core.R

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/11 20:53
 *
 *@Description: LeyuanBirdActivity
 * 欢鹊乐园独立页
 *
 */
class LeYuanBirdActivity : BaseActivity() {
    private val leYuanFragment: LeYuanFragment by lazy { LeYuanFragment.newInstance() }
    override fun getLayoutId() = R.layout.activity_le_yuan

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        StatusBarUtil.setTransparent(this)
        logger.info("WelcomeActivity initViews")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, leYuanFragment).commit()
    }


}