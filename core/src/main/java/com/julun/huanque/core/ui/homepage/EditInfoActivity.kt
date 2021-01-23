package com.julun.huanque.core.ui.homepage

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.BottomActionDialog
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.InviteCompleteForm
import com.julun.huanque.common.bean.forms.UpdateUserInfoForm
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.EditPicAdapter
import com.julun.huanque.core.adapter.HomePageTagAdapter
import com.julun.huanque.core.ui.record_voice.VoiceSignActivity
import com.julun.huanque.core.ui.tag_manager.AuthTagPicActivity
import com.julun.huanque.core.ui.tag_manager.MyTagsActivity
import com.julun.huanque.core.ui.tag_manager.TagPicsActivity
import com.julun.huanque.core.utils.EditUtils
import com.julun.huanque.core.viewmodel.EditInfoViewModel
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
@Route(path = ARouterConstant.EDIT_INFO_ACTIVITY)
class EditInfoActivity : BaseActivity() {

    private val mEditInfoViewModel: EditInfoViewModel by viewModels()

    //进度条的总宽度
    private var TOTAL_PROGRESS_WIDTH = ScreenUtils.getScreenWidth() - dp2px(155f)

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

    //语音状态弹窗
    private val mVoiceStatusFragment: VoiceStatusFragment by lazy { VoiceStatusFragment() }

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
                        mEditInfoViewModel.needFresh = true
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
        if (SPUtils.getBoolean(SPParamKey.First_Edit, true)) {
            iv_demo.performClick()
            SPUtils.commitBoolean(SPParamKey.First_Edit, false)
        }
        progressBar.post { }
        mTagAdapter.mine = true
        mLikeTagAdapter.mine = true
        mLikeTagAdapter.like = true

