package com.julun.huanque.core.ui.share

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.interfaces.routerservice.LoginAndShareService
import com.julun.huanque.common.net.NAction
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.bitmap.BitmapUtil
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


    private var currentSelectView: View? = null
    private var currentSelect: SharePoster? = null

    private var applyModule: String = ShareFromModule.Program

    //分享目标类型(可选项：Banner、Room、InviteFriend、Other)
    private var mShareKeyType = ""

    //分享目标Id ，如果是直播间，则填直播间ID, 如果是邀友，则填邀友海报ID
    private var mShareKeyId = ""
    private var programInfo: MicAnchor? = null
    private val wxService: LoginAndShareService? by lazy {
        ARouter.getInstance().build(ARouterConstant.LOGIN_SHARE_SERVICE).navigation() as? LoginAndShareService
    }
    private val shareAdapter: ShareAdapter by lazy { ShareAdapter() }

    private val shareAdapter2: ShareAdapter by lazy { ShareAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_live_share


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        StatusBarUtil.setTransparent(this)
        programInfo = intent.getSerializableExtra(IntentParamKey.LIVE_INFO.name) as? MicAnchor

        mViewModel.programInfo = programInfo

        rv_share_types.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv_share_types.adapter = shareAdapter
        rv_share_types.addItemDecoration(HorizontalItemDecoration(dp2px(20)))
        initViewModel()

        mViewModel.queryLiveShareInfo()

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
            shareLive(item.type)
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


        if (currentSelectView == null) {
            ToastUtils.show("你还没有选择要分享的目标")
            return
        }

        currentSelectView?.post {

            val bitmap = BitmapUtil.viewConversionBitmap(currentSelectView!!, Color.TRANSPARENT)

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
                        val message = "无法获取到录音权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
    }

    private fun shareLive(type: String) {
        val shareObject=mViewModel.liveShareInfo.value?.getT() ?: return
        when (type) {
            ShareTypeEnum.FriendCircle -> {
                //朋友圈
                if (wxService?.checkWeixinInstalled(this) == true) {
                    shareObject.shareType=WeiXinShareType.WXWeb
                    shareObject.shareWay = ShareWayEnum.WXSceneTimeline
                    shareObject.platForm = ShareTypeEnum.FriendCircle
                    wxService?.weiXinShare(this,shareObject )
                    finish()
                }
            }
            ShareTypeEnum.WeChat -> {
                //微信
                if (wxService?.checkWeixinInstalled(this) == true) {
                    shareObject.shareType=WeiXinShareType.WXWeb
                    shareObject.shareWay = ShareWayEnum.WXSceneSession
                    shareObject.platForm = ShareTypeEnum.WeChat
                    wxService?.weiXinShare(this, shareObject)
                    finish()
                }
            }
            ShareTypeEnum.Sina -> {
                shareObject.shareType= WeiBoShareType.WbWeb
                shareObject.platForm = ShareTypeEnum.Sina
                wxService?.weiBoShare(this, shareObject)
            }
            ShareTypeEnum.ShareImage -> {
                mViewModel.queryLiveQrCode()
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
        if (data != null)
            wxService?.weiBoShareResult(data)
        finish()
    }


    class ShareAdapter : BaseQuickAdapter<ShareType, BaseViewHolder>(R.layout.item_share) {
        override fun convert(holder: BaseViewHolder, item: ShareType) {
            val sdvPic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
            if (item.url.isNotEmpty()) {
                sdvPic.loadImage(item.url, 250f, 450f)
            } else if (item.res != -1) {
                sdvPic.loadImageLocal(item.res)
            }

            holder.setText(R.id.tv_title, item.title)
        }

    }


}