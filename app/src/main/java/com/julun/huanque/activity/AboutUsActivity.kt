package com.julun.huanque.activity

import android.R.attr.phoneNumber
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.julun.huanque.BuildConfig
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.GlobalUtils
import kotlinx.android.synthetic.main.act_about_us.*


/**
 *@创建者   dong
 *@创建时间 2020/8/12 15:31
 *@描述 关于我们
 */
class AboutUsActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_about_us

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_view.textTitle.text = "关于我们"
        tv_version.text = "版本号：${BuildConfig.VERSION_NAME}"
    }

    override fun initEvents(rootView: View) {
        header_view.imageViewBack.onClickNew {
            finish()
        }
        view_suggest.onClickNew {
            //意见反馈与建议,复制微信
            GlobalUtils.copyToSharePlate(this, "逗你玩")
        }
        view_consumer.onClickNew {
            //客服热线，跳转拨打电话页面
            //跳转到拨号界面，同时传递电话号码
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:400-400-400"))
            startActivity(dialIntent)
        }

        view_business_license.onClickNew {
            //营业执照
        }
    }
}