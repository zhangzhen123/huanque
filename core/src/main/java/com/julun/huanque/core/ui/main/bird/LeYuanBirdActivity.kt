package com.julun.huanque.core.ui.main.bird

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.events.HideBirdEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.core.R
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

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
@Route(path = ARouterConstant.LEYUAN_BIRD_ACTIVITY)
class LeYuanBirdActivity : BaseActivity() {
    private val leYuanFragment: LeYuanFragment by lazy { LeYuanFragment.newInstance() }
    override fun getLayoutId() = R.layout.activity_le_yuan_bird

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        StatusBarUtil.setTransparent(this)
        logger.info("WelcomeActivity initViews")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, leYuanFragment).commit()
    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun close(event: HideBirdEvent) {
        finish()
    }
}