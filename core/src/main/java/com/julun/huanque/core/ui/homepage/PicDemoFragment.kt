package com.julun.huanque.core.ui.homepage

import android.view.Gravity
import android.view.ViewGroup
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.loadImageLocal
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.frag_pic_demo.*

/**
 *@创建者   dong
 *@创建时间 2021/1/8 9:18
 *@描述 图片视图Fragment
 */
class PicDemoFragment : BaseDialogFragment() {
    override fun getLayoutId() = R.layout.frag_pic_demo

    override fun initViews() {
        val demoResource = if (SessionUtils.getSex() == Sex.MALE) {
            //男性
            R.mipmap.icon_demo_male
        } else {
            //女性
            R.mipmap.icon_demo_female
        }
        sdv_pic.loadImageLocal(demoResource)
        tv_confirm.onClickNew {
            dismiss()
        }
    }

    override fun configDialog() {
        setDialogSize(Gravity.CENTER, 29, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}