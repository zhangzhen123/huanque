package com.julun.huanque.core.ui.share

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
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
import kotlinx.android.synthetic.main.activity_invite_share.*
import org.jetbrains.anko.imageBitmap
import java.util.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/21 20:49
 *
 *@Description: VoiceSignActivity 邀友分享
 *
 */
@Route(path = ARouterConstant.INVITE_SHARE_ACTIVITY)
class InviteShareActivity : BaseVMActivity<InviteShareViewModel>() {


    private var currentSelectView: View? = null
    private var currentSelect: SharePoster? = null

    private var applyModule: String = ""

    //分享目标类型(可选项：Banner、Room、InviteFriend、Other)
    private var mShareKeyType = ""

    //分享目标Id ，如果是直播间，则填直播间ID, 如果是邀友，则填邀友海报ID
    private var mShareKeyId = ""
    private var programInfo: MicAnchor? = null
    private val wxService: LoginAndShareService? by lazy {
        ARouter.getInstance().build(ARouterConstant.LOGIN_SHARE_SERVICE).navigation() as? LoginAndShareService
    }

    override fun getLayoutId(): Int = R.layout.activity_invite_share


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        applyModule = intent.getStringExtra(IntentParamKey.TYPE.name) ?: applyModule
        programInfo = intent.getSerializableExtra(IntentParamKey.LIVE_INFO.name) as? MicAnchor

        mViewModel.programInfo = programInfo
        rv_share_contents.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv_share_contents.adapter = sharePosterAdapter
        rv_share_contents.addItemDecoration(HorizontalItemDecoration(dp2px(8)))

        rv_share_types.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv_share_types.adapter = shareAdapter
        rv_share_types.addItemDecoration(HorizontalItemDecoration(dp2px(20)))
        initViewModel()
        when (applyModule) {
            ShareFromModule.Program -> {
                mShareKeyType = "Room"
                mShareKeyId = "${mViewModel.programInfo?.programId ?: ""}"
                mViewModel.queryLiveQrCode()
            }
            ShareFromModule.Invite -> {
                mShareKeyType = "InviteFriend"
                mViewModel.querySharePoster(applyModule)
            }
        }

