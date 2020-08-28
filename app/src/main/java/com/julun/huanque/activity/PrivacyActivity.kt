package com.julun.huanque.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.PrivacyEnums
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.suger.onAdapterClickNew
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.NotificationUtils
import com.julun.huanque.common.utils.device.PhoneUtils
import kotlinx.android.synthetic.main.activity_privacy.*

/**
 * 隐私设置页
 * @author WanZhiYuan
 * @since 4.24
 * @create 2019/12/25
 * @detail 适配了小米、华为、魅族、索尼四种手机的权限设置页，其他就跳转到系统设置页
 */
class PrivacyActivity : BaseActivity() {

    //数据源
    private val list = arrayListOf(
        PrivacyEnums.PRIVACY_ADDRESS, PrivacyEnums.PRIVACY_NOTIFICATION,
            PrivacyEnums.PRIVATE_CAMERA, PrivacyEnums.PRIVATE_FLIE, PrivacyEnums.PRIVACY_MIKE)

    override fun getLayoutId(): Int = R.layout.activity_privacy

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        rvList?.adapter = adapter
        rvList?.layoutManager = LinearLayoutManager(this)
    }

    override fun initEvents(rootView: View) {
        ivBack.onClickNew {
            finish()
        }
        adapter.onAdapterClickNew { adapter, view, position ->
            val item = adapter?.getItem(position)
            item ?: return@onAdapterClickNew
            if (item is String) {
                when (item) {
                    PrivacyEnums.PRIVACY_NOTIFICATION -> {
                        //通知跳转和权限跳转不一样
                        val intent = NotificationUtils.gotoNotificationSetting(this@PrivacyActivity)
                        if (!TextUtils.isEmpty(intent.action) && ForceUtils.activityMatch(intent)) {
                            try {
                                //配置action成功才跳转
                                startActivity(intent)
                            } catch (e: Exception) {
                                reportCrash("权限设置入口跳转通知页失败", e)
                            }
                        }
                    }
                    else -> {
                        PhoneUtils.getPermissionSetting(packageName)?.let {
                            if (ForceUtils.activityMatch(it)) {
                                try {
                                    startActivity(it)
                                } catch (e: Exception) {
                                    reportCrash("跳转权限或者默认设置页失败", e)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //每次返回到这个页面都重载一次列表，保证权限获取都是正确的
        adapter.setList(list)
    }

    private val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_privacy_list) {
        override fun convert(helper: BaseViewHolder, item: String) {
            when (item) {
                PrivacyEnums.PRIVACY_ADDRESS -> {
                    helper.setText(R.id.tvPriName, "访问位置")
                    if (ContextCompat.checkSelfPermission(application, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        helper.setText(R.id.tvPriState, "去设置")
                    } else {
                        helper.setText(R.id.tvPriState, "已开启")
                    }
                }
                PrivacyEnums.PRIVATE_CAMERA -> {
                    helper.setText(R.id.tvPriName, "相机权限")
                    if (ContextCompat.checkSelfPermission(application, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        //没有外部存储读取权限,添加该权限
                        helper.setText(R.id.tvPriState, "去设置")
                    } else {
                        helper.setText(R.id.tvPriState, "已开启")
                    }
                }
                PrivacyEnums.PRIVATE_FLIE -> {
                    helper.setText(R.id.tvPriName, "文件存储和访问")
                    if (ContextCompat.checkSelfPermission(application, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        //没有外部存储读取权限,添加该权限
                        helper.setText(R.id.tvPriState, "去设置")
                    } else {
                        helper.setText(R.id.tvPriState, "已开启")
                    }
                }
                PrivacyEnums.PRIVACY_MIKE -> {
                    helper.setText(R.id.tvPriName, "麦克风权限")
                    if (ContextCompat.checkSelfPermission(application, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        //没有外部存储读取权限,添加该权限
                        helper.setText(R.id.tvPriState, "去设置")
                    } else {
                        helper.setText(R.id.tvPriState, "已开启")
                    }
                }
                else -> {
                    //其他
                    helper.setText(R.id.tvPriName, "其他")
                }
            }
            if (item == PrivacyEnums.PRIVACY_NOTIFICATION) {
                //通知和其他权限不一样，单独拎出来判断
                helper.setText(R.id.tvPriName, "通知权限")
                if (NotificationUtils.areNotificationsEnabled(this@PrivacyActivity)) {
                    helper.setText(R.id.tvPriState, "已开启")
                } else {
                    helper.setText(R.id.tvPriState, "去设置")
                }
            }
        }
    }
}