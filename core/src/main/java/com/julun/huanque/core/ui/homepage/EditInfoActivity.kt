package com.julun.huanque.core.ui.homepage

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.BottomActionDialog
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.UpdateUserInfoForm
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.EditPicAdapter
import com.julun.huanque.core.adapter.HomePageTagAdapter
import com.julun.huanque.core.ui.record_voice.VoiceSignActivity
import com.julun.huanque.core.viewmodel.EditInfoViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.trello.rxlifecycle4.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.act_edit_info.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.textColor
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 *@创建者   dong
 *@创建时间 2020/12/29 19:17
 *@描述 编辑资料页面
 */
class EditInfoActivity : BaseActivity() {

    private val mEditInfoViewModel: EditInfoViewModel by viewModels()

    //进度条的总宽度
    private val TOTAL_PROGRESS_WIDTH = ScreenUtils.getScreenWidth() - dp2px(141f)

    //标签adapter
    private val mTagAdapter = HomePageTagAdapter()

    //我喜欢的标签adapter
    private val mLikeTagAdapter = HomePageTagAdapter()

    //图片Adapter
    private val mEditPicAdapter = EditPicAdapter()

    //社交意愿弹窗
    private val mEditSocialWishFragment: EditSocialWishFragment by lazy { EditSocialWishFragment() }

    private var mBarHeiht = 0

    private val mLoadingDialog: LoadingDialog by lazy { LoadingDialog(this) }

    //
    private var currentPicBean: HomePagePicBean? = null

    private var currentAction: String = ""

    private var bottomDialog: BottomActionDialog? = null

