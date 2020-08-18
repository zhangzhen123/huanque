package com.julun.huanque.common.ui.image

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.R
import com.julun.huanque.common.bean.events.ImagePositionEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.luck.picture.lib.ImagePreviewActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.textColor
import java.io.Serializable

/**
 *
 *@author zhangzhen
 *@data 2019/2/13
 *
 * 通用的浏览图片 新增右侧操作用于远程图片浏览 该类依赖于 picture_library
 *
 **/

class ImageActivity : ImagePreviewActivity() {

    companion object {
        /**
         * [activity]起始页面
         * [position]图片所在位置
         * [medias]所有的图片列表
         * 其他
         * [userId]用户id
         *[operate]要做的操作
         */
        fun start(
            activity: Activity,
            position: Int,
            medias: List<String>,
            userId: Long? = null,
            operate: String? = null,
            from: String? = null
        ) {
            val intent = Intent(activity, ImageActivity::class.java)
            val list = medias.map {
                val m = LocalMedia()
                m.path = it
                m
            }
            intent.putExtra(PictureConfig.EXTRA_PREVIEW_SELECT_LIST, list as Serializable)
            intent.putExtra(PictureConfig.EXTRA_POSITION, position)
            intent.putExtra(IntentParamKey.ID.name, userId)
            intent.putExtra(IntentParamKey.OPERATE.name, operate)
            intent.putExtra(IntentParamKey.SOURCE.name, from)
            activity.startActivity(intent)
            activity.overridePendingTransition(com.luck.picture.lib.R.anim.a5, 0)
        }
    }

    //    private var report: DynamicReportDialogFragment? = null
//    private var bottomChooseFragment: BottomChooseFragment? = null//主播
//    private var postId: Long? = null
//    private var mProgramId: Long? = null
    private var mUserId: Long = 0L
    private var operate: String = ""
    private var from: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
//        postId = intent.getLongExtra(POST_ID, 0)
        mUserId = intent.getLongExtra(IntentParamKey.ID.name, 0)
        operate = intent.getStringExtra(IntentParamKey.OPERATE.name) ?: ""
        from = intent.getStringExtra(IntentParamKey.SOURCE.name) ?: ""
        PictureSelector.create(this).themeStyle(R.style.picture_me_style_multi)
        super.onCreate(savedInstanceState)
        //非主播可以点击

        val rightAction = findViewById<TextView>(R.id.right_action)
        val leftBack = findViewById<View>(R.id.left_back)
        val title = findViewById<TextView>(R.id.picture_title)
        when (operate) {
            ImageActivityOperate.REPORT -> {
                rightAction.text = "举报"
                rightAction.textColor = Color.parseColor("#FFCC00")
                rightAction.show()
                rightAction.onClickNew {
                    //
                    logger("举报他")
                    if (mUserId != 0L) {
                        val extra = Bundle()
                        extra.putLong(ParamConstant.TARGET_USER_ID, mUserId)
                        ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra)
                            .navigation()
                    }

                }
            }

        }
        when (from) {
            ImageActivityFrom.CHAT -> {
                leftBack.hide()
                title.hide()
            }
            ImageActivityFrom.HOME -> {

            }

            ImageActivityFrom.RN -> {

            }

        }

        //在外部处理压缩地址 只针对远程图片使用oss的压缩后缀
        images.forEach {
            val isHttp = PictureMimeType.isHttp(it.path)
            if (isHttp) {
                it.compressPath = it.path /* todo+ BusiConstant.COMPRESS_SUFFIX_SQUARE*/
            }
        }

    }

    override fun finish() {
        val currentPosition=images.indexOf(currentMedia)
        if(currentPosition!=-1){
            EventBus.getDefault().post(ImagePositionEvent(currentPosition))
        }
        super.finish()
    }

    override fun onDestroy() {

        super.onDestroy()
    }
}