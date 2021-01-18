package com.julun.huanque.core.ui.tag_manager

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.base.dialog.BottomActionDialog
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.AuthPic
import com.julun.huanque.common.bean.beans.AuthTagPicInfo
import com.julun.huanque.common.bean.beans.BottomAction
import com.julun.huanque.common.constant.BottomActionCode
import com.julun.huanque.common.constant.ManagerTagCode
import com.julun.huanque.common.constant.TagPicAuthStatus
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import com.julun.huanque.core.widgets.discreteview.DSVOrientation
import com.julun.huanque.core.widgets.discreteview.util.transform.Pivot
import com.julun.huanque.core.widgets.discreteview.util.transform.ScaleTransformer
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.android.synthetic.main.activity_tag_auth_pic.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.startActivity

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2021/1/11 14:19
 *
 *@Description: AuthTagPicActivity 认证标签图片
 *
 */
class AuthTagPicActivity : BaseVMActivity<AuthTagPicViewModel>() {

    companion object {
        /**
         * [isMe]是不是我自己的主页
         * [isMyLike]是我喜欢的标签还是我拥有的标签
         * [isSameSex]该用户与本人是否同性
         *
         */
        fun start(act: Activity, tagId: Int, isMe: Boolean, isMyLike: Boolean, isSameSex: Boolean) {
            act.startActivity<AuthTagPicActivity>(
                ManagerTagCode.TAG_INFO to tagId,
                "isMyLike" to isMyLike,
                "isMe" to isMe,
                "isSameSex" to isSameSex
            )
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_tag_auth_pic

    //认证失败弹窗
    private var mTagAuthFailFragment: TagAuthFailFragment? = null

    private val mLoadingDialog: LoadingDialog by lazy { LoadingDialog(this) }
    private var bottomDialog: BottomActionDialog? = null
    private val bottomDialogListener: BottomActionDialog.OnActionListener by lazy {
        object : BottomActionDialog.OnActionListener {
            override fun operate(action: BottomAction) {
                val current = currentItem ?: return
                when (action.code) {
                    BottomActionCode.DELETE -> {
                        MyAlertDialog(this@AuthTagPicActivity, false).showAlertWithOKAndCancel(
                            "该标签照片已经通过认证，删除后再次上传将重新认证",
                            title = "确认删除该照片？",
                            okText = "删除",
                            noText = "再想想",
                            callback = MyAlertDialog.MyDialogCallback(onRight = {
                                mViewModel.deleteTagPic(current.logId)
                            })
                        )

                    }

                }
            }
        }
    }

    //    private val tagManagerViewModel: TagManagerViewModel = HuanViewModelManager.tagManagerViewModel
    private var currentTagId: Int? = null

    private var isMe: Boolean = false

    //是不是同性
    private var isSameSex: Boolean = false

    //来自我喜欢的标签
    private var isMyLike: Boolean = false

    private val addPicAdapter: BaseQuickAdapter<AuthPic, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<AuthPic, BaseViewHolder>(R.layout.item_tag_add_pic) {
            init {
                addChildClickViewIds(R.id.tv_fail_reason)
            }

            override fun convert(holder: BaseViewHolder, item: AuthPic) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.sdv_add_img)
                val tv_verify_status = holder.getView<TextView>(R.id.tv_verify_status)
                holder.setGone(R.id.iv_auth_success, true)
                if (item.logId == -1) {
                    sdv.loadImageLocal(R.mipmap.bg_tag_add_default)
                    holder.setVisible(R.id.rl_status, true)
                        .setGone(R.id.tv_fail_reason, true)
                    tv_verify_status.hide()
                    holder.setText(R.id.tv_add_pic, "上传照片认证")
                } else {
                    sdv.loadImage(item.applyPic, 190f, 250f)
                    when (item.auditStatus) {
                        TagPicAuthStatus.Wait -> {
                            holder.setGone(R.id.rl_status, true)
                            tv_verify_status.show()
                            tv_verify_status.backgroundResource = R.drawable.bg_shape_auth_tag_ing
                            tv_verify_status.text = "审核中"
                        }
                        TagPicAuthStatus.Reject -> {
                            holder.setGone(R.id.rl_status, false)
                            holder.setText(R.id.tv_add_pic, "重新上传")
                            holder.setText(R.id.tv_fail_reason, "原因：" + item.auditReason).setVisible(R.id.tv_fail_reason, true)
                            tv_verify_status.show()
                            tv_verify_status.backgroundResource = R.drawable.bg_shape_auth_tag_error
                            tv_verify_status.text = "审核失败"
                        }
                        TagPicAuthStatus.Pass -> {
                            holder.setGone(R.id.rl_status, true)
                            tv_verify_status.hide()
                            holder.setVisible(R.id.iv_auth_success, true)
                        }
                    }
                }

            }

        }
    }
    private val otherPicsAdapter: BaseQuickAdapter<String, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_tag_other_pic_list) {
            override fun convert(holder: BaseViewHolder, item: String) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.tag_img)
                sdv.loadImage(item, 110f, 148f)
            }

        }
    }
    private var currentItem: AuthPic? = null

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        currentTagId = intent.extras?.getInt(ManagerTagCode.TAG_INFO)
        isSameSex = intent.extras?.getBoolean("isSameSex", false) ?: false
        isMyLike = intent.extras?.getBoolean("isMyLike", false) ?: false
        isMe = intent.extras?.getBoolean("isMe", false) ?: false

        header_page_view.initHeaderView(titleTxt = "我认证的", operateTxt = "上传")
        header_page_view.imageViewBack.onClickNew {
            finish()
        }
        header_page_view.textOperation.onClickNew {
            checkPicPermissions()
        }
        iv_zan.onClickNew {
            val reTagDetail = mViewModel.tagDetail.value ?: return@onClickNew
            if (reTagDetail.isSuccess()) {
                val tagDetail = reTagDetail.getT() ?: return@onClickNew
                val priseCount = tagDetail.praiseNum
                if (priseCount > 0) {
                    ToastUtils.show("你认证的${tagDetail.tagName}已获得${priseCount}个赞")
                } else {
                    ToastUtils.show("你认证的${tagDetail.tagName}还没获得赞哦")

                }
            }

        }
        rv_add_pics.setOrientation(DSVOrientation.HORIZONTAL)
        rv_add_pics.setItemTransitionTimeMillis(150)
        rv_add_pics.setItemTransformer(
            ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .setPivotY(Pivot.Y.BOTTOM.create())
                .build()
        )

        rv_add_pics.adapter = addPicAdapter
        rv_add_pics.addOnItemChangedListener { _, adapterPosition ->
            logger.info("adapterPosition=$adapterPosition")
            val item = addPicAdapter.getItemOrNull(adapterPosition) ?: return@addOnItemChangedListener

            if (item.logId == -1) {
                tv_pic_index.text = "认证成功后欢鹊会第一时间帮你告诉TA"
            } else {
                tv_pic_index.text = "${adapterPosition + 1}/${addPicAdapter.data.size - 1}"
            }
        }
        addPicAdapter.onAdapterClickNew { _, view, position ->
            val item = addPicAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            currentItem = item
            when {
                item.logId == -1 -> {
                    checkPicPermissions()
                }
                item.auditStatus == TagPicAuthStatus.Reject -> {
                    checkPicPermissions()
                }
                item.auditStatus == TagPicAuthStatus.Pass -> {
                    //正常通过的图片操作 删除
                    val actions = arrayListOf<BottomAction>()
                    actions.add(BottomAction(BottomActionCode.DELETE, "删除"))
                    actions.add(BottomAction(BottomActionCode.CANCEL, "取消"))
                    BottomActionDialog.create(bottomDialog, actions, bottomDialogListener).show(this, "BottomActionDialog")
                }
            }
        }
        addPicAdapter.setOnItemChildClickListener { adapter, view, position ->
            val tempData = addPicAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener
            when (view.id) {
                R.id.tv_fail_reason -> {
                    //认证失败
                    mTagAuthFailFragment = TagAuthFailFragment.newInstance(tempData.auditReason)
                    mTagAuthFailFragment?.show(supportFragmentManager, "TagAuthFailFragment")
                }
                R.id.tv_add_pic -> {
                    logger.info("添加图片")
                }
                R.id.sdv_add_img -> {
                    logger.info("添加image")
                }
                else -> {
                }
            }
        }
        initViewModel()

