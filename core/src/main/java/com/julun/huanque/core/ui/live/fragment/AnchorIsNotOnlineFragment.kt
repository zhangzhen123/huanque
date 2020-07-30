package com.julun.huanque.core.ui.live.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.AnchorNoLiveViewModel
import kotlinx.android.synthetic.main.fragment_anchorisnotonline.*

/**
 * 主播不在线时的遮罩视图
 * Created by djp on 2016/11/29.
 */
class AnchorIsNotOnlineFragment : BaseFragment() {
    private val anchorNoLiveViewModel: AnchorNoLiveViewModel by activityViewModels()
    private val mPlayerViewModel: PlayerViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.fragment_anchorisnotonline


    companion object {
        fun newInstance() = AnchorIsNotOnlineFragment()
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mPlayerViewModel?.chatModeState?.observe(this, Observer {
            if (it == true) {
                con.hide()
            } else {
                if (!mPlayerViewModel.isLiving) {
                    con.show()
                }
            }
        })
        anchorNoLiveViewModel.baseData.observe(this, Observer {
            it?.let {
                if (it.lastShowTimeDiffText.isNotEmpty()) {
                    tv_provious.show()
                    tv_provious.text = "上次直播：${it.lastShowTimeDiffText}"
                } else {
                    tv_provious.hide()
                }

            }
        })

        tv_text.text = if (mPlayerViewModel.isThemeRoom) {
            resources.getString(R.string.theme_room_not_playing)
        } else {
            resources.getString(R.string.anchor_not_playing)
        }
    }


}