package com.julun.huanque.core.widgets.live

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.StatisticCode
import com.julun.huanque.common.constant.TabTags
import com.julun.huanque.common.constant.UserChangeType
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.widgets.PhotoHeadView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorListenerAdapter
import com.nineoldandroids.animation.AnimatorSet
import com.nineoldandroids.animation.ObjectAnimator
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.view_live_header.view.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import java.text.DecimalFormat
import java.util.*
import kotlin.properties.Delegates


/**
 * 直播界面上部组件，包括：主播信息、在线用户数据、粉丝前两名等
 * Created by djp on 2016/11/29.
 */
class LiveHeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private val logger = ULog.getLogger("LiveHeaderView")

    // 榜单前2位
    private val TOP_MAX_LIMIT = 2
    private val USER_MAX_COUNT = 30
    private var programId: Long = 0

    //    private var anchorInfo: AnchorInfoInRoom? = null
    //主播基础信息
    private var anchorInfo: AnchorBasicInfo? = null

    // 主播id
//    private var anchorId: Long = 0
//    private var userId: Long = 0

    // 是否关注
    private var isSubscribed: Boolean = false

    //是否加入粉丝团
    private var isFansJoin: Boolean = false

    //是否粉丝团打卡
    private var isFansClockIn: Boolean = false

    //是否是主播
    var isAnchor: Boolean by Delegates.observable(false) { d, old, new ->
        if (new) {
            subscribeAnchor.hide()
        }
    }

    // 当前用户列表（已排除掉主播自己）
    private var roomUsers: MutableList<UserInfoForLmRoom> = Collections.synchronizedList(mutableListOf())

    // 榜单用户列表
    private var topList: MutableList<UserInfoForLmRoom> = Collections.synchronizedList(mutableListOf())

    private var playerViewModel: PlayerViewModel? = null

//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//
//    }

    private fun initViewModel() {
        val activity = context as? PlayerActivity
        activity?.let {
            playerViewModel = ViewModelProvider(activity).get(PlayerViewModel::class.java)
        }
    }

    private val userListAdapter = object : BaseQuickAdapter<UserInfoForLmRoom, BaseViewHolder>(R.layout.item_live_room_user) {
        override fun convert(holder: BaseViewHolder, item: UserInfoForLmRoom) {
            val headerImage = holder.getView<PhotoHeadView>(R.id.headerImage)
            if (item.headFrame.isNotEmpty()) {
                headerImage.setImageCustomByOneFrameSide(
                    headUrl = item.headPic + BusiConstant.OSS_160,
                    frameUrl = item.headFrame,
                    headSize = 30,
                    frameWidth = FrameLayout.LayoutParams.WRAP_CONTENT,
                    frameHeight = 48
                )

            } else {
                headerImage.setImage(
                    headUrl = item.headPic + BusiConstant.OSS_160,
                    headSize = 30,
                    frameWidth = 36,
                    frameHeight = 48
                )
            }
            //添加边框
//            val roundingParams = RoundingParams.fromCornersRadius(5f)
//            roundingParams.roundAsCircle = true
//            ULog.i("当前的贵族等级："+item.levelMap.ROYAL_LEVEL)
//            if (item.levelMap.ROYAL_LEVEL >= 3) {
//                roundingParams.setBorder(ImageUtils.getRoyalLevelColor(item.levelMap.ROYAL_LEVEL, context), context.dip(1f).toFloat())
//            } else {
//                roundingParams.setBorder(Color.TRANSPARENT, 0f)
//                imgView.setPadding(context.dip(1), context.dip(1), context.dip(1), context.dip(1))
//            }
//            imgView.hierarchy.roundingParams = roundingParams
//            ImageUtils.loadImage(imgView, item.headPic, 32f, 32f)
            val smpImage: SimpleDraweeView = holder.getView(R.id.identityImage)
            // 优先显示 守护 图标
            if (item.smallPic.isNotEmpty()) {
                smpImage.visibility = View.VISIBLE
                ImageUtils.loadImageWithHeight_2(smpImage, item.smallPic, dip(16))
            } else {
                smpImage.visibility = View.GONE
            }
        }
    }/*.apply {
        setItemClickListener {
            view, holder, position ->
            // 用户个人信息界面
            if (position >= 0 && roomUsers.size > 0) {
                // bugly上有一个报错position=-1，很奇怪，点击的时候正在刷新列表，roomUsers清空了？
                playerActivity.openUserInfoView(roomUsers[position].userId, false)
            }
        }
    }*/

    init {
        initViewModel()
        LayoutInflater.from(context).inflate(R.layout.view_live_header, this)
        initViews()
    }

    private fun initViews() {
        onlineUserListView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            context,
            androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
            false
        )
        onlineUserListView.adapter = userListAdapter
        authorContainer.onClickNew {
            // 主播个人信息界面
            playerViewModel?.userInfoView?.value = UserInfoBean(
                userId = programId, isAnchor = true, royalLevel = 0, userPortrait = anchorInfo?.headPic
                    ?: "", nickname = authorNicknameText.text.toString()
            )

        }
        // 关注
        subscribeAnchor.onClickNew {
            // 没关注直接关注，关注了打开主播信息界面
            if (!isSubscribed) {
                playerViewModel?.subscribeSource = "直播间左上角"
                reportClick(StatisticCode.Follow + StatisticCode.LiveRoom)
                HuanViewModelManager.huanQueViewModel.follow(programId)
            }
            subscribeAnchor.isEnabled = false
        }
        exitImage.onClickNew {
            playerViewModel?.finishState?.value = true
        }
        flRoyalRootView.onClickNew {
//            if(ivRoyalIcon.isVisible){
//                playerViewModel?.openOnlineDialog?.value = TabTags.TAB_TAG_ROYAL
//            }else{
//                playerViewModel?.openOnlineDialog?.value = TabTags.TAB_TAG_ONLINE
//            }
            playerViewModel?.openOnlineDialog?.value = TabTags.TAB_TAG_ONLINE
        }
        //
        userListAdapter.setOnItemClickListener { _, _, position ->
            // 用户个人信息界面
            // 数据源从adapter中提取 因为roomUsers频繁变动 在通知刷新adapter数据前可能会有真空期
            // 此时adapter中的数据会与roomUsers不一致 就会出现数组越界等问题
            if (position < 0 || position >= userListAdapter.itemCount) {
                logger.info("当前的位置不合法 位置：$position datas总数:${userListAdapter.itemCount}")
                reportCrash("当前的位置不合法 位置：$position datas总数:${userListAdapter.itemCount}")
            } else {
                val user = userListAdapter.getItemOrNull(position)
                user?.let {
                    playerViewModel?.userInfoView?.value =
                        UserInfoBean(it.userId, false, it.royalLevel, it.picId, nickname = it.nickname)
                }

            }
        }
        if (!isInEditMode) {
            tv_user_count.setTFDinCdc2()
        }