//        rv_other_tag_pics.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv_other_tag_pics.layoutManager = GridLayoutManager(this, 3)
        rv_other_tag_pics.adapter = otherPicsAdapter
        rv_other_tag_pics.addItemDecoration(HorizontalItemDecoration(dp2px(8)))

    }

    private var detailInfo: AuthTagPicInfo? = null
    private fun initViewModel() {

        mViewModel.tagDetail.observe(this, Observer {
            if (it.isSuccess()) {
                detailInfo = it.requireT()
                renderData(it.requireT())
            }
        })
        mViewModel.applyResult.observe(this, Observer {
            if (it.isSuccess()) {
                val res = it.requireT()
                var isReplace = false
                kotlin.run {
                    addPicAdapter.data.forEachIndexed { index, pic ->
                        //需要替换的logid
                        val deleteLogId = res.deleteLogId
                        if (deleteLogId > 0) {
                            if (deleteLogId == pic.logId) {
                                isReplace = true
                                pic.applyPic = res.applyPic
                                pic.auditStatus = res.auditStatus
                                pic.auditReason = res.auditReason
                                pic.tagId = res.tagId
                                pic.logId = res.logId
                                addPicAdapter.notifyItemChanged(index)
                                return@run
                            }
                        } else {
                            if (pic.logId == res.logId) {
                                isReplace = true
                                pic.applyPic = res.applyPic
                                pic.auditStatus = res.auditStatus
                                pic.auditReason = res.auditReason
                                pic.tagId = res.tagId
                                addPicAdapter.notifyItemChanged(index)
                                return@run
                            }
                        }
                    }

                }
                //如果不是重新上传 这里就是新增
                if (!isReplace) {
                    val last = addPicAdapter.data.lastOrNull()
                    if (last != null) {
                        //一定要带上logId
                        last.logId = res.logId
                        last.applyPic = res.applyPic
                        last.auditStatus = res.auditStatus
                        last.auditReason = res.auditReason
                        last.tagId = res.tagId

                        addPicAdapter.notifyItemChanged(addPicAdapter.data.indexOf(last))
                    }
                    addPicAdapter.addData(AuthPic(logId = -1))
                }
            }
        })
        mViewModel.deleteResult.observe(this, Observer {
            if (it.isSuccess()) {
                val res = it.requireT()
                var rm: AuthPic? = null
                kotlin.run {
                    addPicAdapter.data.forEachIndexed { index, pic ->
                        if (pic.logId == res) {
                            rm = pic
                            return@run
                        }

                    }

                }

                if (rm != null) {
                    addPicAdapter.remove(rm!!)
                }
                addPicAdapter.notifyDataSetChanged()
            }
        })


        currentTagId ?: return
        mViewModel.authDetail(QueryType.INIT, tagId = currentTagId!!/*, friendId = currentLikeUserId!!*/)
    }

    private fun renderData(info: AuthTagPicInfo) {

        //
        sdv_tag.loadImage(info.tagIcon, 14f, 14f)
        tv_tag.text = "${info.tagName}"
        if (isMe) {
            top_tips.hide()
        } else {
            top_tips.show()
            if (info.auth) {
                if (!isSameSex && isMyLike) {
                    tv_top_left.text = "你拥有TA喜欢"
                    tv_top_right.text = "快通过私信告诉TA吧～"

                } else if (isSameSex && !isMyLike) {
                    tv_top_left.text = "你也拥有"
                    tv_top_right.text = "快通过私信告诉TA吧～"
                } else {
                    top_tips.hide()
                }
            } else {
                if (!isSameSex && isMyLike) {
                    tv_top_left.text = "你还没有TA喜欢的"
                    tv_top_right.text = "快上传照片认证吧～"

                } else if (isSameSex && !isMyLike) {
                    tv_top_left.text = "你还没有"
                    tv_top_right.text = "快上传照片认证吧～"
                } else {
                    top_tips.hide()
                }

            }


        }

        header_page_view.textTitle.text = "我认证的${info.tagName}"
        tv_zan_num.text = "${info.praiseNum}"
        tv_desc.text = info.tagDesc

        val adList = info.authPicList
        //logId=-1代表无图片
        adList.add(AuthPic(logId = -1))
        addPicAdapter.setList(adList)

        otherPicsAdapter.setList(info.otherAuthPicList)
    }

    /**
     * 检查图片权限
     */
    private fun checkPicPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
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
            .enableCrop(true)// 是否裁剪
            .withAspectRatio(3, 4)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
            .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
            .isDragFrame(false)
            .compress(true)// 是否压缩
            .synOrAsy(true)//同步true或异步false 压缩 默认同步
            //.compressSavePath(getPath())//压缩图片保存地址
            .glideOverride(120, 120)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
            .isGif(false)// 是否显示gif图片
