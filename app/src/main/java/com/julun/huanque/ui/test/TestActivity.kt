package com.julun.huanque.ui.test

import android.animation.FloatEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.util.Property
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.julun.huanque.R
import com.julun.huanque.activity.LoginActivity
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.bean.beans.TIBean
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.MD5Util
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.VideoUtils
import com.julun.huanque.common.widgets.draweetext.AnimatedRainbowSpan
import com.julun.huanque.core.ui.live.dialog.CardManagerDialogFragment
import com.julun.huanque.core.ui.record_voice.VoiceSignActivity
import com.julun.huanque.message.activity.PrivateConversationActivity
import com.julun.huanque.message.fragment.ChatSendGiftFragment
import com.julun.huanque.support.LoginManager
import com.julun.jpushlib.TagAliasOperatorHelper
import com.julun.rnlib.RNPageActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.android.synthetic.main.activity_test.*
import org.jetbrains.anko.startActivity
import java.io.File
import java.math.BigInteger
import java.util.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 19:32
 *
 *@Description: 测试页面供 测试各种功能使用
 *
 */
@Route(path = ARouterConstant.TEST_ACTIVITY)
class TestActivity : BaseActivity() {

    private val viewModel: TestViewModel by viewModels()

    override fun getLayoutId() = R.layout.activity_test

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        viewModel.userInfo.observe(this, Observer {
            println("我是用户信息：=$it")
            ret_resp.text = "语音签名：${it.points.getOrNull(0)?.voiceContent}"
            ret_resp1.text = "语音签名：${it.points.getOrNull(1)?.voiceContent}"
            ret_resp2.text = "语音签名：${it.points.getOrNull(2)?.voiceContent}"
        })
        viewModel.userLevelInfo.observe(this, Observer {
            println("我是用户等级信息：=$it")
        })
        viewModel.loadState.observe(this, Observer {
            println("我请求状态信息：=$it")
        })

        test_net.onClickNew {
            viewModel.getInfo()
        }
        test_rxjava.onClickNew {
//            viewModel.getUserLevelByRx()
            viewModel.getUserLevelByRx2()

        }

        test_rn.onClickNew {
            logger.info("测试rn跳转")
            RNPageActivity.start(this, "HomePage");
        }

        crash.onClickNew {
            CrashReport.testJavaCrash();
//            "kjhd".toInt()
        }
        login.onClickNew {
            startActivity<LoginActivity>()
            finish()
        }
        goto_photo.onClickNew {
            goToPictureSelectPager(1, PictureConfig.TYPE_VIDEO)
        }
        set_push.onClickNew {
            val userId = SessionUtils.getUserId().toString()
            logger.info("jpush userId=$userId")
            val tagAliasBean = TagAliasOperatorHelper.TagAliasBean()
            tagAliasBean.action = TagAliasOperatorHelper.ACTION_SET
            tagAliasBean.isAliasAction = true
            tagAliasBean.alias = userId
            TagAliasOperatorHelper.getInstance().handleAction(tagAliasBean)
        }
        open_cv.onClickNew {
            PrivateConversationActivity.newInstance(this, 20000041)
        }
        open_gift.onClickNew {
            val gift = ChatSendGiftFragment()
            gift.show(this, "ChatSendGiftFragment")
        }

