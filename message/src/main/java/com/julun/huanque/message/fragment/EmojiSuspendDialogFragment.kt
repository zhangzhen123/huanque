package com.julun.huanque.message.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.widgets.emotion.Emotion
import com.julun.huanque.message.R

/**
 *@创建者   dong
 *@创建时间 2020/7/16 19:52
 *@描述 emoji表情变大悬浮弹窗
 */
class EmojiSuspendDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(bean: Emotion, location: IntArray): EmojiSuspendDialogFragment {
            val fragment = EmojiSuspendDialogFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(ParamConstant.Emotion, bean)
                putIntArray(ParamConstant.LOCATION, location)
            }
            return fragment
        }

    }

    private var mLocation: IntArray? = null

    override fun getLayoutId() = R.layout.fragment_normal_emoji_suspend

    override fun needEnterAnimation() = false

    override fun initViews() {
        val bean = arguments?.getSerializable(ParamConstant.Emotion) as? Emotion
//        bean?.let {
//            iv_emoji.imageResource = it.drawableRes
//            tv_emoji.text = it.text
//        }

        mLocation = arguments?.getIntArray(ParamConstant.LOCATION)
    }

    override fun configDialog() {
    }
    @SuppressLint("RtlHardcoded")
    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        mLocation?.let { location ->
            if (location.size >= 4) {
                val window = dialog?.window ?: return
                val dX = location[0]
                val params = window.attributes
                params.gravity = Gravity.BOTTOM or Gravity.LEFT
                params.width = DensityHelper.dp2px(50)
                params.height = DensityHelper.dp2px(66)
                params.x = dX
                params.y = ScreenUtils.getScreenHeightHasVirtualKey() - ScreenUtils.getNavigationBarHeightIfRoom(requireActivity()) - location[1]
                window.attributes = params
            }
        }
    }
}