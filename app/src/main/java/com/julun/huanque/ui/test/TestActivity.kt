package com.julun.huanque.ui.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.react.bridge.Arguments
import com.julun.huanque.R
import com.julun.huanque.activity.LoginActivity
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.message.activity.PrivateConversationActivity
import com.julun.huanque.message.fragment.ChatSendGiftFragment
import com.julun.huanque.support.LoginManager
import com.julun.jpushlib.TagAliasOperatorHelper
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnManager
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.android.synthetic.main.activity_test.test_rn
import kotlinx.android.synthetic.main.fragment_mine.*
import org.jetbrains.anko.startActivity

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
            ret_resp.text="语音签名：${it.points.getOrNull(0)?.voiceContent}"
            ret_resp1.text="语音签名：${it.points.getOrNull(1)?.voiceContent}"
            ret_resp2.text="语音签名：${it.points.getOrNull(2)?.voiceContent}"
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
            goToPictureSelectPager(5)
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
            ARouter.getInstance().build(ARouterConstant.REALNAME_MAIN_ACTIVITY).navigation()
        }
        tv_clear_session.onClickNew {
            LoginManager.doLoginOut {
                if(it){
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logger("onActivityResult")
        try {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {

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
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger("图片返回出错了")
        }
    }

    /**
     *打开相册选择
     */
    private fun goToPictureSelectPager(max: Int) {
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
            .forResult(PictureConfig.CHOOSE_REQUEST)

        //结果回调onActivityResult code
    }
}