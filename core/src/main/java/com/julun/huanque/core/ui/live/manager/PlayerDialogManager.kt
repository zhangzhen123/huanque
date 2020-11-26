package com.julun.huanque.core.ui.live.manager

import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.TabBean
import com.julun.huanque.common.bean.beans.MicOperateBean
import com.julun.huanque.common.bean.beans.PKCreateEvent
import com.julun.huanque.common.constant.TabTags
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.OrderDialogManager
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.viewmodel.ConnectMicroViewModel
import com.julun.huanque.common.viewmodel.VideoChangeViewModel
import com.julun.huanque.common.viewmodel.VideoViewModel
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.live.dialog.LiveSquareDialogFragment
import com.julun.huanque.core.ui.live.dialog.OnlineDialogFragment
import com.julun.huanque.core.ui.live.dialog.ScoreDialogFragment
import com.julun.huanque.core.ui.live.fragment.SendGiftFragment
import com.julun.huanque.core.ui.live.fragment.UserCardFragment
import com.julun.huanque.core.viewmodel.*

/**
 * 随着直播间弹窗越来越多,逻辑和视图层耦合太严重，分离和解耦大量dialog逻辑和展示，统一功能相同的弹窗
 * @author WanZhiYuan
 * @since 4.24
 *
 *
 * @date 2020/5/22
 * @author zhangzhen
 * @alter 弹窗管理器整改优化
 */
class PlayerDialogManager(val context: PlayerActivity) {


    companion object {
        //默认的拼接key
        private const val DEFAULT_KEY = "playDialog:"
    }

    private val logger = ULog.getLogger(this.javaClass.name)

    private val playerViewModel: PlayerViewModel by context.viewModels()
    private val mConfigViewModel: OrientationViewModel by context.viewModels()
    private val playerBannerViewModel: PlayerBannerViewModel by context.viewModels()
    private val videoPlayerViewModel: VideoChangeViewModel by context.viewModels()
    private val mVideoViewModel: VideoViewModel by context.viewModels()

    private val huanQueViewModel = HuanViewModelManager.huanQueViewModel
    //    private var mBasePlayerViewModel: BasePlayerViewModel? = null
    //新版PK
    private val pKViewModel: PKViewModel by context.viewModels()

    //主播不在线ViewModel
    private val anchorNoLiveViewModel: AnchorNoLiveViewModel by context.viewModels()

    //连麦ViewModel
    private val connectMicroViewModel: ConnectMicroViewModel by context.viewModels()

    //道具相关ViewModel
    private val propViewModel: PropViewModel by context.viewModels()


    private val dialogsCache = hashMapOf<String, DialogFragment?>()

    // 礼物面板
    private var giftFragment: SendGiftFragment? = null


    init {
        prepareViewModel()
    }

    /**
     * 创建fragment的class唯一标记的key
     */
    private fun getFragmentKey(clazz: Class<*>): String {
        return DEFAULT_KEY + clazz.name
    }

    private fun prepareViewModel() {
        playerViewModel.refreshDialog.observe(context, Observer {
            it ?: return@Observer
            refreshDialog(it)
        })
        playerViewModel.showDialog.observe(context, Observer {
            it ?: return@Observer
            showDialog(it)
        })
        playerViewModel.closeDialog.observe(context, Observer {
            it ?: return@Observer
            hideFragment(it)
        })
        playerViewModel.topDialog.observe(context, Observer { it?.let { iit -> openTopDialog(iit.content, iit.drawable) } })
        playerViewModel.recommendView.observe(context, Observer {
            val programId = playerViewModel.programId
//                openDialog(clazz = RecommendFragment::class.java, builder = {
//                    RecommendFragment.newInstance(programId)
//                })
        })

        playerViewModel.giftView.observe(context, Observer {
            if (it != null) {
//                openGiftView()
            }
        })

        playerViewModel.needRefreshAll.observe(context, Observer {
            it ?: return@Observer
            refreshGiftDialog()
        })
        playerViewModel.guardView.observe(context, Observer {
            if (it != null) {
//                openGuardView()
            }
        })
        playerViewModel.shareView.observe(context, Observer { showUserMoreSetting() })
        connectMicroViewModel.createConnectShow.observe(context, Observer {
            if (it == true) {
                openCreateConnectMicroFragment()
            }
        })
        connectMicroViewModel.createPkShow.observe(context, Observer {
            if (it == true) {
                openCreatePKView()
            }
        })
        connectMicroViewModel.notarizeShowState.observe(context, Observer {
            if (it != null) {
                openNotarizeConnectMicroFragment(it)
            }
        })


        anchorNoLiveViewModel.showRecommendProgram.observe(context, Observer {
            if (it == true) {
//                openDialog(RecommendProgramInfoDialog::class.java, reuse = false)
            }
        })

        playerViewModel.squareView.observe(context, Observer {
            if (it == true) {
                if (!isFragmentShow(LiveSquareDialogFragment::class.java)) {
                    openDialog(LiveSquareDialogFragment::class.java)
                }

            }
        })


        playerViewModel.scoreView.observe(context, Observer {
            openDialog(ScoreDialogFragment::class.java, builder = { ScoreDialogFragment.newInstance(playerViewModel.programId) })
        })

        playerViewModel.openOnlineDialog.observe(context, Observer { TabTag ->

            openDialog(OnlineDialogFragment::class.java, builder = {

                val royalCount = playerViewModel.roomData?.royalCount ?: 0
                val onlineUserNum = playerViewModel.roomData?.onlineUserNum ?: 0
                val royalTitle = "贵族席位(${royalCount})"

                val onlineUserTitle = "在线用户(${onlineUserNum})"

                OnlineDialogFragment.newInstance(
                    arrayListOf(
                        TabBean(TabTags.TAB_TAG_ONLINE, onlineUserTitle, royalCount < 0),
                        TabBean(TabTags.TAB_TAG_ROYAL, royalTitle, royalCount > 0)
                    )
                )
            })
        })


        playerViewModel.userInfoView.observe(context, Observer { info ->
            if (info != null) {
                //显示用户名片
                openDialog(UserCardFragment::class.java, builder = {
                    UserCardFragment.newInstance(info.userId, info.nickname)
                })
                playerViewModel.userInfoView.value = null
            }
        })

        playerViewModel.guideToFollow.observe(context, Observer { info ->
            if (info != null) {
//                openDialog(GuideFollowFragment::class.java,reuse = true)
                MyAlertDialog(context).showAlertWithOKAndCancel("看了这么久了，关注一下吧", MyAlertDialog.MyDialogCallback(onCancel = {
                    playerViewModel.finishCertain = true
                    playerViewModel.finishState.value = true
                }, onRight = {
                    playerViewModel.finishCertain = true
                    huanQueViewModel.follow(playerViewModel.programId)
                    playerViewModel.finishState.value = true
                }), "关注提醒", okText = "关注并退出", noText = "直接退出")
            }
        })
    }