    private val bottomDialogListener: BottomActionDialog.OnActionListener by lazy {
        object : BottomActionDialog.OnActionListener {
            override fun operate(action: BottomAction) {

                val picBean = currentPicBean ?: return
                when (action.code) {
                    BottomActionCode.DELETE -> {
                        mEditInfoViewModel.updateCover(picBean.logId, null)
                    }
                    BottomActionCode.REPLACE -> {
                        currentAction = BottomActionCode.REPLACE
                        checkPicPermissions()
                    }
                    BottomActionCode.REPLACE_HEAD -> {
                        currentAction = BottomActionCode.REPLACE_HEAD
                        checkPicPermissions()
                    }
                    BottomActionCode.AUTH_HEAD -> {
                        //
                        //                        MyAlertDialog(this@EditInfoActivity).showAlertWithOKAndCancel(
//                            "通过人脸识别技术确认照片为真人将获得认证标识，提高交友机会哦~",
//                            MyAlertDialog.MyDialogCallback(onRight = {
//                            }), "真人照片未认证", okText = "去认证", noText = "取消"
//                        )
                        (ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
                            .navigation() as? IRealNameService)?.checkRealHead { e ->
                            if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
                                MyAlertDialog(this@EditInfoActivity, false).showAlertWithOKAndCancel(
                                    e.busiMessage.toString(),
                                    title = "修改提示",
                                    okText = "修改头像",
                                    noText = "取消",
                                    callback = MyAlertDialog.MyDialogCallback(onRight = {
                                        currentAction = BottomActionCode.REPLACE_HEAD
                                        checkPicPermissions()
                                    })
                                )

                            }
                        }


                    }

                }
            }
        }
    }

    //图片示例Fragment
    private val mPicDemoFragment = PicDemoFragment()

    override fun isRegisterEventBus() = true

    override fun getLayoutId() = R.layout.act_edit_info

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mBarHeiht = StatusBarUtil.getStatusBarHeight(this)
        header_page.textTitle.text = "编辑资料"
        header_page.textOperation.show()
        header_page.textOperation.text = "保存"
        header_page.textOperation.isEnabled = false
        initRecyclerView()
        initViewModel()
        mEditInfoViewModel.getBasicInfo()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
        header_page.textOperation.onClickNew {
            //保存
        }
        tv_nickname_title.onClickNew {
            //昵称
            UpdateNicknameActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.nickname ?: "")
        }
        tv_sign_title.onClickNew {
            UpdateSignActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.mySign ?: "")
        }
        tv_voice_title.onClickNew {
            val intent = Intent(this, VoiceSignActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                mEditInfoViewModel.needFresh = true
                startActivity(intent)
            }
        }
        iv_close_progress.onClickNew {
            //隐藏资料完成度布局
            con_progress.hide()
        }

        tv_profression_title.onClickNew {
            //选择家乡
            val info = mEditInfoViewModel.basicInfo.value ?: return@onClickNew
            val homeTownStr = StringBuilder()
            if (info.homeTown.homeTownProvince.isNotEmpty()) {
                homeTownStr.append(info.homeTown.homeTownProvince)
            }
            if (homeTownStr.isNotEmpty()) {
                homeTownStr.append("/")
            }
            homeTownStr.append(info.homeTown.homeTownCity)
            HomeTownActivity.newInstance(this, homeTownStr.toString())
        }
        tv_age_constellation_title.onClickNew {
            //年龄  星座
            val basicInfo = mEditInfoViewModel.basicInfo.value ?: return@onClickNew
            UpdateBirthdayActivity.newInstance(this, basicInfo.birthday)
        }

        tv_school_title.onClickNew {
            //学校
            SchoolActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.schoolInfo ?: return@onClickNew)
        }
        tv_job_title.onClickNew {
            ProfessionActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.profession ?: return@onClickNew)
        }
        tv_social_wish_title.onClickNew {
            //社交意愿
            mEditSocialWishFragment.show(supportFragmentManager, "EditSocialWishFragment")
        }
        mEditPicAdapter.setOnItemClickListener { adapter, view, position ->
            val tempBean = mEditPicAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            currentPicBean = tempBean
            if (tempBean.coverPic.isEmpty()) {
                //空白位置,进入选择图片页面
                currentAction = BottomActionCode.ADD
                checkPicPermissions()
            } else {
                showBottomDialog(tempBean.headerPic == BooleanType.TRUE)
            }
        }
        iv_demo.onClickNew {
            mPicDemoFragment.show(supportFragmentManager, "PicDemoFragment")
        }
        tv_figure_title.onClickNew {
            FigureActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.figure ?: return@onClickNew)
        }

    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mEditInfoViewModel.basicInfo.observe(this, Observer {
            showViewByData(it ?: return@Observer)
        })
        mEditInfoViewModel.wishData.observe(this, Observer {
            if (it != null) {
                showWishContent(it)
            }
        })
        mEditInfoViewModel.updateHeadResult.observe(this, Observer {
            if (it != null) {
                if (it.isSuccess()) {
                    //头像修改成功 直接刷新整个页面
                    mEditInfoViewModel.getBasicInfo()
                } else {
                    val error = it.error
                    if (error?.busiCode == ErrorCodes.USER_LOSE_REAL_HEAD) {
                        MyAlertDialog(this@EditInfoActivity, false).showAlertWithOKAndCancel(
                            error.busiMessage.toString(),
                            title = "修改提示",
                            okText = "修改头像",
                            noText = "确定使用",
                            callback = MyAlertDialog.MyDialogCallback(onRight = {
                                currentAction = BottomActionCode.REPLACE_HEAD
                                checkPicPermissions()
                            }, onCancel = {
                                mEditInfoViewModel.updateHeadPic(
                                    headPic = mEditInfoViewModel.currentHeadPic,
                                    check = BooleanType.FALSE
                                )
                            })
                        )

                    }
                }
            }
        })
