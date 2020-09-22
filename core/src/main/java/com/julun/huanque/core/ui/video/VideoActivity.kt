package com.julun.huanque.core.ui.video

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_video.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/22 20:21
 *
 *@Description: 通用的播放视频页面
 *
 */
class VideoActivity : BaseActivity() {

    companion object {
        /**
         * [activity]起始页面
         * [media]视频地址
         * 其他
         *[operate]要做的操作
         */
        fun start(
            activity: Activity,
            media: String,
            operate: String? = null,
            from: String? = null
        ) {
            val intent = Intent(activity, VideoActivity::class.java)
            intent.putExtra(IntentParamKey.URL.name, media)
            intent.putExtra(IntentParamKey.OPERATE.name, operate)
            intent.putExtra(IntentParamKey.SOURCE.name, from)
            activity.startActivity(intent)
        }
    }

    //    private var report: DynamicReportDialogFragment? = null
//    private var bottomChooseFragment: BottomChooseFragment? = null//主播
//    private var postId: Long? = null
//    private var mProgramId: Long? = null
    private var mUserId: Long = 0L
    private var operate: String = ""
    private var url: String = ""
    private var from: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
//        postId = intent.getLongExtra(POST_ID, 0)
//        mUserId = intent.getLongExtra(IntentParamKey.ID.name, 0)
//        operate = intent.getStringExtra(IntentParamKey.OPERATE.name) ?: ""
//        from = intent.getStringExtra(IntentParamKey.SOURCE.name) ?: ""
        url = intent.getStringExtra(IntentParamKey.URL.name) ?: ""
//        PictureSelector.create(this).themeStyle(R.style.picture_me_style_multi)
        super.onCreate(savedInstanceState)


    }


    override fun getLayoutId(): Int {
        return R.layout.activity_video
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        ivBack.onClickNew {
            finish()
        }
        video_view.play(url)
    }
}