    /**
     * 打开礼物弹窗
     */
    fun openGiftDialog() {
        if (giftFragment == null) {
            giftFragment = SendGiftFragment.newInstance()
            dialogsCache[getFragmentKey(SendGiftFragment::class.java)] = giftFragment
        }
        giftFragment?.show(context, "SendGiftFragment")
    }

    /**
     * 关闭礼物弹窗
     * @param isReset 再次打开是否重置弹窗
     */
    fun closeGiftDialog(isReset: Boolean) {
        giftFragment?.dismiss()
        if (isReset) {
            giftFragment = null
        }
    }

    /**
     * 重置礼物弹窗
     */
    fun refreshGiftDialog() {
        if (giftFragment == null) {
            return
        }
        if (giftFragment != null && !giftFragment!!.isAdded) {
            //如果未打开礼物面板那就重置吧
            giftFragment = null
            return
        }
        playerViewModel.refreshGift.value = true
    }

    //显示用户更多设置布局
    fun showUserMoreSetting() {
//        mDialogManager.openDialog(clazz = UserMoreActionFragment::class.java, builder = {
//            UserMoreActionFragment.newInstance(viewModel.programId)
//        })
    }

    /**
     * 打开顶部弹窗
     */
    fun openTopDialog(content: String, icon: Int = 0) {
//        openDialog(TopShowFragment::class.java, builder = {
//            TopShowFragment.newInstance(content, icon)
//        })
    }


    /**
     * 显示创建连麦弹窗
     */
    fun openCreateConnectMicroFragment() {
//        openDialog(CreateConnectMicroFragment::class.java, builder = { dialog ->
//            dialog ?: CreateConnectMicroFragment.newInstance(BusiConstant.ConnectType.CONNECTMICRO)
//        }, reuse = true)
    }

    /**
     * 显示等待连麦弹窗
     */
    fun openNotarizeConnectMicroFragment(data: MicOperateBean) {
        val list = data.detailList
//        if (list.size >= 2) {
//            //最少需要有两个主播数据  不需要复用
//            openDialog(NotarizeConnectMicroFragment::class.java, builder = {
//                NotarizeConnectMicroFragment.newInstance(data)
//            })
//        }
    }

    /**
     * 显示等待连麦弹窗
     */
    fun openNotarizePKFragment(data: PKCreateEvent) {
        val list = data.detailList
        if (list.size >= 2) {
            //最少需要有两个主播数据  不需要复用
//            openDialog(NotarizeConnectMicroFragment::class.java, builder = {
//                NotarizeConnectMicroFragment.newInstance(data)
//            })
        }
    }

    /**
     * 弹出创建pk
     */
    fun openCreatePKView() {
        logger.info("开始打开创建PK")
//        openDialog(CreateConnectMicroFragment::class.java, builder = {
//            val createPKFragment = it ?: CreateConnectMicroFragment.newInstance(BusiConstant.ConnectType.PK)
//            createPKFragment
//        }, reuse = true)
    }

