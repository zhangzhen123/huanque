package com.julun.huanque.message.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.message.R
import kotlinx.android.synthetic.main.act_pic_content.*

/**
 *@创建者   dong
 *@创建时间 2020/9/23 16:50
 *@描述 图片作为内容显示的activity
 */
@Route(path = ARouterConstant.PIC_CONTENT_ACTIVITY)
class PicContentActivity : BaseActivity() {
    companion object {
        fun newInstance(act: Activity, url: String,title : String) {
            val intent = Intent(act, PicContentActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.RULE_PIC_URL, url)
                intent.putExtra(ParamConstant.Title, title)
                act.startActivity(intent)
            }
        }
    }

    override fun getLayoutId() = R.layout.act_pic_content

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val url = intent?.getStringExtra(ParamConstant.RULE_PIC_URL) ?: ""
        ImageUtils.loadImageWithWidth(sdv, url, ScreenUtils.getScreenWidth())
        header_page.textTitle.text = intent?.getStringExtra(ParamConstant.Title) ?: ""
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew { finish() }
    }
}