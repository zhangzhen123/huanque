package com.julun.huanque.common.ui.image

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.julun.huanque.common.R
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.luck.picture.lib.ImagePreviewActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textColorResource
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
         *
         */
        fun start(activity: Activity, position: Int, medias: List<String>, userId: String? = null) {
            val intent = Intent(activity, ImageActivity::class.java)
            val list = medias.map {
                val m = LocalMedia()
                m.path = it
                m
            }
            intent.putExtra(PictureConfig.EXTRA_PREVIEW_SELECT_LIST, list as Serializable)
            intent.putExtra(PictureConfig.EXTRA_POSITION, position)
            intent.putExtra(IntentParamKey.ID.name, userId)
            activity.startActivity(intent)
            activity.overridePendingTransition(com.luck.picture.lib.R.anim.a5, 0)
        }
    }

//    private var report: DynamicReportDialogFragment? = null
//    private var bottomChooseFragment: BottomChooseFragment? = null//主播
//    private var postId: Long? = null
//    private var mProgramId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
//        postId = intent.getLongExtra(POST_ID, 0)
//        mProgramId = intent.getLongExtra(IntentParamKey.PROGRAM_ID.name, 0)
        PictureSelector.create(this).themeStyle(R.style.picture_me_style_multi)
        super.onCreate(savedInstanceState)
        //非主播可以点击

        val rightAction = findViewById<TextView>(R.id.right_action)
        rightAction.text = "举报"
        rightAction.textColorResource=R.color.black_333
        rightAction.show()
        rightAction.onClickNew {
            //
            logger("举报他")
        }
        //在外部处理压缩地址
        images.forEach {
            it.compressPath = it.path /* todo+ BusiConstant.COMPRESS_SUFFIX_SQUARE*/
        }

    }


    override fun onDestroy() {

        super.onDestroy()
    }
}