    /**
     * 打开弹窗的统一方法
     * [clazz]要打开的弹窗的类型
     * [builder]有些弹窗需要特殊构造 比如需要传值 [builder]有的话优先使用 这里使用函数式传值的好处就是 可以后期处理想要的操作 而且是根据需要延迟创建
     * [reuse]是否需要复用
     * [order]是否需要排队 [OrderDialogManager.NO_ORDER]不需要 [OrderDialogManager.LOCAL_ORDER]局部排队显示 [OrderDialogManager.GLOBAL_ORDER]全局排队显示
     * [OrderDialogManager.USER_POSITIVE]用户手动打开
     * [showLogin]是否需要先登录
     *
     *
     */
    fun <T : DialogFragment> openDialog(
        clazz: Class<T>,
        builder: (T?) -> T? = { null },
        reuse: Boolean = false,
        order: Int = OrderDialogManager.NO_ORDER,
        showLogin: Boolean = false
    ) {
        val fragment: DialogFragment
        //是否需要登录
        if (showLogin) {
            if (!MixedHelper.checkLogin()) {
                return
            }
        }
        if (reuse) {
            val cacheFragment = dialogsCache[getFragmentKey(clazz)]

            if (cacheFragment != null) {
                if (cacheFragment.isVisible) {
                    cacheFragment.dismiss()
                }
                fragment = cacheFragment
                //基本不会转化错
                try {
                    builder.invoke(cacheFragment as? T?)
                } catch (e: ClassCastException) {
                    e.printStackTrace()
                }

            } else {
                fragment = builder.invoke(null) ?: clazz.newInstance()
                dialogsCache[getFragmentKey(clazz)] = fragment
            }


        } else {
            fragment = builder(null) ?: clazz.newInstance()
            dialogsCache[getFragmentKey(clazz)] = fragment
        }


        when (order) {
            OrderDialogManager.LOCAL_ORDER -> {
                if (fragment is BaseDialogFragment)
                    context.addOrderDialog(fragment)
            }
            OrderDialogManager.GLOBAL_ORDER -> {
                //暂时没有

            }
            OrderDialogManager.USER_POSITIVE -> {
                if (fragment is BaseDialogFragment)
                    fragment.showPositive(context.supportFragmentManager, clazz.name)
            }
            else -> fragment.show(context, clazz.name)
        }


    }

    /**
     * 针对通过viewModel控制弹窗的需要特殊处理
     *
     */
    fun <T : DialogFragment> showDialog(clazz: Class<T>) {

        var reuse = false
        var order = -1
        var showLogin = false
        when (clazz) {
            //todo
//            SigninDialogFragment::class.java -> {
//                openDialog(clazz, builder = { SigninDialogFragment.newInstance(requestType = BusiConstant.WelfareCenterType.TYPE_CENTER) })
//                return
//            }
        }

        openDialog(clazz, reuse = reuse, order = order, showLogin = showLogin)
    }

    /**
     *
     * 操作弹窗  此处只处理关闭或者刷新 新创建只使用[openDialog]
     * [clazz]要操作的弹窗
     * [operator]操作类型
     *
     */
    fun <T : DialogFragment> refreshDialog(clazz: Class<T>) {
        val fragment = dialogsCache[getFragmentKey(clazz)] ?: return
        if (fragment is BaseDialogFragment) {
            fragment.refreshDialog()
        }
    }

    /**
     * 在登录后重置状态
     */
    fun resetStatus() {

    }

    /**
     * 隐藏所有可能显示的弹窗
     */
    fun hideAllDialog() {
        //关闭所有的正在显示的弹窗 但不清空缓存
        dialogsCache.values.forEach {
            if (it?.isVisible == true) {
                it.dismiss()
            }
        }
    }

    /**
     * 关闭所有的弹窗
     * 清空所有的弹窗缓存
     */
    fun clearAllDialog() {
        dialogsCache.values.forEach {
            if (it?.isVisible == true) {
                it.dismiss()
            }
        }
        dialogsCache.clear()
    }

    /**
     * 某个弹窗是否正在显示
     */
    fun isFragmentShow(dialog: Class<*>): Boolean {
        val fragment = dialogsCache[getFragmentKey(dialog)] ?: return false
        return fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    }

    /**
     * 返回某一个弹窗 没有就返回空
     */
    fun <T : DialogFragment> getFragment(dialog: Class<T>): T? {
        return try {
            dialogsCache[getFragmentKey(dialog)] as? T
        } catch (e: ClassCastException) {
            null
        }
    }

    /**
     * 从缓存中删除一个弹窗
     */
    fun removeFragment(dialog: Class<*>) {
        dialogsCache.remove(getFragmentKey(dialog))
    }

    /**
     * 隐藏一个弹窗
     */
    fun hideFragment(dialog: Class<*>) {
        val fragment = dialogsCache[getFragmentKey(dialog)]
        if (fragment?.isVisible == true) {
            fragment.dismiss()
        }
    }

}