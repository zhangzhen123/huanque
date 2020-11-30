package com.julun.huanque.core.ui.share

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.interfaces.routerservice.LoginAndShareService
import com.julun.huanque.common.net.NAction
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.bitmap.BitmapUtil
import com.julun.huanque.common.utils.device.PhoneUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_live_share.*
import java.util.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/29 16:38
 *
 *@Description: 直播间分享页面
 *
 */
class LiveShareActivity : BaseVMActivity<InviteShareViewModel>() {
    companion object {
        //跳转密友页面的code
        const val RequestCode = 0x0001

        /**
         * @param type 来源类型 （动态，评论，其他）
         * @param postId 动态ID
         */
        fun newInstance(act: Activity, type: String, postId: Long? = null, commentId: Long? = null) {
            val intent = Intent(act, LiveShareActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.TYPE, type)
                if (postId != null) {
                    intent.putExtra(ParamConstant.POST_ID, postId)
                    if (commentId != null) {
                        intent.putExtra(ParamConstant.COMMENT_ID, commentId)
                    }
                }
                act.startActivity(intent)
            }
        }
    }


    private var currentSelectView: View? = null
    private var currentSelect: SharePoster? = null

    private var applyModule: String = ShareFromModule.Program

    //分享目标类型(可选项：Banner、Room、InviteFriend、Other)
    private var mShareKeyType = ""

    //分享目标Id ，如果是直播间，则填直播间ID, 如果是邀友，则填邀友海报ID
    private var mShareKeyId = ""
    private var programInfo: MicAnchor? = null

    //动态ID
    private var mPostId = 0L

    //评论ID
    private var mCommentId = 0L

    private val wxService: LoginAndShareService? by lazy {
        ARouter.getInstance().build(ARouterConstant.LOGIN_SHARE_SERVICE).navigation() as? LoginAndShareService
    }
    private val shareAdapter: ShareAdapter by lazy { ShareAdapter() }

    private val shareAdapter2: ShareAdapter by lazy { ShareAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_live_share


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        StatusBarUtil.setTransparent(this)
        initViewModel()

        rv_share_types.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv_share_types.adapter = shareAdapter
        rv_share_types.addItemDecoration(HorizontalItemDecoration(dp2px(20)))

        mViewModel.type = intent?.getStringExtra(ParamConstant.TYPE) ?: ""
        if (mViewModel.type == ShareFromType.Share_Dynamic || mViewModel.type == ShareFromType.Share_Comment) {
            //动态分享  直接显示分享路径
            if (mViewModel.type == ShareFromType.Share_Dynamic) {
                shareAdapter.setNewInstance(mViewModel.getShareType(false, hasInner = true))
            } else {
                shareAdapter.setNewInstance(mViewModel.getShareType(false))
            }

            mPostId = intent?.getLongExtra(ParamConstant.POST_ID, 0) ?: 0L
            mCommentId = intent?.getLongExtra(ParamConstant.COMMENT_ID, 0) ?: 0L
            if (mViewModel.type == ShareFromType.Share_Comment) {
                //分享评论，获取分享信息
                mViewModel.queryPostShareInfo(null, mPostId, mCommentId)
            }
        } else {
            //其他分享
            programInfo = intent.getSerializableExtra(IntentParamKey.LIVE_INFO.name) as? MicAnchor

            mViewModel.programInfo = programInfo

            mViewModel.queryLiveShareInfo()
        }


        playInAnimator()
        root_share.onTouch { _, event ->
            finish()
            false
        }
        tv_cancel_share.onClickNew {
            finish()
        }
        share_Img_close.onClick {
            finish()
        }
        ll_bottom.onClick { }
        share_img_container.onClick { }
    }

    private fun playInAnimator() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom)
        animation.duration = 300
        ll_bottom.startAnimation(animation)
    }

    override fun initEvents(rootView: View) {
        tv_cancel_share.onClickNew {
//            playOutAnimator()
            finish()
        }
        shareAdapter.onAdapterClickNew { _, _, position ->

            val item = shareAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            if (mViewModel.type == ShareFromType.Share_Dynamic) {
                //动态分享
                mViewModel.queryPostShareInfo(item.type, mPostId)
            } else if (mViewModel.type == ShareFromType.Share_Comment) {
                //分享图片
                saveViewToImageFile(item.type)
            } else {
                //其他分享
                shareLive(item.type)
            }
        }

        shareAdapter2.onAdapterClickNew { _, _, position ->

            val item = shareAdapter2.getItemOrNull(position) ?: return@onAdapterClickNew
            saveViewToImageFile(item.type)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    private fun saveViewToImageFile(type: String) {
        var bitmap: Bitmap? = null
        if (mViewModel.type == ShareFromType.Share_Comment) {
            //分享评论
            val con_comment_share = ll_comment.findViewById<ConstraintLayout>(R.id.view_comment)
            bitmap = BitmapUtil.viewConversionBitmap(con_comment_share ?: return, Color.TRANSPARENT)
        } else {
            //其他分享
            if (currentSelectView == null) {
                ToastUtils.show("你还没有选择要分享的目标")
                return
            }
            currentSelectView?.post {
                bitmap = BitmapUtil.viewConversionBitmap(currentSelectView!!, Color.TRANSPARENT)
            }
        }

        if (bitmap == null) {
            return
        }

        when (type) {
            ShareTypeEnum.FriendCircle -> {
                //朋友圈
                if (wxService?.checkWeixinInstalled(this) == true) {
                    wxService?.weiXinShare(this, ShareObject().apply {
                        this.shareType = WeiXinShareType.WXImage
                        this.shareWay = ShareWayEnum.WXSceneTimeline
                        this.shareImage = bitmap
                        //以下是应用内传递数据使用
                        this.platForm = ShareTypeEnum.FriendCircle
                        this.shareKeyType = mShareKeyType
                        this.shareKeyId = mShareKeyId
                        this.platForm = ShareTypeEnum.FriendCircle
                        this.postId = mPostId
                    })
                    finish()
                }
            }
            ShareTypeEnum.WeChat -> {
                //微信
                if (wxService?.checkWeixinInstalled(this) == true) {
                    wxService?.weiXinShare(this, ShareObject().apply {
                        this.shareType = WeiXinShareType.WXImage
                        this.shareWay = ShareWayEnum.WXSceneSession
                        this.shareImage = bitmap
                        //以下是应用内传递数据使用
                        this.platForm = ShareTypeEnum.WeChat
                        this.shareKeyType = mShareKeyType
                        this.shareKeyId = mShareKeyId
                        this.platForm = ShareTypeEnum.WeChat
                        this.postId = mPostId
                    })
                    finish()
                }
            }
            ShareTypeEnum.Sina -> {
                wxService?.weiBoShare(this, ShareObject().apply {
                    this.shareType = WeiBoShareType.WbImage
                    this.shareImage = bitmap
                    //以下是应用内传递数据使用
                    this.platForm = ShareTypeEnum.Sina
                    this.shareKeyType = mShareKeyType
                    this.shareKeyId = mShareKeyId
                    this.postId = mPostId
                })
            }
            ShareTypeEnum.SaveImage -> {
                requestRWPermission {
                    Observable.just(bitmap!!).observeOn(Schedulers.io()).map { b ->
                        FileUtils.saveBitmapToDCIM(b, UUID.randomUUID().toString())
                    }.observeOn(AndroidSchedulers.mainThread()).subscribe {
                        if (it == true) {
                            ToastUtils.show("保存成功")

                        }
                    }
                }
            }
        }

    }

    private fun requestRWPermission(action: NAction) {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger("获取权限成功")
                        action()
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger("获取权限被永久拒绝")
                        val message = "存储权限被禁用，请到设置中授予欢鹊存储权限"
                        MyAlertDialog(this).showAlertWithOKAndCancel(message = message,
                            title = "设置提醒",
                            noText = "取消",
                            okText = "去设置",
                            callback = MyAlertDialog.MyDialogCallback(
                                onRight = {
                                    PhoneUtils.getPermissionSetting(packageName).let {
                                        if (ForceUtils.activityMatch(it)) {
                                            try {
                                                startActivity(it)
                                            } catch (e: Exception) {
                                                reportCrash("跳转权限或者默认设置页失败", e)
                                            }
                                        }
                                    }

                                }
                            ))
                    }
                }

            }
    }

    private fun shareLive(type: String) {
        var shareObject = if (mViewModel.type == ShareFromType.Share_Dynamic || mViewModel.type == ShareFromType.Share_Comment) {
            val postShareBean = mViewModel.postShareBeanData.value ?: return
            ShareObject().apply {
                shareUrl = postShareBean.shareUrl
                shareTitle = postShareBean.content
                shareContent = postShareBean.content
                sharePic = StringHelper.getOssImgUrl(postShareBean.pic)
                postId = mPostId
//                sharePic = StringHelper.getOssImgUrl("user/head/2019110608363175169.jpg?x-oss-process=image/resize,m_fixed,w_100,h_100")
            }
        } else {
            mViewModel.liveShareInfo.value?.getT() ?: return

        }


        when (type) {
            ShareTypeEnum.FriendCircle -> {
                //朋友圈
                if (wxService?.checkWeixinInstalled(this) == true) {
                    shareObject.shareType = WeiXinShareType.WXWeb
                    shareObject.shareWay = ShareWayEnum.WXSceneTimeline
                    shareObject.platForm = ShareTypeEnum.FriendCircle
                    wxService?.weiXinShare(this, shareObject)
                    finish()
                }
            }
            ShareTypeEnum.WeChat -> {
                //微信
                if (wxService?.checkWeixinInstalled(this) == true) {
                    shareObject.shareType = WeiXinShareType.WXWeb
                    shareObject.shareWay = ShareWayEnum.WXSceneSession
                    shareObject.platForm = ShareTypeEnum.WeChat
                    wxService?.weiXinShare(this, shareObject)
                    finish()
                }
            }
            ShareTypeEnum.Sina -> {
                shareObject.shareType = WeiBoShareType.WbWeb
                shareObject.platForm = ShareTypeEnum.Sina
                wxService?.weiBoShare(this, shareObject)
            }
            ShareTypeEnum.ShareImage -> {
                mViewModel.queryLiveQrCode()
            }
            ShareTypeEnum.Chat -> {
                //站内消息
                val postShareBean = mViewModel.postShareBeanData.value ?: return
                ShareFriendsActivity.newInstance(this, postShareBean, RequestCode)
            }
        }

    }

    private fun initViewModel() {
        mViewModel.shares.observe(this, Observer {
            if (it.isSuccess()) {
                shareAdapter.setNewInstance(it.requireT())
            }
        })
        mViewModel.liveShareInfo.observe(this, Observer {
            if (it.isSuccess()) {
                //分享信息回来后再获取分享类型
                mViewModel.queryShareType(applyModule)
            }
        })
        mViewModel.sharePosters.observe(this, Observer {
            if (it.isSuccess()) {
                renderViewData(it.requireT())
            }
        })
        mViewModel.postShareBeanData.observe(this, Observer {
            if (it != null) {

                if (mViewModel.type == ShareFromType.Share_Dynamic) {
                    //分享动态,获取到服务端的分享数据，开始分享
                    shareLive(it.shareType)
                } else if (mViewModel.type == ShareFromType.Share_Comment) {
                    //分享评论，显示图片
                    showCommentByComment(it)
                }
            }
        })
    }

    private fun renderViewData(posterInfo: SharePosterInfo) {
        currentSelect = posterInfo.posterList.getOrNull(0) ?: return

        ll_bottom.hide()
        val viewRoot = share_img_container
        val sdvSharePic = viewRoot.findViewById<SimpleDraweeView>(R.id.sdv_share_pic)
        val sdvQrCode = viewRoot.findViewById<SimpleDraweeView>(R.id.sdv_qr_code)
        val tvName = viewRoot.findViewById<TextView>(R.id.tv_user_name)
        val text2 = viewRoot.findViewById<TextView>(R.id.tv002)
        sdvSharePic.loadImage(currentSelect!!.posterPic, 235f, 235f)
        sdvQrCode.setImageBitmap(currentSelect!!.qrBitmap)
        val name = if (currentSelect!!.authorName.length > 5) {
            "${currentSelect!!.authorName.substring(0, 5)}..."
        } else {
            currentSelect!!.authorName
        }
        tvName.text = name
        text2.text = "分享给好友"
        val shareTypeView = viewRoot.findViewById<RecyclerView>(R.id.rv_share_types_img)
        shareTypeView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        shareTypeView.adapter = shareAdapter2
        shareAdapter2.setNewInstance(mViewModel.getShareType(false))
        fl_holder.post {
            share_img_container.show()
            share_Img_close.show()
            setShadowView()
        }
    }


    /**
     * 显示动态分享数据
     */
    private fun showCommentByComment(postShareBean: PostShareBean) {
        ll_comment.show()
        ll_comment.findViewById<TextView>(R.id.tv_content).text = postShareBean.content
        ll_comment.findViewById<TextView>(R.id.tv_comment).text = postShareBean.commentContent
        ll_comment.findViewById<TextView>(R.id.tv_nickname).text = postShareBean.commentUserName
        val sdv_pic = ll_comment.findViewById<SimpleDraweeView>(R.id.sdv_pic)
        if (postShareBean.pic.isNotEmpty()) {
            sdv_pic.loadImage(postShareBean.pic, 220f, 123f)
        } else {
            //显示默认图
            sdv_pic.loadImageLocal(R.mipmap.pic_comment_place)
        }
        val sdv_qr_code = ll_comment.findViewById<SimpleDraweeView>(R.id.sdv_qr_code)
        sdv_qr_code.setImageBitmap(postShareBean.qrBitmap ?: return)
    }


    private fun setShadowView() {
        if (currentSelect == null) {
            return
        }
        fl_holder.removeAllViews()
        val viewRoot = LayoutInflater.from(this).inflate(R.layout.item_live_share, fl_holder)
        val sdvSharePic = viewRoot.findViewById<SimpleDraweeView>(R.id.sdv_share_pic)
        val sdvQrCode = viewRoot.findViewById<SimpleDraweeView>(R.id.sdv_qr_code)
        val tvName = viewRoot.findViewById<TextView>(R.id.tv_user_name)
        val text2 = viewRoot.findViewById<TextView>(R.id.tv002)
        sdvSharePic.loadImage(currentSelect!!.posterPic, 235f, 235f)
        sdvQrCode.setImageBitmap(currentSelect!!.qrBitmap)
        val name = if (currentSelect!!.authorName.length > 5) {
            "${currentSelect!!.authorName.substring(0, 5)}..."
        } else {
            currentSelect!!.authorName
        }
        tvName.text = name
        text2.text = "扫码加为好友"
        currentSelectView = viewRoot.findViewById(R.id.live_share_container)

    }


    override fun showLoadState(state: NetState) {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK) {
            //密友页面返回
            finish()
            return
        }
        if (data != null)
            wxService?.weiBoShareResult(data)
        finish()
    }


    class ShareAdapter : BaseQuickAdapter<ShareType, BaseViewHolder>(R.layout.item_share) {
        override fun convert(holder: BaseViewHolder, item: ShareType) {
            val sdvPic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
            if (item.url.isNotEmpty()) {
                sdvPic.loadImage(item.url, 45f, 45f)
            } else if (item.res != -1) {
                sdvPic.loadImageLocal(item.res)
            }

            holder.setText(R.id.tv_title, item.title)
        }

    }


}