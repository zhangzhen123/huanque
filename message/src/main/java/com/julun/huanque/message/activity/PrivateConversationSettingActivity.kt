package com.julun.huanque.message.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.beans.ChatUserBean
import com.julun.huanque.common.bean.events.ChatBackgroundChangedEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.message.R
import com.julun.huanque.message.viewmodel.PrivateConversationSettingViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import io.rong.imlib.model.Conversation
import kotlinx.android.synthetic.main.act_private_conversation_setting.*
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/7/2 9:02
 *@描述  私聊会话设置页面
 */
class PrivateConversationSettingActivity : BaseActivity() {

    private var mPrivateConversationSettingViewModel: PrivateConversationSettingViewModel? = null

    companion object {
        fun newInstance(activity: Activity, targetID: Long, sex: String) {
            val intent = Intent(activity, PrivateConversationSettingActivity::class.java)
            intent.putExtra(ParamConstant.TARGETID, targetID)
            intent.putExtra(ParamConstant.SEX, sex)
            activity.startActivity(intent)
        }
    }

    override fun getLayoutId() = R.layout.act_private_conversation_setting

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        findViewById<TextView>(R.id.tvTitle).text = "聊天详情"

        initViewModel()
        mPrivateConversationSettingViewModel?.targetId = intent?.getLongExtra(ParamConstant.TARGETID, 0) ?: 0
        mPrivateConversationSettingViewModel?.getConversationDisturbStatus()
        val sex = intent?.getStringExtra(ParamConstant.SEX) ?: ""
        ImageUtils.setDefaultHeaderPic(sdv_header, sex)

        mPrivateConversationSettingViewModel?.getChatDetail()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mPrivateConversationSettingViewModel = ViewModelProvider(this).get(PrivateConversationSettingViewModel::class.java)
        mPrivateConversationSettingViewModel?.disturbStatus?.observe(this, Observer {
            if (it != null) {
                iv_no_disturbing.isSelected = it == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB.value
            }
        })
        mPrivateConversationSettingViewModel?.chatDetailData?.observe(this, Observer {
            if (it != null) {
                ImageUtils.loadImage(sdv_header, it.headPic, 60f, 60f)
                tv_nickname.text = it.nickname
                tv_introduce.text = it.mySign

                tv_notification_status.text = "亲密度等级Lv${it.chatBackgroundLevel}解锁"
                if (it.intimateLevel >= it.chatBackgroundLevel) {
                    tv_notification_status.hide()
                } else {
                    tv_notification_status.show()
                }

                //欢遇状态
                val meetResource = GlobalUtils.getMeetStatusResource(it.meetStatus)
                if (meetResource > 0) {
                    //显示图标
                    iv_huanyu.show()
                    iv_huanyu.setImageResource(meetResource)
                } else {
                    //隐藏图标
                    iv_huanyu.hide()
                }

            }
        })

        mPrivateConversationSettingViewModel?.blackStatus?.observe(this, Observer {
            if (it != null) {
                val blackStr = if (it) {
                    "已添加至黑名单"
                } else {
                    ""
                }
                tv_black_status.text = blackStr
            }
        })
    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew {
            finish()
        }
        iv_no_disturbing.onClickNew {
            mPrivateConversationSettingViewModel?.conversationDisturbSetting(iv_no_disturbing.isSelected)
        }
        view_info.onClickNew {
            //跳转主页
            RNPageActivity.start(
                this,
                RnConstant.PERSONAL_HOMEPAGE,
                Bundle().apply { putLong("userId", mPrivateConversationSettingViewModel?.targetId ?: 0) })
        }
        view_report.onClickNew {
            val extra = Bundle()
            extra.putLong(ParamConstant.TARGET_USER_ID, mPrivateConversationSettingViewModel?.targetId ?: 0)

            ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra).navigation()
        }

        view_blacklist.onClickNew {
            //拉黑
            MyAlertDialog(
                this
            ).showAlertWithOKAndCancel(
                "加入黑名单，你们将相互不能给对方发送消息",
                MyAlertDialog.MyDialogCallback(onRight = {
                    //取消黑名单
                    if (mPrivateConversationSettingViewModel?.blackStatus?.value == true) {
                        mPrivateConversationSettingViewModel?.recover()
                    }
                }, onCancel = {
                    //拉黑
                    if (mPrivateConversationSettingViewModel?.blackStatus?.value == false) {
                        mPrivateConversationSettingViewModel?.black()
                    }
                }), "加入黑名单", "允许", "不允许"
            )
        }

        view_set_bg.onClickNew {
            //设置背景
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        goToPictureSelectPager()
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger.info("获取权限被永久拒绝")
                        val message = "无法获取到相机/存储权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
    }

    /**
     *
     */
    private fun goToPictureSelectPager() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            .theme(R.style.picture_me_style_single)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
            .minSelectNum(1)// 最小选择数量
            .imageSpanCount(4)// 每行显示个数
            .selectionMode(PictureConfig.SINGLE)
            .previewImage(true)// 是否可预览图片
            .isCamera(true)// 是否显示拍照按钮
            .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
            .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
            //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
            .enableCrop(false)// 是否裁剪
            .compress(true)// 是否压缩
            .synOrAsy(true)//同步true或异步false 压缩 默认同步
            //.compressSavePath(getPath())//压缩图片保存地址
            .glideOverride(120, 120)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
            .isGif(false)// 是否显示gif图片
//                    .selectionMedia(selectList)// 是否传入已选图片
            .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
            //.cropCompressQuality(90)// 裁剪压缩质量 默认100
            .minimumCompressSize(100)// 小于100kb的图片不压缩
//            .cropWH(200, 200)// 裁剪宽高比，设置如果大于图片本身宽高则无效
//            .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
//            .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
            .isDragFrame(false)
//            .circleDimmedLayer(true)// 是否圆形裁剪
//            .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
//            .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
//            .rotateEnabled(false) // 裁剪是否可旋转图片
//            .scaleEnabled(true)// 裁剪是否可放大缩小图片
            .forResult(PictureConfig.CHOOSE_REQUEST)

        //结果回调onActivityResult code
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                logger.info("收到图片")
                val selectList = PictureSelector.obtainMultipleResult(data)
                for (media in selectList) {
                    Log.i("图片-----》", media.path)
                }
                if (selectList.size > 0) {
                    val media = selectList[0]
//                    val path: String?
//                    path = if (media.isCut && !media.isCompressed) {
//                        // 裁剪过
//                        media.cutPath
//                    } else if (media.isCompressed || media.isCut && media.isCompressed) {
//                        // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
//                        media.compressPath
//                    } else {
//                        media.path
//                    }
//                    logger.info("DXC  收到图片:$path，media.path = ${media.path}")
                    //media.path
//                    sendChatMessage(pic = path, localPic = media.path, messageType = Message_Pic)
                    SharedPreferencesUtils.commitString(GlobalUtils.getBackgroundKey(mPrivateConversationSettingViewModel?.targetId ?: 0), media.path)
                    EventBus.getDefault().post(ChatBackgroundChangedEvent(mPrivateConversationSettingViewModel?.targetId ?: 0))
//                    ImageUtils.copyImageToSdCard(media.path,"${SessionUtils.getUserId()}-${mPrivateConversationSettingViewModel?.targetId}")
//                    if(!mLoadingDialog.isShowing){
//                        mLoadingDialog.showDialog()
//                    }
//                    mViewModel?.uploadHead(path)
//                    RongCloudManager.setMediaMessage()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.info("图片返回出错了")
        }
    }
}