//                    .selectionMedia(selectList)// 是否传入已选图片
            .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
            //.cropCompressQuality(90)// 裁剪压缩质量 默认100
            .minimumCompressSize(100)// 小于100kb的图片不压缩
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
        if (requestCode == PictureConfig.CHOOSE_REQUEST) {
            val selectList = PictureSelector.obtainMultipleResult(data)
            for (media in selectList) {
                Log.i("图片-----》", media.path)
            }
            startUploadImages(selectList)
        }
    }

    private fun startUploadImages(selectList: List<LocalMedia>) {
        try {
            if (selectList.isNotEmpty()) {
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
                if (!mLoadingDialog.isShowing) {
                    mLoadingDialog.showDialog()
                }
                OssUpLoadManager.uploadFiles(
                    pathList,
                    OssUpLoadManager.APPLY_POSITION
                ) { code, list ->
                    if (code == OssUpLoadManager.CODE_SUCCESS) {
                        logger("上传oss成功结果的：$list")
                        val first = list?.firstOrNull()
                        val tagId = currentTagId
                        if (first != null && tagId != null) {
                            val logId = if (currentItem?.logId == -1) {
                                null
                            } else {
                                currentItem?.logId
                            }
                            mViewModel.applyTagPic(tagId, first, logId)
                        }


                    } else {
                        ToastUtils.show("上传失败，请稍后重试")

                    }
                    if (mLoadingDialog.isShowing) {
                        mLoadingDialog.dismiss()
                    }

                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger("图片返回出错了")
        }

    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {
                state_pager_view.showSuccess()
            }
            NetStateType.LOADING -> {
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(btnClick = View.OnClickListener {
                    mViewModel.authDetail(
                        QueryType.INIT,
                        tagId = currentTagId ?: return@OnClickListener/*, friendId = currentLikeUserId!!*/
                    )
                })
            }

        }


    }
}