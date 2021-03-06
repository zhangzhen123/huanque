package com.julun.huanque.core.ui.live.dialog

import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.DialogOrderNumber
import com.julun.huanque.common.constant.StatisticCode
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.reportClick
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import kotlinx.android.synthetic.main.dialog_guide_follow.*
import org.jetbrains.anko.imageResource

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2019/12/12 11:07
 *
 *@Description: GuideFollowFragment
 *
 */
class GuideFollowFragment : BaseDialogFragment() {
    //与playerActivity通信
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private val huanQueViewModel = HuanViewModelManager.huanQueViewModel

    companion object {
        fun newInstance(): GuideFollowFragment {
            return GuideFollowFragment()
        }
    }

    override fun getLayoutId(): Int = R.layout.dialog_guide_follow
    override fun order() = DialogOrderNumber.LOCAL_DIALOG


    override fun configDialog() {
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
    override fun initViews() {

        initData()
        follow_anchor.onClickNew {
            playerViewModel.subscribeSource = "底部引导"
            reportClick(eventCode = StatisticCode.Follow + StatisticCode.LiveRoom)
            huanQueViewModel.follow(playerViewModel.programId)
            dismiss()
        }
    }

    private fun initData() {
        val baseData = playerViewModel?.baseData?.value ?: return
        headPicImage.loadImage(baseData.headPic)
        nameText.text = baseData.programName
        anchor_level_icon.imageResource = ImageHelper.getAnchorLevelResId(baseData.anchorLevel)

    }

    override fun reCoverView() {
        super.reCoverView()
        initData()
    }


}