        mViewModel.queryShareType(applyModule)
        val rsLP = rv_share_contents.layoutParams as LinearLayout.LayoutParams
        when (applyModule) {
            ShareFromModule.Program -> {
                ll_copy.hide()
                rsLP.weight = 0f
                rsLP.height = LinearLayout.LayoutParams.WRAP_CONTENT
            }
            ShareFromModule.Invite -> {
                ll_copy.show()
                rsLP.weight = 1f
                rsLP.height = 0
            }
        }
        playInAnimator()
    }

    private fun playInAnimator() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom)
        animation.duration = 300
        rv_share_contents.startAnimation(animation)
        ll_bottom.startAnimation(animation)
    }

    private fun playOutAnimator() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_bottom)
        animation.duration = 200
        rv_share_contents.startAnimation(animation)
        ll_bottom.startAnimation(animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                finish()
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })
    }

    override fun initEvents(rootView: View) {
        tv_cancel_share.onClickNew {
//            playOutAnimator()
            finish()
        }

        tv_copy.onClickNew {
            GlobalUtils.copyToSharePlate(this, currentCode, attentionContent = "已复制邀请码")
        }
        sharePosterAdapter.setOnItemClickListener { _, view, position ->
            val item = sharePosterAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            when (item.itemType) {
                1 -> {
                    if (currentSelect != item) {
//                        currentSelectView = view
                        currentSelect = item
                        sharePosterAdapter.notifyDataSetChanged()
                        setShadowView()
                        mShareKeyId = "${item.posterId}"
                    }

                }
                2 -> {
                    logger.info("分享直播间 没有选中")
                }
            }

        }

        shareAdapter.onAdapterClickNew { _, _, position ->

            val item = shareAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            saveViewToImageFile(item.type)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    /**
     * 为了截图跟展示的不一样 这里搞个看不见的影子view 用于截图
     */
    private fun setShadowView() {
        if (currentSelect == null) {
            return
        }
        fl_holder.removeAllViews()
        when (applyModule) {
            ShareFromModule.Program -> {
                val viewRoot = LayoutInflater.from(this).inflate(R.layout.item_live_share, fl_holder)
//                val w=dp2px(275)
//                val h=dp2px(339)
//            viewRoot.layout(0,0,w,h)
//            val measuredWidth = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY)
//            val measuredHeight = View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
//            /** 当然，measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局。
//             * 按示例调用layout函数后，View的大小将会变成你想要设置成的大小。
//             */
//            /** 当然，measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局。
//             * 按示例调用layout函数后，View的大小将会变成你想要设置成的大小。
//             */
//            viewRoot.measure(measuredWidth, measuredHeight)
//            viewRoot.layout(0, 0, viewRoot.measuredWidth, viewRoot.measuredHeight)
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
            ShareFromModule.Invite -> {
                val viewRoot = LayoutInflater.from(this).inflate(R.layout.item_invite_share, fl_holder)
                val sdvSharePic = viewRoot.findViewById<SimpleDraweeView>(R.id.sdv_share_pic)
                val sdvQrCode = viewRoot.findViewById<SimpleDraweeView>(R.id.sdv_qr_code)
                val sdvUserPic = viewRoot.findViewById<SimpleDraweeView>(R.id.sdv_user_pic)
                val tvName = viewRoot.findViewById<TextView>(R.id.tv_user_name)
                val tvInvite = viewRoot.findViewById<TextView>(R.id.tv_invite_code)
                sdvSharePic.loadImage(currentSelect!!.posterPic, 250f, 450f)
//                sdvQrCode.loadImage(currentSelect!!.qrCode, 60f, 60f)
                sdvQrCode.imageBitmap = currentSelect!!.qrBitmap
                sdvUserPic.loadImage(SessionUtils.getHeaderPic(), 45f, 45f)
                val name = if (SessionUtils.getNickName().length > 5) {
                    "${SessionUtils.getNickName().substring(0, 5)}..."
                } else {
                    SessionUtils.getNickName()
                }
                tvName.text = name

                if (currentSelect!!.inviteCode.isNotEmpty()) {
                    tvInvite.text = "邀请码${currentSelect!!.inviteCode}"
                    tvInvite.show()
                } else {
                    tvInvite.hide()
                }
                val ivCheck = viewRoot.findViewById<ImageView>(R.id.iv_check)
                ivCheck.hide()
                currentSelectView = viewRoot.findViewById(R.id.item_invite_container)
            }
        }

    }

    private fun saveViewToImageFile(type: String) {


        if (currentSelectView == null) {
            ToastUtils.show("你还没有选择要分享的目标")
            return
        }

//        val checkView = currentSelectView?.findViewById<View>(R.id.iv_check)
//        //隐藏不必要的view
//        checkView?.hide()
        currentSelectView?.post {

            val bitmap = when (applyModule) {
                ShareFromModule.Program -> {
                    BitmapUtil.viewConversionBitmap(currentSelectView!!, Color.TRANSPARENT)
                }
                else -> {
                    BitmapUtil.viewConversionBitmap(currentSelectView!!)
                }
            }

//            val bitmap = BitmapUtil.viewConversionBitmap(currentSelectView!!)
//            checkView?.show()
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
                    })
                }
                ShareTypeEnum.SaveImage -> {
                    requestRWPermission {
                        Observable.just(bitmap).observeOn(Schedulers.io()).map { b ->
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

    }

    private fun initViewModel() {
        mViewModel.sharePosters.observe(this, Observer {
            if (it.isSuccess()) {
                renderViewData(it.requireT())
            }
        })
        mViewModel.shares.observe(this, Observer {
            if (it.isSuccess()) {
                shareAdapter.setNewInstance(it.requireT())
            }
        })

    }

    private var currentCode: String = ""
    private fun renderViewData(posterInfo: SharePosterInfo) {
        sharePosterAdapter.setNewInstance(posterInfo.posterList)
        currentCode = posterInfo.inviteCode

        if (applyModule == ShareFromModule.Program) {
            rv_share_contents.post {
                currentSelect = sharePosterAdapter.getItemOrNull(0)
//                currentSelectView = sharePosterAdapter.getViewByPosition(0, R.id.live_share_container)
                setShadowView()
            }
        } else if (applyModule == ShareFromModule.Invite) {
            tv_invite_code.text = posterInfo.inviteCode
            rv_share_contents.post {
                currentSelect = sharePosterAdapter.getItemOrNull(0)
//                currentSelectView = sharePosterAdapter.getViewByPosition(0, R.id.item_invite_container)
                setShadowView()
                sharePosterAdapter.notifyDataSetChanged()
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

    override fun showLoadState(state: NetState) {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null)
            wxService?.weiBoShareResult(data)
        finish()
    }

    private val sharePosterAdapter: BaseMultiItemQuickAdapter<SharePoster, BaseViewHolder> by lazy {
        object : BaseMultiItemQuickAdapter<SharePoster, BaseViewHolder>() {
            init {
                addItemType(1, R.layout.item_invite_share)
                addItemType(2, R.layout.item_live_share)
            }

            override fun convert(holder: BaseViewHolder, item: SharePoster) {

                when (holder.itemViewType) {
                    1 -> {
                        val sdvSharePic = holder.getView<SimpleDraweeView>(R.id.sdv_share_pic)
                        val sdvQrCode = holder.getView<SimpleDraweeView>(R.id.sdv_qr_code)
                        val sdvUserPic = holder.getView<SimpleDraweeView>(R.id.sdv_user_pic)

                        sdvSharePic.loadImage(item.posterPic, 250f, 450f)
//                        sdvQrCode.loadImage(item.qrCode, 60f, 60f)
                        sdvQrCode.imageBitmap = item.qrBitmap
                        sdvUserPic.loadImage(SessionUtils.getHeaderPic(), 45f, 45f)
                        val name = if (SessionUtils.getNickName().length > 5) {
                            "${SessionUtils.getNickName().substring(0, 5)}..."
                        } else {
                            SessionUtils.getNickName()
                        }
                        holder.setText(R.id.tv_user_name, name)

                        if (item.inviteCode.isNotEmpty()) {
                            holder.setGone(R.id.tv_invite_code, false).setText(R.id.tv_invite_code, "邀请码${item.inviteCode}")
                        } else {
                            holder.setGone(R.id.tv_invite_code, true)
                        }
                        val ivCheck = holder.getView<ImageView>(R.id.iv_check)

                        ivCheck.isSelected = currentSelect?.posterId == item.posterId
                    }
                    2 -> {
                        val sdvSharePic = holder.getView<SimpleDraweeView>(R.id.sdv_share_pic)
                        val sdvQrCode = holder.getView<SimpleDraweeView>(R.id.sdv_qr_code)
                        sdvSharePic.loadImage(item.posterPic, 235f, 235f)
                        sdvQrCode.setImageBitmap(item.qrBitmap)
                        val name = if (item.authorName.length > 5) {
                            "${item.authorName.substring(0, 5)}..."
                        } else {
                            item.authorName
                        }
                        holder.setText(R.id.tv_user_name, name)
                    }
                }


            }
        }
    }

    private val shareAdapter: BaseQuickAdapter<ShareType, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<ShareType, BaseViewHolder>(R.layout.item_share) {

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


}