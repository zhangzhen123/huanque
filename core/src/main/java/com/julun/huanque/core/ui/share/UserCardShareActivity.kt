package com.julun.huanque.core.ui.share

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
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
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.bitmap.BitmapUtil
import com.julun.huanque.common.utils.device.PhoneUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.layoutmanager.stacklayout2.StackLayoutManager
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_user_card_share.*
import java.util.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2021/2/1 11:51
 *
 *@Description: UserCardShareActivity 用户卡片资料分享
 *
 */
@Route(path = ARouterConstant.USER_CARD_SHARE_ACTIVITY)
class UserCardShareActivity : BaseVMActivity<UserCardShareViewModel>() {
    companion object {
        //跳转密友页面的code
        const val RequestCode = 0x0001

        /**
         * @param userId 用户id
         */
        fun newInstance(act: Activity, userId: Long? = null) {
            val intent = Intent(act, UserCardShareActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                if (userId != null) {
                    intent.putExtra(ParamConstant.UserId, userId)
                }
                act.startActivity(intent)
            }
        }
    }


    private lateinit var mStackLayoutManager: StackLayoutManager
    private var currentSelectView: View? = null

    //分享目标类型(可选项：Banner、Room、InviteFriend、Other)
    private var mShareKeyType = ""

    //分享目标Id ，如果是直播间，则填直播间ID, 如果是邀友，则填邀友海报ID
    private var mShareKeyId = ""

    private var mUserId = 0L


    private val wxService: LoginAndShareService? by lazy {
        ARouter.getInstance().build(ARouterConstant.LOGIN_SHARE_SERVICE).navigation() as? LoginAndShareService
    }
    private val shareAdapter: ShareAdapter by lazy { ShareAdapter() }

    private val cardAdapter: UserCardShareAdapter by lazy { UserCardShareAdapter() }


    override fun getLayoutId(): Int = R.layout.activity_user_card_share


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        StatusBarUtil.setTransparent(this)
        mUserId = intent.getLongExtra(ParamConstant.UserId, 0)
        initViewModel()

        rv_share_types.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv_share_types.addItemDecoration(HorizontalItemDecoration(dp2px(20)))
        rv_share_types.adapter = shareAdapter

        playInAnimator()

        mStackLayoutManager = StackLayoutManager(StackLayoutManager.ScrollOrientation.RIGHT_TO_LEFT, 2)
        rv_share_contents.layoutManager = mStackLayoutManager
        rv_share_contents.adapter = cardAdapter


        ll_bottom.onClick { }
        mViewModel.queryCardInfo(mUserId)
        mViewModel.queryShareType()
    }

    private fun playInAnimator() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom)
        animation.duration = 300
        rv_share_contents.startAnimation(animation)
        ll_bottom.startAnimation(animation)
    }

    override fun initEvents(rootView: View) {

        root_share_view.onTouch { _, event ->
            finish()
            false
        }
        tv_cancel_share.onClickNew {
            finish()
        }
        mStackLayoutManager.setItemChangedListener(object : StackLayoutManager.ItemChangedListener {
            override fun onItemChanged(position: Int) {
                logger.info("onItemChanged pos=$position")
                tv_index.text="${position+1}/${cardAdapter.data.size}"
                currentSelectView = cardAdapter.getViewByPosition(position, R.id.card_container)
            }
        })
        shareAdapter.onAdapterClickNew { _, _, position ->

            val item = shareAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            //分享图片
            saveViewToImageFile(item.type)

        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    private fun saveViewToImageFile(type: String) {
        var bitmap: Bitmap? = null
        if (currentSelectView == null) {
            ToastUtils.show("你还没有选择要分享的目标")
            return
        }
        currentSelectView?.post {
            bitmap = BitmapUtil.viewConversionBitmap(currentSelectView!!, Color.TRANSPARENT)
            shareImage(type, bitmap ?: return@post)
        }
    }

    /**
     * 分享图片
     */
    private fun shareImage(type: String, bitmap: Bitmap) {
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
                        this.userId = mUserId
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
                        this.userId = mUserId
                    })
                    finish()
                }
            }
            ShareTypeEnum.Sina -> {
                wxService?.weiBoShare(this, ShareObject().apply {
                    this.shareImage = bitmap
                    this.shareType = WeiBoShareType.WbImage
                    //以下是应用内传递数据使用
                    this.platForm = ShareTypeEnum.Sina
                    this.shareKeyType = mShareKeyType
                    this.shareKeyId = mShareKeyId
                    this.userId = mUserId
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



    private fun initViewModel() {
        mViewModel.shares.observe(this, Observer {
            if (it.isSuccess()) {
                shareAdapter.setNewInstance(it.requireT())
            }
        })
        mViewModel.userCardInfo.observe(this, Observer {
            if (it.isSuccess()) {
                //todo test
                cardAdapter.info = it.requireT()
                cardAdapter.setList(it.requireT().bitmaps)
                rv_share_contents.post {
                    tv_index.text="1/${cardAdapter.data.size}"
                    currentSelectView = cardAdapter.getViewByPosition(0, R.id.card_container)
                }
            }
        })

    }

    private fun renderViewData(posterInfo: SharePosterInfo) {

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