//        mEditInfoViewModel.processData.observe(this, Observer {
//            if (it != null) {
//                updateProgress(it.perfection)
//            }
//        })

    }

    private fun showBottomDialog(isHead: Boolean) {
        val actions = arrayListOf<BottomAction>()
        if (isHead) {
            actions.add(BottomAction(BottomActionCode.REPLACE_HEAD, "更换头像"))
            if (currentPicBean?.realPic != BooleanType.TRUE) {
                actions.add(BottomAction(BottomActionCode.AUTH_HEAD, "认证头像"))
            }
        } else {
            actions.add(BottomAction(BottomActionCode.REPLACE, "更换"))
            actions.add(BottomAction(BottomActionCode.DELETE, "删除"))
        }
        actions.add(BottomAction(BottomActionCode.CANCEL, "取消"))
        BottomActionDialog.create(bottomDialog, actions, bottomDialogListener).show(this, "BottomActionDialog")
    }

    /**
     * 显示数据
     */
    private fun showViewByData(info: EditPagerInfo) {
        updateProgress(info.perfection)

        //显示图片相关
        val pics = info.picList
        //用来显示的数据
        val showPics = mutableListOf<HomePagePicBean>()
        showPics.add(HomePagePicBean(info.headPic, info.authMark, headerPic = BusiConstant.True))
        pics.forEach {
            showPics.add(it)
        }
        if (showPics.size < 9) {
            (0 until (9 - showPics.size)).forEach { _ ->
                showPics.add(HomePagePicBean())
            }
        }

        mEditPicAdapter.setList(showPics)

        val normalColor = GlobalUtils.getColor(R.color.black_333)
        val greyColor = GlobalUtils.getColor(R.color.black_999)
        tv_nickname.text = info.nickname
        //签名
        val sign = info.mySign
        if (sign.isEmpty()) {
            tv_sign.text = "编辑个签，展示我的独特态度"
            tv_sign.textColor = greyColor
        } else {

            tv_sign.textColor = normalColor
            if (sign.length <= 20) {
                tv_sign.text = sign
            } else {
                tv_sign.text = sign.substring(1, 20).plus("...")
            }
        }

        //语音签名
        val voiceBean = info.voice
        if (voiceBean.voiceUrl.isEmpty() && voiceBean.voiceStatus.isEmpty()) {
            //未录制过音频
            tv_voice.text = "把你唱给Ta听"
            tv_voice.textColor = greyColor
        } else {
            //录制过音频
            tv_voice.textColor = greyColor
            when (voiceBean.voiceStatus) {
                VoiceBean.Wait -> {
                    //等待审核
                    tv_voice.text = "等待审核"
                }
                VoiceBean.Reject -> {
                    //被拒绝
                    tv_voice.text = "审核不通过"
                }
                VoiceBean.Pass -> {
                    //审核通过
                    tv_voice.text = "${info.voice.length}秒录音"
                }
            }

        }

        //基本资料
        val homeTownStr = StringBuilder()
        if (info.homeTown.homeTownProvince.isNotEmpty()) {
            homeTownStr.append(info.homeTown.homeTownProvince)
        }
        if (homeTownStr.isNotEmpty()) {
            homeTownStr.append("/")
        }
        homeTownStr.append(info.homeTown.homeTownCity)
        tv_profression.text = homeTownStr.toString()

        //年龄和星座
        val ageConstell = StringBuilder()
        ageConstell.append(info.age)
        ageConstell.append("/")
        ageConstell.append(info.constellationInfo.constellationName)
        tv_age_constellation.text = ageConstell.toString()

        //身材
        val weight = info.figure.weight
        val height = info.figure.height
        val whBuilder = StringBuilder()
        if (height > 0) {
            whBuilder.append("${height}cm")
        }
        if (weight > 0) {
            if (height > 0) {
                whBuilder.append("/")
            }
            whBuilder.append("${weight}kg")
        }
        tv_figure.text = whBuilder.toString()

        tv_school.text = info.schoolInfo.school

        tv_job.text = "${info.profession.professionTypeText}/${info.profession.professionName}"


        val tagList = info.myAuthTag.showTagList
        tv_tag_count.text = "${info.myAuthTag.markTagNum}"
        val realTagList = mutableListOf<HomeTagBean>()
        if (tagList.size >= 4) {
            tv_more_tag.show()
            tagList.take(4).forEach {
                realTagList.add(it)
            }
        } else {
            tv_more_tag.hide()
            realTagList.addAll(tagList)
        }
        mTagAdapter.setList(realTagList)

        val likeTagList = info.myLikeTag.showTagList
        tv_like_tag_count.text = "${info.myLikeTag.markTagNum}"
        val realLikeTagList = mutableListOf<HomeTagBean>()
        if (likeTagList.size >= 4) {
            tv_more_like_tag.show()
            likeTagList.take(4).forEach {
                realLikeTagList.add(it)
            }
        } else {
            tv_more_like_tag.hide()
            realLikeTagList.addAll(likeTagList)
        }
        mLikeTagAdapter.setList(realLikeTagList)

        tv_user_id.text = "欢鹊ID ${info.userId}"
    }

    /**
     * 显示社交意愿数据
     */
    private fun showWishContent(wishList: MutableList<SocialWishBean>) {
        val wishStr = StringBuilder()
        wishList.forEach {
            if (wishStr.isNotEmpty()) {
                wishStr.append("/")
            }
            wishStr.append(it.wishTypeText)
        }
        if (wishStr.length <= 20) {
            tv_social_wish.text = wishStr.toString()
        } else {
            tv_social_wish.text = wishStr.substring(0, 20).plus("...")
        }
    }

    /**
     * 设置进度条宽度
     */
    private fun updateProgress(progress: Int) {
        view_progress_placeholder
        progressBar.progress = progress
        val placeViewWidtth = TOTAL_PROGRESS_WIDTH * progress / 100
        val placeParams = view_progress_placeholder.layoutParams
        placeParams.width = max(placeViewWidtth, 1)
        view_progress_placeholder.layoutParams = placeParams
        tv_progress.text = "${progress}%"
        tv_progress.postInvalidate()
    }


    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        //图片
        recycler_view_pic.layoutManager = GridLayoutManager(this, 3)
        recycler_view_pic.adapter = mEditPicAdapter


        val callback = object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                val swipeFlags = 0
                return makeMovementFlags(dragFlags, swipeFlags)
