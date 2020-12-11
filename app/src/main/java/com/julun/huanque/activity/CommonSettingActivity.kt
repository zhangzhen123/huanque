package com.julun.huanque.activity

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.RomUtils
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.permission.PermissionUtils
import com.julun.huanque.viewmodel.CommonSettingViewModel
import kotlinx.android.synthetic.main.act_player_setting.*

/**
 *@创建者   dong
 *@创建时间 2020/9/24 9:51
 *@描述 直播设置页面
 */
@Route(path = ARouterConstant.PLAYER_SETTING_ACTIVITY)
class CommonSettingActivity : BaseActivity() {

    private val mCommonSettingViewModel: CommonSettingViewModel by viewModels()

    //用户点击去设置跳转到权限设置页面
    private var mUserAction = false

    companion object {
        private const val PERMISSIONALERT_WINDOW_CODE = 123
    }

    override fun getLayoutId() = R.layout.act_player_setting

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        header_page.textTitle.text = "通用设置"
        showDefaultView()

        mCommonSettingViewModel.getHideLocationState()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mCommonSettingViewModel.hideLocationState.observe(this, Observer {
            iv_hide_location.isSelected = it == BusiConstant.True
        })
    }


    /**
     * 显示默认布局，请求权限回来刷新布局
     */
    private fun showDefaultView() {
        val permissionEnable = PermissionUtils.checkFloatPermission(this)
        if (mUserAction) {
            mUserAction = false
            if (permissionEnable) {
                iv_floating.isSelected = true
                SPUtils.commitBoolean(SPParamKey.Player_Close_Floating_Show, true)
            } else {
                iv_floating.isSelected = false
            }
        } else {
            val floatingShow = SPUtils.getBoolean(SPParamKey.Player_Close_Floating_Show, true)
            iv_floating.isSelected = permissionEnable && floatingShow
        }
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }

        iv_floating.onClickNew {
            if (iv_floating.isSelected) {
                iv_floating.isSelected = false
                SPUtils.commitBoolean(SPParamKey.Player_Close_Floating_Show, false)
            } else {
                //检查权限
                val enable = PermissionUtils.checkFloatPermission(this)
                if (!enable) {
                    //出现弹窗
                    MyAlertDialog(this).showAlertWithOKAndCancel(
                        "悬浮窗权限被禁用，请到设置中授予欢鹊悬浮窗权限",
                        MyAlertDialog.MyDialogCallback(onRight = {
                            mUserAction = true
                            try {
                                if (RomUtils.isOppo() && Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
                                    //oppo 5.1系统  跳转安全中心
                                    val intent = Intent(Intent.ACTION_MAIN)
                                    val componentName =
                                        ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity")
                                    intent.component = componentName
                                    startActivity(intent)
                                } else {
                                    val intent = Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION")
                                    intent.data = Uri.parse("package:$packageName")
                                    startActivityForResult(intent, PERMISSIONALERT_WINDOW_CODE)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }), "设置提醒", "去设置"
                    )
                } else {
                    iv_floating.isSelected = true
                    SPUtils.commitBoolean(SPParamKey.Player_Close_Floating_Show, true)
                }

            }
        }
        iv_hide_location.onClickNew {
            val status = if (iv_hide_location.isSelected) {
                BusiConstant.False
            } else {
                BusiConstant.True
            }
            mCommonSettingViewModel.saveHideLocation(status)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIONALERT_WINDOW_CODE) {
            showDefaultView()
        }
    }
}