        mBarHeiht = StatusBarUtil.getStatusBarHeight(this)
        header_page.textTitle.text = "编辑资料"
        header_page.textOperation.show()
        header_page.textOperation.text = "保存"
        header_page.textOperation.isEnabled = true
        initRecyclerView()
        initViewModel()
        mEditInfoViewModel.getBasicInfo()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            onBackPressed()
        }
        header_page.textOperation.onClickNew {
            //保存
            val picSb = StringBuilder()
            mEditPicAdapter.data.forEach {
                if (it.headerPic != BusiConstant.True && it.logId != 0L) {
                    if (picSb.isNotEmpty()) {
                        picSb.append(",")
                    }
                    picSb.append("${it.logId}")
                }
            }
            if (picSb.toString() != mEditInfoViewModel.getOriginPicStr()) {
                //数据发生变化，需要请求接口
                mEditInfoViewModel.saveConverOrder(picSb.toString())
            } else {
                finish()
            }
        }
        tv_nickname_title.onClickNew {
            //昵称
            if (mEditInfoViewModel.basicInfo.value?.userType == UserType.Anchor) {
                ToastUtils.show("主播修改昵称请联系官方")
                return@onClickNew
            }
            UpdateNicknameActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.nickname ?: "")
        }
        tv_sign_title.onClickNew {
            UpdateSignActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.mySign ?: "")
        }
        tv_voice_title.onClickNew {
            val voiceBean = mEditInfoViewModel.basicInfo.value?.voice ?: return@onClickNew
            val status = voiceBean.voiceStatus
            if (status.isEmpty() && voiceBean.voiceUrl.isEmpty()) {
                val intent = Intent(this, VoiceSignActivity::class.java)
                if (ForceUtils.activityMatch(intent)) {
                    mEditInfoViewModel.needFresh = true
                    startActivity(intent)
                }
                return@onClickNew
            }
            if (voiceBean.voiceStatus == VoiceBean.Wait) {
                //审核中
                ToastUtils.show("语音签名正在审核中")
            } else {
                mVoiceStatusFragment.show(supportFragmentManager, "VoiceStatusFragment")
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
            val needJump = getNeedJumpOrder(HomeTownActivity::class.java)
            if (needJump) {
                EditUtils.jumpActivity(this, 0)
            } else {
                HomeTownActivity.newInstance(this, homeTownStr.toString())
            }
        }
        tv_age_constellation_title.onClickNew {
            //年龄  星座
            val basicInfo = mEditInfoViewModel.basicInfo.value ?: return@onClickNew

            val needJump = getNeedJumpOrder(UpdateBirthdayActivity::class.java)
            if (needJump) {
                EditUtils.jumpActivity(this, 0)
            } else {
                UpdateBirthdayActivity.newInstance(this, basicInfo.birthday)
            }

        }

        tv_figure_title.onClickNew {
            val figureBean = mEditInfoViewModel.basicInfo.value?.figure ?: return@onClickNew
            val needJump = getNeedJumpOrder(FigureActivity::class.java)
            if (needJump) {
                EditUtils.jumpActivity(this, 0)
            } else {
                FigureActivity.newInstance(this, figureBean)
            }
        }

        tv_school_title.onClickNew {
            //学校
            val schoolInfo = mEditInfoViewModel.basicInfo.value?.schoolInfo ?: return@onClickNew

            val needJump = getNeedJumpOrder(SchoolActivity::class.java)
            if (needJump) {
                EditUtils.jumpActivity(this, 0)
            } else {
                SchoolActivity.newInstance(this, schoolInfo)
            }

        }
        tv_job_title.onClickNew {
            val profession = mEditInfoViewModel.basicInfo.value?.profession ?: return@onClickNew
            val needJump = getNeedJumpOrder(ProfessionActivity::class.java)
            if (needJump) {
                EditUtils.jumpActivity(this, 0)
            } else {
                ProfessionActivity.newInstance(this, profession)
            }
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

        ll_tag.onClickNew {
            mEditInfoViewModel.needFresh = true
            MyTagsActivity.start(this, MyTagType.AUTH)
        }
        ll_like_tag.onClickNew {
            mEditInfoViewModel.needFresh = true
            MyTagsActivity.start(this, MyTagType.LIKE)
        }
    }

    /**
     * 获取是否需要顺序跳转
     */
    private fun getNeedJumpOrder(curClass: Class<out BaseActivity>): Boolean {
        val basicInfo = mEditInfoViewModel.basicInfo.value ?: return false

        if (basicInfo.homeTown.homeTownId == 0 && basicInfo.birthday == "" && basicInfo.figure.height == 0) {
            //城市为空 && 生日为空 && 身材为空
            val schoolInfo = basicInfo.schoolInfo
            if (schoolInfo.educationCode.isEmpty() && schoolInfo.education.isEmpty() && schoolInfo.school.isEmpty()
                && schoolInfo.startYear.isEmpty()
            ) {
                //学校为空
                val profession = basicInfo.profession
                if (profession.professionId == 0 && profession.incomeText.isEmpty()) {
                    //职业为空，需要连续跳转
                    val tagList = mutableListOf<Class<out BaseActivity>>()
                    tagList.add(HomeTownActivity::class.java)
                    tagList.add(UpdateBirthdayActivity::class.java)
                    tagList.add(FigureActivity::class.java)
                    tagList.add(SchoolActivity::class.java)
                    tagList.add(ProfessionActivity::class.java)

                    EditUtils.jumpList.clear()
                    EditUtils.jumpList.add(curClass)
                    tagList.forEach {
                        if (!EditUtils.jumpList.contains(it)) {
                            EditUtils.jumpList.add(it)
                        }
                    }
                    return true
                }
            }
        }
        return false
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
        mEditInfoViewModel.homePagePicChangeBean.observe(this, Observer {
            if (it != null) {
                //图片有变动
                doWithConverPic(it)
            }
        })
        mEditInfoViewModel.processData.observe(this, Observer {
            if (it != null) {
                updateProgress(it.perfection)
            }
        })

        mEditInfoViewModel.coverOrderSaveFlag.observe(this, Observer {
            if (it == true) {
                finish()
            }
        })
    }

    /**
     * 对封面进行处理
     */
    private fun doWithConverPic(bean: HomePagePicBean) {
        //有变动的logid
        val logId = bean.logId
        if (logId == 0L) {
            return
        }
        var targetIndex = -1
        var targetBean: HomePagePicBean? = null
        mEditPicAdapter.data.forEachIndexed { index, homePagePicBean ->
            if (homePagePicBean.headerPic != BusiConstant.True && (homePagePicBean.logId == logId || homePagePicBean.logId == 0L) && targetBean == null) {
                targetIndex = index
                targetBean = homePagePicBean
            }
        }
        if (targetIndex >= 0 && targetBean != null) {
            //找到对应的封面
            val pic = bean.coverPic
            if (pic.isNotEmpty()) {
                //有图片   新增或者变更
                targetBean?.logId = bean.logId
                targetBean?.coverPic = bean.coverPic
                mEditPicAdapter.notifyDataSetChanged()
            } else {
                //无图片，删除
                mEditPicAdapter.removeAt(targetIndex)
                (0 until (9 - mEditPicAdapter.data.size)).forEach { _ ->
                    mEditPicAdapter.addData(HomePagePicBean())
                }
            }

        }
    }


    private fun showBottomDialog(isHead: Boolean) {
        val actions = arrayListOf<BottomAction>()
        if (isHead) {
            if (currentPicBean?.realPic != BooleanType.TRUE) {
                actions.add(BottomAction(BottomActionCode.AUTH_HEAD, "认证头像"))
            }
            actions.add(BottomAction(BottomActionCode.REPLACE_HEAD, "更换头像"))
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
        if (info.perfection == 100) {
            //隐藏进度条
            con_progress.hide()
        } else {
            //显示进度条
            con_progress.show()
            updateProgress(info.perfection)
        }

        SPUtils.commitString(SPParamKey.RealPeople, info.headRealPeople)
        //显示图片相关
        val pics = info.picList
        //用来显示的数据
        val showPics = mutableListOf<HomePagePicBean>()
        showPics.add(HomePagePicBean(info.headPic, info.headRealPeople, headerPic = BusiConstant.True))
        pics.forEach {
            showPics.add(it)
        }
        if (showPics.size < 9) {
            (0 until (9 - showPics.size)).forEach { _ ->
                showPics.add(HomePagePicBean())
            }
        }
//originCoverPicList
        mEditPicAdapter.setList(showPics)
        tv_progress_bottom.text = info.perfectGuide?.guideText ?: ""

        showSign(info.mySign)
        val normalColor = GlobalUtils.getColor(R.color.black_333)
        val greyColor = GlobalUtils.getColor(R.color.black_999)
        tv_nickname.text = info.nickname
//        //签名
//        val sign = info.mySign
//        if (sign.isEmpty()) {
//            tv_sign.text = "编辑个签，展示我的独特态度"
//            tv_sign.textColor = greyColor
//        } else {
//
//            tv_sign.textColor = normalColor
//            if (sign.length <= 15) {
//                tv_sign.text = sign
//            } else {
//                tv_sign.text = sign.substring(1, 15).plus("...")
//            }
//        }

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
                    tv_voice.text = "审核中"
                }
                VoiceBean.Reject -> {
                    //被拒绝
                    tv_voice.text = "审核不通过"
                    tv_voice.textColor = GlobalUtils.formatColor("#FF2207")
                }
                VoiceBean.Pass -> {
                    //审核通过
                    tv_voice.text = "${info.voice.length}秒录音"
                }
                else -> {
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
        showBasicInfo(tv_home_town, homeTownStr.toString())

        //年龄和星座
        val ageConstell = StringBuilder()
        if (info.age != 0) {
            ageConstell.append(info.age)
            ageConstell.append("/")
            ageConstell.append(info.constellation)
        }
        showBasicInfo(tv_age_constellation, ageConstell.toString())

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
//        tv_figure.text = whBuilder.toString()
        showBasicInfo(tv_figure, whBuilder.toString())

//        tv_school.text = info.schoolInfo.school
        showBasicInfo(tv_school, info.schoolInfo.school)

//        tv_job.text = "${info.profession.professionTypeText}/${info.profession.professionName}"
        if (info.profession.professionName.isNotEmpty()) {
            showBasicInfo(tv_job, info.profession.professionName)
        } else {
            showBasicInfo(tv_job, "")
        }


        val tagList = info.myAuthTag.showTagList
        tv_tag_count.text = "${info.myAuthTag.markTagNum}"
        val realTagList = mutableListOf<UserTagBean>()
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
        val realLikeTagList = mutableListOf<UserTagBean>()
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
     * 显示基础数据
     */
    private fun showBasicInfo(tv: TextView, content: String) {
        if (content.isEmpty()) {
            //内容为空
            tv.text = "未完成"
            tv.textColor = GlobalUtils.formatColor("#FF2207")
        } else {
            //内容不未空
            tv.text = content
            tv.textColor = GlobalUtils.getColor(R.color.black_333)
        }
    }

    /**
     * 显示签名
     */
    private fun showSign(sign: String) {
        val normalColor = GlobalUtils.getColor(R.color.black_333)
        val greyColor = GlobalUtils.getColor(R.color.black_999)
        //签名
        if (sign.isEmpty()) {
            tv_sign.text = "编辑个签，展示我的独特态度"
            tv_sign.textColor = greyColor
        } else {
            tv_sign.textColor = normalColor
            if (sign.length <= 15) {
                tv_sign.text = sign
            } else {
                tv_sign.text = sign.substring(0, 15).plus("...")
            }
        }
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

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                //取到swipe、drag开始和结束时机，viewHolder 不为空的时候是开始，空的时候是结束
                val firstData = mEditPicAdapter.getItemOrNull(0) ?: return
                if (viewHolder == null) {
                    //结束
                    if (firstData.headerPic == BusiConstant.True) {
                        firstData.showNoMoveAttention = BusiConstant.False
                    }
                } else {
                    //开始
                    if (firstData.headerPic == BusiConstant.True) {
                        firstData.showNoMoveAttention = BusiConstant.True
                    }
                }
                mEditPicAdapter.notifyItemChanged(0)
                super.onSelectedChanged(viewHolder, actionState)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun canDropOver(
                recyclerView: RecyclerView,
                current: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val tempData = mEditPicAdapter.getItemOrNull(target.adapterPosition) ?: return false
                if (tempData.headerPic != BusiConstant.True && tempData.coverPic.isNotEmpty()) {
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
            if (tempData.headerPic != BusiConstant.True && tempData.coverPic.isNotEmpty()) {
                helper.startDrag(recycler_view_pic.getChildViewHolder(view))
                playItemAni(view)
            } else {
                if (tempData.headerPic == BusiConstant.True) {
                    //头像 显示提示
                    tempData.showNoMoveAttention = BusiConstant.True
                    mEditPicAdapter.notifyItemChanged(0)
                    timerHideAttention()
                }

            }
            return@setOnItemLongClickListener true
        }
        recycler_view_tag.layoutManager = GridLayoutManager(this, 4)
        recycler_view_tag.adapter = mTagAdapter
        mTagAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mTagAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            AuthTagPicActivity.start(this, item.tagId, true, false, true)
            mEditInfoViewModel.needFresh = true
        }

        recycler_view_like_tag.layoutManager = GridLayoutManager(this, 4)
        recycler_view_like_tag.adapter = mLikeTagAdapter
        mLikeTagAdapter.setOnItemClickListener { adapter, view, position ->
            val item = mLikeTagAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            TagPicsActivity.start(this, item, SessionUtils.getUserId())
            mEditInfoViewModel.needFresh = true
        }

    }

    private fun playItemAni(view: View) {
        val ani1 = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.0f, 1.1f, 1.0f)
        val ani2 = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.0f, 1.1f, 1.0f)
        val aniSet = AnimatorSet()
        aniSet.playTogether(ani1, ani2)
        aniSet.interpolator = OvershootInterpolator()
        aniSet.duration = 300
        aniSet.start()
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
                4
            }
            else -> {
                4
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
            .withAspectRatio(3, heightRatio)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
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
            showSign(sign)
            mEditInfoViewModel.basicInfo.value?.mySign = sign
//            tv_sign.text = sign
//            tv_sign.textColor = GlobalUtils.getColor(R.color.black_333)
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
            tv_home_town.text = homeTownStr.toString()
            tv_home_town.textColor = GlobalUtils.getColor(R.color.black_333)
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
                    it.constellation = constellationName
                }
                val ageConstell = StringBuilder()
                ageConstell.append(age)
                ageConstell.append("/")
                ageConstell.append(constellationName)
                tv_age_constellation.text = ageConstell.toString()
                tv_age_constellation.textColor = GlobalUtils.getColor(R.color.black_333)
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
        showBasicInfo(tv_school, bean.school)
//        tv_school.text = bean.school
//        tv_school.textColor = GlobalUtils.getColor(R.color.black_333)
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
//            if (bean.school.isNotEmpty()) {
            it.school = bean.school
//            }
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
            tv_job.textColor = GlobalUtils.getColor(R.color.black_333)
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
            tv_figure.textColor = GlobalUtils.getColor(R.color.black_333)
        }
    }

    override fun onBackPressed() {
        val picSb = StringBuilder()
        mEditPicAdapter.data.forEach {
            if (it.headerPic != BusiConstant.True && it.logId != 0L) {
                if (picSb.isNotEmpty()) {
                    picSb.append(",")
                }
                picSb.append("${it.logId}")
            }
        }
        //图片保存弹窗
        if (picSb.toString() != mEditInfoViewModel.getOriginPicStr()) {
            //数据发生变化，需要提示
            MyAlertDialog(this).showAlertWithOKAndCancel(
                "封面图有调整，要保存修改吗？\n要保存请点击【保存】",
                MyAlertDialog.MyDialogCallback(onRight = {
                    header_page.textOperation.performClick()
                }, onCancel = {
                    finish()
                }), "修改未保存", "保存", noText = "放弃保存"
            )
            return
        }

        //资料弹窗
        val perfection = mEditInfoViewModel.basicInfo.value?.perfection ?: 0
        if (perfection != null) {
            //签名
//            val sign = basicData.mySign
//            val homeTownId = basicData.homeTown.homeTownId
//            val age = basicData.age
//            val figure = basicData.figure.figure
//            val school = basicData.schoolInfo.school
//            val professionId = basicData.profession.professionId
            if (perfection < 100) {
                //有数据未完成
                MyAlertDialog(this).showAlertWithOKAndCancel(
                    "资料完整度80%以上用户获得心动概率更高",
                    MyAlertDialog.MyDialogCallback(onRight = {

                    }, onCancel = {
                        finish()
                    }), "是否放弃填写资料？", "继续填写", noText = "放弃"
                )
                return
            }

        }

        super.onBackPressed()


    }

}