package com.julun.huanque.core.ui.live.fragment

import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.RecommendInfo
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.AnchorNoLiveViewModel
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.fragment_recommend.*

/**
 *@创建者   dong
 *@创建时间 2020/10/29 19:10
 *@描述 推荐弹窗
 */
class RecommendFragment : BaseDialogFragment() {

    private val mAnchorNoLiveViewModel: AnchorNoLiveViewModel by activityViewModels()
    private val mPlayerViewModel: PlayerViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_recommend

    override fun initViews() {
        iv_close.onClickNew {
            dismiss()
        }
        tv_go.onClickNew {
            val bean = mAnchorNoLiveViewModel.recommendProgram.value ?: return@onClickNew
            mPlayerViewModel.checkoutRoom.value = bean.programId
        }
        tv_hot.setTFDinCdc2()

    }

    override fun onStart() {
        super.onStart()
        mAnchorNoLiveViewModel.countDownNumber.value = null
        setDialogSize(Gravity.CENTER, 30, ViewGroup.LayoutParams.WRAP_CONTENT)
        initViewModel()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mAnchorNoLiveViewModel.recommendProgram.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }
        })
        mAnchorNoLiveViewModel.countDownNumber.observe(this, Observer {
            if (it != null) {
                tv_count.text = "${it}秒后将进入以下直播间"
                if (it == 0) {
                    tv_go.performClick()
                }
            }
        })
        mAnchorNoLiveViewModel.showRecommendProgram.observe(this, Observer {
            if (it != true) {
                mAnchorNoLiveViewModel.stopCountDown()
                dismiss()
            }
        })
    }

    /**
     * 根据数据显示视图
     */
    private fun showViewByData(info: RecommendInfo) {
        mAnchorNoLiveViewModel.startCountDown()
        tv_city.text = info.city
        tv_nickname.text = info.nickname
        tv_hot.text = "${info.heatValue}"
        val border = ScreenUtils.getScreenWidth(requireContext()) - dp2px(120f)
        sdv_cover.loadImageInPx(StringHelper.getOssImgUrl(info.coverPic), border, border)
    }

}