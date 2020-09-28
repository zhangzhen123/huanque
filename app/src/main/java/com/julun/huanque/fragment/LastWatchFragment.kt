package com.julun.huanque.fragment

import android.os.Bundle
import android.view.Gravity
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.UserEnterRoomRespBase
import com.julun.huanque.common.constant.PlayerFrom
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.core.ui.live.PlayerActivity
import kotlinx.android.synthetic.main.fragment_last_watch.*

/**
 *@创建者   dong
 *@创建时间 2020/9/25 10:40
 *@描述 上次观看
 */
class LastWatchFragment : BaseDialogFragment() {
    companion object {
        private const val Last_Watch = "Last_Watch"
        fun newInstance(data: UserEnterRoomRespBase): LastWatchFragment {
            val fragment = LastWatchFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(Last_Watch, data)
            }
            return fragment
        }

    }

    private var mData: UserEnterRoomRespBase? = null

    override fun getLayoutId() = R.layout.fragment_last_watch

    override fun initViews() {
        mData = arguments?.getSerializable(Last_Watch) as? UserEnterRoomRespBase
        tv_nickname.text = mData?.programName
        svv.showCover(mData?.prePic ?: "")
        val playinfo = mData?.playInfo
        if (mData?.isLiving == true && playinfo != null) {
            //直播中
            iv_play.hide()
            view_shader.hide()
            svv.play(GlobalUtils.getPlayUrl(playinfo))
        }
        initEvents()
    }

    private fun initEvents() {
        iv_close.onClickNew {
            dismiss()
        }

        con.onClickNew {
            //跳转直播间
            PlayerActivity.start(requireActivity(), mData?.programId ?: return@onClickNew, mData?.prePic ?: "", PlayerFrom.GuessYouLike)
            dismiss()
        }
    }


    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.CENTER, 35, 333)
    }

    override fun order(): Int {
        return 900
    }
}