        test_real.onClickNew {
            //实名认证界面
            ARouter.getInstance().build(ARouterConstant.REAL_NAME_MAIN_ACTIVITY).navigation()
        }
        tv_clear_session.onClickNew {
            LoginManager.doLoginOut {
                if (it) {
                    logger.info("退出登录成功")
                }
            }
        }
        report.onClickNew {
            val extra = Bundle()
            extra.putLong(ParamConstant.TARGET_USER_ID, 2000000)
            extra.putInt(ParamConstant.REPORT_TYPE, 0)

            ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra).navigation()
        }
        test_loading.onClickNew {
            LoadingDialog(this).showDialog(true)
        }

        tv_record_voice.onClickNew {
            startActivity<VoiceSignActivity>()
        }

        tv_test_md5.onClickNew {
            val count = 50000
            val current = System.currentTimeMillis()
            repeat(count) {
                val r = MD5Util.encodePassword(UUID.randomUUID().toString())
            }
            logger.info("UUID+md5耗时=${System.currentTimeMillis() - current}")
            val current2 = System.currentTimeMillis()
            repeat(count) {
                val r = UUID.randomUUID().toString().replace("-", "")
            }
            logger.info("UUID+replace耗时=${System.currentTimeMillis() - current2}")

            val current3 = System.currentTimeMillis()
            repeat(count) {
                val r = UUID.randomUUID().toString()
            }
            logger.info("UUID耗时=${System.currentTimeMillis() - current3}")
            val current4 = System.currentTimeMillis()
            repeat(count) {
                val r = MD5Util.encodeBySHA256(UUID.randomUUID().toString())
            }
            logger.info("UUID+SHA256耗时=${System.currentTimeMillis() - current4}")

            val current5 = System.currentTimeMillis()
            repeat(count) {
                val r = MD5Util.encodeBySHA(UUID.randomUUID().toString())
            }
            logger.info("UUID+SHA耗时=${System.currentTimeMillis() - current5}")
            /**50000次计算的耗时：
             * 2020-07-23 19:47:30.519 11886-11886/com.julun.huanque I/TestActivity: UUID+md5耗时=2880
            2020-07-23 19:47:32.003 11886-11886/com.julun.huanque I/TestActivity: UUID+replace耗时=1483
            2020-07-23 19:47:32.665 11886-11886/com.julun.huanque I/TestActivity: UUID耗时=662
            2020-07-23 19:47:36.972 11886-11886/com.julun.huanque I/TestActivity: UUID+SHA256耗时=4307
            2020-07-23 19:47:40.047 11886-11886/com.julun.huanque I/TestActivity: UUID+SHA耗时=3075
             */
        }
        tv_bind.onClickNew {
            ARouter.getInstance().build(ARouterConstant.PHONE_NUM_LOGIN_ACTIVITY).with(Bundle().apply {
                putInt(IntentParamKey.TYPE.name, PhoneLoginType.TYPE_BIND)
            }).navigation()
        }

        tv_invite.onClickNew {
//            ARouter.getInstance().build(ARouterConstant.INVITE_SHARE_ACTIVITY).withString(IntentParamKey.TYPE.name, ShareFromModule.Program).withSerializable(
//                IntentParamKey.LIVE_INFO.name,MicAnchor(prePic = "user/head/b0295e5d-5c2d-45a8-928f-5333c880f489.jpg").apply {
//                    programName="主播昵称"
//                    programId=10007
//                    headPic="user/head/b0295e5d-5c2d-45a8-928f-5333c880f489.jpg"
//                }
//            ).navigation()
            ARouter.getInstance().build(ARouterConstant.INVITE_SHARE_ACTIVITY)
                .withString(IntentParamKey.TYPE.name, ShareFromModule.Invite).navigation()
        }

        btn_usercard.onClickNew {
            //用户卡片
        }

        btn_manager.onClickNew {
            //管理弹窗
            val dialog = CardManagerDialogFragment()
            dialog.show(supportFragmentManager, "CardManagerDialogFragment")
        }
        val spannableString = DraweeSpanStringBuilder("1234567一句带彩虹屁的文本还带动效一句带彩虹色的文本还带动效 WWWWAAAA243555")
        val start = 10
        val end = 20
        val span = AnimatedRainbowSpan()
        spannableString.setSpan(span, start, end, 0)
        spannableString.setImageSpan(this, R.mipmap.intimate_level_1, 3, 3, dp2px(30), dp2px(16))
        spannableString.setImageSpan(this, R.mipmap.anim_living, 5, 5, dp2px(30), dp2px(16))
        text_rainbow.setDraweeSpanStringBuilder(spannableString)

        val objectAnimator: ObjectAnimator = ObjectAnimator.ofFloat(
            span, ANIMATED_COLOR_SPAN_FLOAT_PROPERTY, 0f, 100f
        )
        objectAnimator.setEvaluator(FloatEvaluator())
        objectAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                text_rainbow.setDraweeSpanStringBuilder(spannableString)
            }
        })
        objectAnimator.setInterpolator(LinearInterpolator())
        objectAnimator.setDuration(DateUtils.MINUTE_IN_MILLIS * 3)
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE)
//        objectAnimator.start()

        spannableString.setDraweeSpanChangedListener { builder ->
            logger.info("setDraweeSpanChangedListener")
            text_rainbow.setDraweeSpanStringBuilder(builder)
        }


        test_big_format.text = "num1=${StringHelper.formatBigNum(BigInteger("562"))}\n" +
                "num2=${StringHelper.formatBigNum(BigInteger("5621"))}\n" +
                "num3=${StringHelper.formatBigNum(BigInteger("4843195"))}\n" +
                "num4=${StringHelper.formatBigNum(BigInteger("21773753736"))}\n" +
                "num5=${StringHelper.formatBigNum(BigInteger("5621773753736"))}\n" +
                "num6=${StringHelper.formatBigNum(BigInteger("105807943509339"))}\n" +
                "num7=${StringHelper.formatBigNum(BigInteger("5297181089855920"))}\n" +
                "num8=${StringHelper.formatBigNum(BigInteger("938219908377998000"))}\n"
    }

    private val ANIMATED_COLOR_SPAN_FLOAT_PROPERTY: Property<AnimatedRainbowSpan, Float> =
        object : Property<AnimatedRainbowSpan, Float>(
            Float::class.java, "ANIMATED_COLOR_SPAN_FLOAT_PROPERTY"
        ) {
            override operator fun set(span: AnimatedRainbowSpan, value: Float) {
                span.translateXPercentage = value
            }

            override operator fun get(span: AnimatedRainbowSpan): Float {
                return span.translateXPercentage
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        logger.info("onSaveInstanceState=${outState}")
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logger("onActivityResult")
        try {
            if (requestCode == 1001) {

                val selectList = PictureSelector.obtainMultipleResult(data)
                logger("收到图片=$selectList")
                for (media in selectList) {
                    Log.i("图片-----》", media.path)
                }
                if (selectList.size > 0) {
                    val pathList = mutableListOf<String>()
                    selectList.forEach {
                        val isGif = PictureMimeType.isGif(it.pictureType)
                        //动图直接上传原图
                        when {
                            isGif -> {
                                pathList.add(it.path)
                            }
                            it.isCompressed -> {
                                pathList.add(it.compressPath)
                            }
                            else -> {
                                pathList.add(it.path)
                            }
                        }
                    }

                }
            } else if (requestCode == 1002) {

                val selectList = PictureSelector.obtainMultipleResult(data)
                logger("收到视频=${selectList} ")
                val media = selectList[0]
                val bitmap = VideoUtils.getVideoFirstFrame(File(media.path))
//                val bitmap = VideoUtils.getVideoThumbnail(media.path)
                val vf = File(media.path)
                val bFile = FileUtils.bitmap2File(
                    bitmap
                        ?: return, vf.nameWithoutExtension, CommonInit.getInstance().getApp()
                ) ?: return
                logger("bitmap=${bitmap?.byteCount} ")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger("图片返回出错了")
        }

    }

    private fun goToPictureSelectPager(max: Int, type: Int) {
        if (type == PictureConfig.TYPE_IMAGE) {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .theme(com.julun.rnlib.R.style.picture_me_style_multi)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .minSelectNum(1)// 最小选择数量
                .maxSelectNum(max)
                .imageSpanCount(4)// 每行显示个数
                .selectionMode(PictureConfig.MULTIPLE)
                .previewImage(true)// 是否可预览图片
                .isCamera(true)// 是否显示拍照按钮
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                .enableCrop(true)// 是否裁剪
                .compress(true)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                //.compressSavePath(getPath())//压缩图片保存地址
                .glideOverride(150, 150)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .isGif(false)// 是否显示gif图片
//                    .selectionMedia(selectList)// 是否传入已选图片
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                .forResult(1001)

        } else if (type == PictureConfig.TYPE_VIDEO) {
            //只传单视频
            PictureSelector.create(this).openGallery(PictureMimeType.ofVideo())
                .theme(com.julun.huanque.common.R.style.picture_me_style_multi)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .maxSelectNum(1)// 最大图片选择数量
                .minSelectNum(1)// 最小选择数量
                .imageSpanCount(4)// 每行显示个数
                .selectionMode(PictureConfig.MULTIPLE)
                .previewVideo(true)// 是否可预览视频
                .isCamera(true)// 是否显示拍照按钮
                //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                .compress(true)// 是否压缩
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                //.compressSavePath(getPath())//压缩图片保存地址
                .glideOverride(150, 150)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
//                .selectionMedia(selectList)// 是否传入已选图片
                .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                //.cropCompressQuality(90)// 裁剪压缩质量 默认100
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                //.rotateEnabled(true) // 裁剪是否可旋转图片
                //.scaleEnabled(true)// 裁剪是否可放大缩小图片
                //.videoQuality()// 视频录制质量 0 or 1
                //.videoSecond()//显示多少秒以内的视频or音频也可适用
                //.recordVideoSecond()//录制视频秒数 默认60s
                .forResult(1002)
        }

        //结果回调onActivityResult code
    }

}