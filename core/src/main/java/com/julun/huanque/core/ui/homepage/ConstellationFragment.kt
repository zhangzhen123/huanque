package com.julun.huanque.core.ui.homepage

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.loadImageInPx
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.frag_constellation.*

/**
 *@创建者   dong
 *@创建时间 2020/12/29 14:26
 *@描述 星座弹窗
 */
class ConstellationFragment : BaseBottomSheetFragment() {
    companion object {
        /**
         * 更新生日页面使用
         */
        fun newInstance(type: String): ConstellationFragment {
            val fragment = ConstellationFragment()
            val bundle = Bundle().apply {
                putString(ParamConstant.TYPE, type)
            }
            fragment.arguments = bundle
            return fragment
        }

    }

    private val mPicWidth = ScreenUtils.getScreenWidth()

    private val mPicHeight = mPicWidth * 378 / 375

    override fun getLayoutId() = R.layout.frag_constellation

    override fun initViews() {
        val type = arguments?.getString(ParamConstant.TYPE) ?: ""
        if (type.isNotEmpty()) {
            sdv_constell.loadImageInPx("http://cdn.ihuanque.com/config/app/constellation_${type}.png", mPicWidth, mPicHeight)
        }
    }

    override fun getHeight(): Int {
        return dp2px(46) + mPicHeight
    }

    override fun onStart() {
        super.onStart()
//        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val win = dialog?.window ?: return
        win.setWindowAnimations(R.style.dialog_bottom_bottom_style)
        val parent = view?.parent
        if (parent is View) {
            parent.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}