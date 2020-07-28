package com.julun.huanque.core.ui.share

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.ShareObject
import com.julun.huanque.common.bean.beans.SharePoster
import com.julun.huanque.common.bean.beans.SharePosterInfo
import com.julun.huanque.common.bean.beans.ShareType
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.interfaces.routerservice.WeiXinService
import com.julun.huanque.common.net.NAction
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.bitmap.BitmapUtil
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_invite_share.*
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

    private var applyModule: String=""
    private val wxService: WeiXinService? by lazy {
        ARouter.getInstance().build(ARouterConstant.WEIXIN_SERVICE).navigation() as? WeiXinService
    }

    override fun getLayoutId(): Int = R.layout.activity_invite_share


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        applyModule= intent.getStringExtra(IntentParamKey.TYPE.name)?:applyModule
        rv_share_contents.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv_share_contents.adapter = sharePosterAdapter
        rv_share_contents.addItemDecoration(HorizontalItemDecoration(dp2px(8)))

        rv_share_types.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv_share_types.adapter = shareAdapter
        rv_share_types.addItemDecoration(HorizontalItemDecoration(dp2px(13)))
        initViewModel()

        mViewModel.querySharePoster(applyModule)
        mViewModel.queryShareType()
    }

    override fun initEvents(rootView: View) {
        tv_cancel_share.onClickNew {
            finish()
        }

        tv_copy.onClickNew {
            GlobalUtils.copyToSharePlate(this, currentCode)
        }
        sharePosterAdapter.setOnItemClickListener { _, view, position ->
            if (currentSelect != sharePosterAdapter.getItemOrNull(position)) {
                currentSelectView = view
                currentSelect = sharePosterAdapter.getItemOrNull(position)
                sharePosterAdapter.notifyDataSetChanged()
            }

        }

        shareAdapter.onAdapterClickNew { _, _, position ->

            val item = shareAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            saveViewToImageFile(item.type)
        }
    }

    private fun saveViewToImageFile(type: String) {


        if (currentSelectView == null) {
            ToastUtils.show("你还没有选择要分享的目标")
            return
        }
        val checkView = currentSelectView?.findViewById<View>(R.id.iv_check)
        //隐藏不必要的view
        checkView?.hide()
        val bitmap = BitmapUtil.viewConversionBitmap(currentSelectView!!)
        checkView?.show()
        when (type) {
            ShareTypeEnum.FriendCircle -> {
                wxService?.weiXinShare(this, ShareObject().apply {
                    this.shareType = WeiXinShareType.WXImage
                    this.shareWay = ShareWayEnum.WXSceneTimeline
                    this.shareImage = bitmap
                })
            }
            ShareTypeEnum.WeChat -> {
                wxService?.weiXinShare(this, ShareObject().apply {
                    this.shareType = WeiXinShareType.WXImage
                    this.shareWay = ShareWayEnum.WXSceneSession
                    this.shareImage = bitmap
                })
            }
            ShareTypeEnum.Sina -> {
                wxService?.weiBoShare(this, ShareObject().apply {
                    this.shareType = WeiBoShareType.WbImage
                    this.shareImage = bitmap
                })
            }
            ShareTypeEnum.SaveImage -> {
                requestRWPermission {
                    Observable.just(bitmap).observeOn(Schedulers.io()).map { b ->
                        FileUtils.saveBitmapToDCIM(b, UUID.randomUUID().toString())
                    }.observeOn(AndroidSchedulers.mainThread()).subscribe {
                        if (it == true) {
                            ToastUtils.show("保存分享图片成功")

                        }
                    }


                }
            }


        }

    }

    private fun initViewModel() {
        mViewModel.sharePosters.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderViewData(it.getT())
            }
        })
        mViewModel.shares.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                shareAdapter.setNewInstance(it.getT())
            }
        })

    }

    private var currentCode: String = ""
    private fun renderViewData(posterInfo: SharePosterInfo) {
        sharePosterAdapter.setNewInstance(posterInfo.posterList)
        currentCode = posterInfo.inviteCode
        tv_invite_code.text = posterInfo.inviteCode
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

    override fun showLoadState(state: NetState) {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null)
            wxService?.weiBoShareResult(data)
    }

    private val sharePosterAdapter: BaseQuickAdapter<SharePoster, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<SharePoster, BaseViewHolder>(R.layout.item_invite_share) {

            override fun convert(holder: BaseViewHolder, item: SharePoster) {
                val sdvSharePic = holder.getView<SimpleDraweeView>(R.id.sdv_share_pic)
                val sdvQrCode = holder.getView<SimpleDraweeView>(R.id.sdv_qr_code)
                val sdvUserPic = holder.getView<SimpleDraweeView>(R.id.sdv_user_pic)

                sdvSharePic.loadImage(item.posterPic, 250f, 450f)
                sdvQrCode.loadImage(item.qrCode, 60f, 60f)
                sdvUserPic.loadImage(SessionUtils.getHeaderPic(), 45f, 45f)

                holder.setText(R.id.tv_user_name, SessionUtils.getNickName())

                if (item.inviteCode.isNotEmpty()) {
                    holder.setGone(R.id.tv_invite_code, false).setText(R.id.tv_invite_code, "邀请码${item.inviteCode}")
                } else {
                    holder.setGone(R.id.tv_invite_code, true)
                }
                val ivCheck = holder.getView<ImageView>(R.id.iv_check)

                ivCheck.isSelected = currentSelect?.posterId == item.posterId


            }
        }
    }

    private val shareAdapter: BaseQuickAdapter<ShareType, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<ShareType, BaseViewHolder>(R.layout.item_share) {

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


}