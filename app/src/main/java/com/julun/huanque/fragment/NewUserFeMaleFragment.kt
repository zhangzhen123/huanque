package com.julun.huanque.fragment

import android.view.Gravity
import android.view.KeyEvent
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.*
import com.julun.huanque.core.ui.video.VideoActivity
import com.julun.huanque.viewmodel.MainViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_newuser_female.*
import kotlinx.android.synthetic.main.fragment_newuser_female.iv_get
import kotlinx.android.synthetic.main.fragment_newuser_female.view_bottom
import kotlinx.android.synthetic.main.fragment_newuser_male.*

/**
 *@创建者   dong
 *@创建时间 2020/9/19 10:42
 *@描述 新手礼包
 */
class NewUserFeMaleFragment : BaseDialogFragment() {

    private val mMainViewModel: MainViewModel by activityViewModels()

    private var playerUrl = ""

    override fun getLayoutId() = R.layout.fragment_newuser_female

    override fun initViews() {
        iv_get.onClickNew {
            //跳转聊主学院
            RNPageActivity.start(requireActivity(), RnConstant.CHAT_COLLEGE_PAGE)
            dismiss()
        }
        iv_close.onClickNew {
            ToastUtils.show("您可以在聊主学院中学到更多赚钱小窍门")
            dismiss()
        }
        sdv.onClickNew {
            //开始播放
            view_shader.hide()
//            svv.play(StringHelper.getOssVideoUrl(playerUrl))
            VideoActivity.start(requireActivity(), StringHelper.getOssVideoUrl(playerUrl))
            dismiss()
        }
        initViewModel()

        val params = view_bottom.layoutParams
        params.width = ScreenUtils.getScreenWidth() * 305 / 375
        view_bottom.layoutParams = params
    }

    private fun initViewModel() {
        mMainViewModel.newUserBean.observe(this, Observer {
            if (it != null) {
//                svv.showCover(it.videoCover)
                sdv.loadImage(it.videoCover, 265f, 130f)
                playerUrl = it.videoUrl
            }
        })
    }

    override fun order() = 300

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.CENTER, 0)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
    }
}