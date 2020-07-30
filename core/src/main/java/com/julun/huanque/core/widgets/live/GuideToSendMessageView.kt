package com.julun.huanque.core.widgets.live

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.julun.huanque.core.R


/**
 *
 *@author zhangzhen
 *@data 2019/4/16
 * 引导发送消息提示
 *
 **/


class GuideToSendMessageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs) {
//    private val logger = ULog.getLogger("GuideToSendMessageView")

//    private var playerViewModel: PlayerViewModel? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_guide_to_send_message, this)
    }

//    override fun onFinishInflate() {
//        super.onFinishInflate()
//        initViews()
//    }
//    fun initViews() {
//        initViewModel()
//    }

//    private fun initViewModel() {
//        val activity = context as? PlayerActivity
//        activity?.let { act ->
//            playerViewModel = ViewModelProviders.of(act).get(PlayerViewModel::class.java)
//        }
//    }
}