//                return makeMovementFlags(0, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                //得到当拖拽的viewHolder的Positio n
                val datas = mEditPicAdapter.data
                //得到当拖拽的viewHolder的Position
                val fromPosition = viewHolder.adapterPosition
                //拿到当前拖拽到的item的viewHolder
                //拿到当前拖拽到的item的viewHolder
                val toPosition = target.adapterPosition
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(datas, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(datas, i, i - 1)
                    }
                }
                mEditPicAdapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun canDropOver(
                recyclerView: RecyclerView,
                current: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val tempData = mEditPicAdapter.getItemOrNull(target.adapterPosition) ?: return false

                // && tempData.pic.isNotEmpty()
                if (tempData.headerPic != BusiConstant.True) {
                    return true
                }
                return false
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }
        }
        val helper = ItemTouchHelper(callback)

        helper.attachToRecyclerView(recycler_view_pic)
        mEditPicAdapter.setOnItemLongClickListener { adapter, view, position ->
            val tempData = adapter.getItemOrNull(position) as? HomePagePicBean
                ?: return@setOnItemLongClickListener true
            // && tempData.pic.isNotEmpty()
            if (tempData.headerPic != BusiConstant.True) {
                helper.startDrag(recycler_view_pic.getChildViewHolder(view))
            } else {
                if (tempData.headerPic == BusiConstant.True) {
                    //头像 显示提示
                    tempData.showNoMoveAttention = BusiConstant.True
                    mEditPicAdapter.notifyItemChanged(position)
                    timerHideAttention()
                }

            }
            return@setOnItemLongClickListener true
        }

        recycler_view_tag.layoutManager = GridLayoutManager(this, 4)
        recycler_view_tag.adapter = mTagAdapter
        mTagAdapter.setOnItemClickListener { adapter, view, position ->
//            ll_tag.performClick()
        }

        recycler_view_like_tag.layoutManager = GridLayoutManager(this, 4)
        recycler_view_like_tag.adapter = mLikeTagAdapter

    }


    private var mHideDisposable: Disposable? = null

    /**
     * 定时隐藏提示效果
     */
    private fun timerHideAttention() {
        mHideDisposable?.dispose()
        mHideDisposable = Observable.timer(2, TimeUnit.SECONDS)
            .bindUntilEvent(this, Lifecycle.Event.ON_DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mEditPicAdapter.getItemOrNull(0)?.showNoMoveAttention = BusiConstant.False
                mEditPicAdapter.notifyItemChanged(0)
            }, {})
    }


    override fun onRestart() {
        super.onRestart()
        if (mEditInfoViewModel.needFresh) {
            mEditInfoViewModel.getBasicInfo()
            mEditInfoViewModel.needFresh = false
        }
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
                        //判断亲密特权
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
        val heightRatio = when (currentAction) {
            BottomActionCode.REPLACE_HEAD -> {
                1
            }
            else -> {
                2
            }
        }
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
            .withAspectRatio(1, heightRatio)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
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
                    OssUpLoadManager.COVER_POSITION
                ) { code, list ->
                    if (code == OssUpLoadManager.CODE_SUCCESS) {
                        logger("上传oss成功结果的：$list")
                        val first = list?.firstOrNull()
                        if (first != null) {
                            when (currentAction) {
                                BottomActionCode.REPLACE -> {
                                    if (currentPicBean != null) {
                                        mEditInfoViewModel.updateCover(currentPicBean!!.logId, coverPic = first)
                                    }
                                }
                                BottomActionCode.REPLACE_HEAD -> {
                                    mEditInfoViewModel.updateHeadPic(headPic = first, check = BooleanType.TRUE)
                                }
                                BottomActionCode.ADD -> {
                                    mEditInfoViewModel.updateCover(null, coverPic = first)
                                }
                            }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun userInfoUpdate(info: UpdateUserInfoForm) {
        val nickname = info.nickname
        if (nickname?.isNotEmpty() == true) {
            //昵称变动
            tv_nickname.text = nickname
            mEditInfoViewModel.basicInfo.value?.nickname = nickname
        }
        val sign = info.mySign
        if (sign?.isNotEmpty() == true) {
            //个性签名变动
            tv_sign.text = sign
            tv_sign.textColor = GlobalUtils.getColor(R.color.black_333)
            mEditInfoViewModel.basicInfo.value?.mySign = sign
        }
        val cityName = info.cityName
        val provinceName = info.provinceName
        if (cityName?.isNotEmpty() == true && provinceName?.isNotEmpty() == true) {
            //城市名称
            mEditInfoViewModel.basicInfo.value?.homeTown?.homeTownProvince = provinceName
            mEditInfoViewModel.basicInfo.value?.homeTown?.homeTownCity = cityName
            //更新文案
            val homeTownStr = StringBuilder()
            homeTownStr.append(info.provinceName)
            homeTownStr.append("/")
            homeTownStr.append(info.cityName)
            tv_profression.text = homeTownStr.toString()
        }
        val birthday = info.birthday
        if (birthday != null) {
            val birthDate = TimeUtils.string2Date("yyyy-MM-dd", birthday) ?: return
            val age = TimeUtils.getAgeByDate(birthDate)
            val constellationName = ConstellationUtils.getConstellation(birthDate).name
            if (constellationName.isNotEmpty()) {
                mEditInfoViewModel.basicInfo.value?.let {
                    it.birthday = birthday
                    it.age = age
                    it.constellationInfo.constellationName = constellationName
                }
                val ageConstell = StringBuilder()
                ageConstell.append(age)
                ageConstell.append("/")
                ageConstell.append(constellationName)
                tv_age_constellation.text = ageConstell.toString()
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun userProcesshange(bean: UserProcessBean) {
        mEditInfoViewModel.basicInfo.value?.perfection = bean.perfection
        updateProgress(bean.perfection)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun schoolChange(bean: SchoolInfo) {
        tv_school.text = bean.school
        mEditInfoViewModel.basicInfo.value?.schoolInfo?.let {
            if (bean.startYear.isNotEmpty()) {
                it.startYear = bean.startYear
            }
            if (bean.educationCode.isNotEmpty()) {
                it.educationCode = bean.educationCode
            }
            if (bean.education.isNotEmpty()) {
                it.education = bean.education
            }
            if (bean.school.isNotEmpty()) {
                it.school = bean.school
            }
        }
    }

    /**
     * 职业数据有变动
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun professChange(info: ProfessionInfo) {
        val profession = mEditInfoViewModel.basicInfo.value?.profession ?: return
        if (info.professionId != profession.professionId) {
            //只更新 职业ID，职业名称，行业名称3个字段(其余字段未使用)
            profession.professionId = info.professionId
            profession.professionTypeText = info.professionTypeText
            profession.professionName = info.professionName
            tv_job.text = "${profession.professionTypeText}/${profession.professionName}"
        }
    }

    /**
     * 身材数据变动
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun figureChange(info: FigureBean) {
        val figureBean = mEditInfoViewModel.basicInfo.value?.figure ?: return
        if (info.weight != 0 && info.height != 0) {
            //数据变动
            figureBean.weight = info.weight
            figureBean.height = info.height

            //身材
            val weight = info.weight
            val height = info.height
            val whBuilder = StringBuilder()
            if (height > 0) {
                whBuilder.append("${height}cm")
            }
            if (weight > 0) {
                if (height > 0) {
                    whBuilder.append("/")
                }
                whBuilder.append("${weight}kg")
            }
            tv_figure.text = whBuilder.toString()
        }
    }


}