//        ivFansJoin.onClickNew {
//            //粉丝团加入
//            playerViewModel?.actionBeanData?.value = BottomActionBean(ClickType.FANS)
//        }
//        lavFansEvent.onClickNew {
//            //粉丝团打卡
//            playerViewModel?.clockIn?.value = true
//        }
//        flRoyalRootView.onClickNew {
//            //打开贵族列表弹窗
//            playerViewModel?.openOnlineDialog?.value = DialogTypes.DIALOG_ROYAL
//        }

    }

    /**
     * 初始化基础信息  由basic接口提供
     */
    fun initBaseData(dto: UserEnterRoomRespBase) {
        programId = dto.programId
//        anchorId = dto.anchorId
//        userId = dto.anchorId
        ImageUtils.loadImage(authorPhotoImage, dto.headPic, 30f, 30f)

        // 直播间名称
        authorNicknameText.text = dto.programName
        iv_anchor_level.imageResource = ImageHelper.getAnchorLevelShortResId(dto.anchorLevel)
    }

    /**
     * 初始化头部UI
     */
    private fun prepare(info: AnchorBasicInfo, prettyId: Long?) {
//        userId = info.anchorId
        // 直播间名称
        authorNicknameText.text = info.programName
        //头像
        ImageUtils.loadImage(authorPhotoImage, info.headPic, 30f, 30f)
        // 直播间ID

    }

    fun initData(roomData: UserEnterRoomRespDto) {
//        logger.info("liveHeader ：${JsonUtil.seriazileAsString(roomData)}")
        hotText.text = "${StringHelper.formatNum(roomData.heatValue)}"
        changeFansViews(roomData.groupMember, roomData.fansClockIn)

        if (isAnchor) {
            //主播身份
            prepare(roomData.anchor ?: return, roomData.prettyId)
        }
        isSubscribed = roomData.follow

        if (!isSubscribed) {
            subscribeAnchor.show()
        } else {
            subscribeAnchor.hide()
        }

        val royalCount = roomData.royalCount ?: 0
        val guardCount = roomData.guardCount ?: 0
        when {
            royalCount < 0 -> tvRoyalContent.text = "0"
            royalCount > 9999 -> tvRoyalContent.text = "9999"
            else -> tvRoyalContent.text = "$royalCount"
        }
//        when {
//            guardCount < 0 -> tvGuardContent.text = "0"
//            guardCount > 9999 -> tvGuardContent.text = "9999"
//            else -> tvGuardContent.text = "$guardCount"
//        }
        //直播间用户数量
        tv_user_count.text = formatCount(roomData.onlineUserNum)
//        tv_user_count.text = formatCount(roomData.onlineUserNum)
        // 停播跳转重进直播间是需要清空数据
        roomUsers.clear()
        topList.clear()

//        topList = orderWithScore(roomData.topContribution)
//        doRefreshTopView()

        roomUsers = orderUsersAndDelAnchor(roomData.onlineUsers)
        isReduceUserCount()
        doRefreshRoomUserList()
//        if (roomData.royalCount > 0) {
//            royalButtonAnimation()
//        } else {
//            stopAniWithOriginal()
//        }
    }

    private var dispose: Disposable? = null

    var closeAnimation = false
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dispose?.dispose()
        closeAnimation = true
        mRoyalAnimation?.cancel()
    }

    private var currentCount: Int = 0

    /*对在线人数进行处理*/
    private fun formatCount(count: Int): String {
        currentCount = count
        return if (count < 10000) {
            "$count"
        } else {
            DecimalFormat("#.#").format((count / 10000).toDouble()) + "万"

        }
    }

    /**
     * 更新热度数据
     */
    fun updateHeatValue(heatNum: Long) {
        hotText.text = "${StringHelper.formatNum(heatNum)}"
    }


    fun setSubscribeEnable(bool: Boolean) {
        subscribeAnchor.isEnabled = bool
    }

    // 关注成功：显示主播等级图标，隐藏关注图标
    // 取消关注：隐藏主播等级图标，显示关注图标
    fun subscribeSuccess(bool: Boolean) {
        isSubscribed = bool

        if (isAnchor) {
            subscribeAnchor.hide()
            return
        }
        changeFansViews(isFansJoin, isFansClockIn)
        if (!bool) {
            subscribeAnchor.show()
        } else {
            subscribeAnchor.hide()
        }

    }

    /**
     * 改变粉丝团按钮状态
     */
    fun changeFansViews(gruopMember: Boolean = false, fansJoin: Boolean = false) {
        isFansClockIn = fansJoin
        isFansJoin = gruopMember
//        if (!isSubscribed) {
//            subscribeAnchor.show()
//            lavFansEvent.hide()
//            ivFansJoin.hide()
//        } else {
//            subscribeAnchor.hide()
//            ivFansJoin.hide()
//            lavFansEvent.hide()
//            if (!gruopMember) {
//                ivFansJoin.show()
//            } else {
//                if (!fansJoin) {
//                    lavFansEvent.show()
//                }
//            }
//        }
    }

    /**
     * 将在线用户按照score进行排序，并且删除主播
     */
    private fun orderUsersAndDelAnchor(userList: MutableList<UserInfoForLmRoom>): MutableList<UserInfoForLmRoom> {
        val orderUsers = orderWithScore(userList)
        for (user in orderUsers) {
            if (user.userId == programId) {
                orderUsers.remove(user)
                break
            }
        }
        return orderUsers
    }

    private fun isReduceUserCount() {
        if (roomUsers.size > USER_MAX_COUNT) {
            roomUsers = roomUsers.subList(0, USER_MAX_COUNT)
        }
    }

    // 通用排序方法 通过字典中的score属性进行排序
    private fun orderWithScore(userList: MutableList<UserInfoForLmRoom>): MutableList<UserInfoForLmRoom> {
        if (userList.size > 1) {
            userList.sortWith(Comparator<UserInfoForLmRoom> { obj1, obj2 -> obj2.score.compareTo(obj1.score) })
        }
        return userList
    }

    // 刷新在线用户, 主线程执行
    private fun doRefreshRoomUserList() {
        //最多显示20个用户
        if (roomUsers.size <= 20) {
            userListAdapter.setList(roomUsers)
        } else {
            val list = mutableListOf<UserInfoForLmRoom>()
            list.addAll(roomUsers.subList(0, 20))
            userListAdapter.setList(list)
        }
    }

    // 当前新增用户是否在列表中，不在则添加
    private fun addRoomUser(newObj: UserInfoForLmRoom) {
        val newUserId = newObj.userId
        var bool = false
        for ((userId) in roomUsers) {
            if (userId == newUserId) {
                bool = true
                break
            }
        }
        if (!bool) {
            roomUsers.add(newObj)
        }
    }

    /**
     * 接收刷新在线用户列表的事件消息
     */
    fun handleRoomUserChange(data: RoomUserChangeEvent) {
        logger.info("收到数量变化通知：${data.totalCount}")
//        Single.just(data).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { item ->
        if (data.changes.isNotEmpty()) {
            val userList = data.changes
            userList.forEach {
                when (it.type) {
                    UserChangeType.New -> addRoomUser(it)
                    UserChangeType.Mod -> replaceExistUserItem(it)
                    UserChangeType.Del -> deleteExistUserItem(it)
                }
            }
            roomUsers = orderWithScore(roomUsers)
            isReduceUserCount()
        }
        doRefreshRoomUserList()
        //更新roomData数据
        val roomData = playerViewModel?.roomData ?: return
        roomData.onlineUserNum = data.totalCount
        roomData.royalCount = data.royalCount
        roomData.guardCount = data.guardCount
//        if (roomData.royalCount > 0) {
//            royalButtonAnimation()
//        } else {
//            stopAniWithOriginal()
//        }
        //修改贵族和非贵族的数据
        tv_user_count.text = formatCount(roomData.onlineUserNum)
        tvRoyalContent.text = formatCount(roomData.royalCount)
    }

    private fun replaceExistUserItem(newObj: UserInfoForLmRoom) {
        roomUsers.forEachIndexed { index, obj ->
            if (obj.userId == newObj.userId) {
                roomUsers[index] = newObj
                return
            }
        }
    }

    // 用户退出直播间
    private fun deleteExistUserItem(delObj: UserInfoForLmRoom) {
        for (user in roomUsers) {
            if (user.userId == delObj.userId) {
                roomUsers.remove(user)
                break
            }
        }
    }

    private var mRoyalAnimation: AnimatorSet? = null
    private var stopRoyalAni: Boolean = false

    //是否正在执行动画
    private var mIsStartAnimation: Boolean = false

    //是否回归原样
    private var mIsResetView: Boolean = false

    /**
     * 贵族入口动画
     * 参数：3秒旋转360度切换icon，无限循环
     */
    private fun royalButtonAnimation() {
        if (mRoyalAnimation?.isStarted == true || mIsStartAnimation) {
            return
        }
        mIsResetView = false
        mIsStartAnimation = true
        stopRoyalAni = false
        if (mRoyalAnimation != null) {
            mRoyalAnimation?.startDelay = 3000
            mRoyalAnimation?.start()
            return
        }
        mRoyalAnimation = mRoyalAnimation ?: AnimatorSet()
        val rotationY0201 = ObjectAnimator.ofFloat(flRoyalRootView, "rotationY", 0f, -90f)
        val rotationY02015 = ObjectAnimator.ofFloat(flRoyalRootView, "rotationY", -90f, -270f)
        val rotationY0202 = ObjectAnimator.ofFloat(flRoyalRootView, "rotationY", -270f, -360f)
        rotationY0201.duration = 210L
        rotationY02015.duration = 2L
        rotationY0202.duration = 210L
        rotationY0201.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                //回复原状
                if (mIsResetView && stopRoyalAni) {
                    flRoyalRootView.rotationY = -360f
                    ivRoyalIcon.hide()
                    tvRoyalContent.hide()
                    tv_user_count.show()
//                    ivGuardIcon.hide()
//                    tvGuardContent.hide()
                    return
                }
                if (ivRoyalIcon.isVisible()) {
                    ivRoyalIcon.hide()
                    tvRoyalContent.hide()
                    tv_user_count.show()
//                    ivGuardIcon.show()
//                    tvGuardContent.show()
                } else {
                    ivRoyalIcon.show()
                    tvRoyalContent.show()
//                    ivGuardIcon.hide()
//                    tvGuardContent.hide()
                    tv_user_count.hide()
                }
            }
        })
        mRoyalAnimation?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (!stopRoyalAni) {
                    mRoyalAnimation?.start()
                } else {
                    mIsStartAnimation = false
                }
            }
        })
        mRoyalAnimation?.playSequentially(rotationY0201, rotationY02015, rotationY0202)
        mRoyalAnimation?.startDelay = 3000
        mRoyalAnimation?.start()
    }

    /**
     * 停止动画并回归原态
     */
    private fun stopAniWithOriginal() {
        stopRoyalAni = true
        mIsStartAnimation = false
        mIsResetView = true
        mRoyalAnimation?.cancel()
    }
}