package com.julun.huanque.activity

import android.os.Bundle
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.permission.PermissionUtils
import kotlinx.android.synthetic.main.act_player_setting.*
import kotlinx.android.synthetic.main.alert_dialog.view.*

/**
 *@创建者   dong
 *@创建时间 2020/9/24 9:51
 *@描述 直播设置页面
 */
class PlayerSettingActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_player_setting

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_page.titleText.text = "直播设置"
        showDefaultView()
    }

    private fun showDefaultView() {
        val permissionEnabler = PermissionUtils.checkFloatPermission(this)
        val floatingShow = SPUtils.getBoolean(SPParamKey.Player_Close_Floating_Show, true)
        iv_floating.isSelected = permissionEnabler && floatingShow
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }

        iv_floating.onClickNew { }
    }
}