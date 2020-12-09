package com.julun.huanque.core.widgets

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.julun.huanque.common.bean.beans.HomePageProgram
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils

/**
 *@创建者   dong
 *@创建时间 2020/12/9 16:39
 *@描述 需要添加播放的ConstraintLayout
 */
class PlayerConstraintLayout(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    //播放数据
    private var mHomeProgramInfo: HomePageProgram? = null

    fun setmHomeProgramInfo(info: HomePageProgram) {
        mHomeProgramInfo = info
        showPlayerInfo()
    }


    /**
     * 显示播放数据
     */
    private fun showPlayerInfo() {
        mHomeProgramInfo?.let { item ->
            singleVideoView?.showCover(StringHelper.getOssImgUrl(item.programCover), false)
            if (singleVideoView?.hasPlayer() != true) {
                singleVideoView?.initPlayer()
            }
            if (item.living == BusiConstant.True) {
                val playInfo = item.playInfo
                if (playInfo != null) {
                    singleVideoView?.play(GlobalUtils.getPlayUrl(playInfo))
                }
            }
        }
    }

    var singleVideoView: SingleVideoView? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        singleVideoView = singleVideoView ?: SingleVideoView(context)
        if (singleVideoView?.parent == null) {
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            this.addView(singleVideoView, 0, params)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                singleVideoView?.outlineProvider = SurfaceVideoViewOutlineProvider(dp2pxf(6));
                singleVideoView?.clipToOutline = true;
            }
        }
        singleVideoView?.show()
        showPlayerInfo()

    }
}