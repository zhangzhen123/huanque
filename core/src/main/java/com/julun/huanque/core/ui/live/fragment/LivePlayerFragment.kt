package com.julun.huanque.core.ui.live.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.bean.beans.BottomActionBean
import com.julun.huanque.common.bean.beans.LiveBean
import com.julun.huanque.common.bean.beans.MicAnchor
import com.julun.huanque.common.bean.beans.PlayInfo
import com.julun.huanque.common.bean.events.FloatingCloseEvent
import com.julun.huanque.common.bean.events.VideoPlayerEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.reportClick
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.viewmodel.VideoChangeViewModel
import com.julun.huanque.common.viewmodel.VideoViewModel
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.OrientationViewModel
import com.julun.huanque.core.widgets.SingleVideoView
import kotlinx.android.synthetic.main.fragment_live_player.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *@创建者   dong
 *@创建时间 2019/12/2 10:20
 *@描述 声网播放器
 */
open class LivePlayerFragment : BaseFragment() {

    private val videoPlayerViewModel: VideoChangeViewModel by activityViewModels()
    private val mVideoViewModel: VideoViewModel by activityViewModels()
    private val mConfigViewModel: OrientationViewModel by activityViewModels()
    private val mPlayerViewModel: PlayerViewModel by activityViewModels()
    private val huanQueViewModel = HuanViewModelManager.huanQueViewModel

    //保存播放使用的View
    private val mViewList = mutableListOf<SingleVideoView>()

    private val singleVideoListener: SingleVideoView.OnVideoListener by lazy {
        object : SingleVideoView.OnVideoListener {
            override fun onClickAuthorInfo(authorInfo: MicAnchor) {
                logger.info("点击了主播信息$authorInfo")
                mPlayerViewModel.checkoutRoom.value = authorInfo.programId
            }

            override fun onClickAuthorFollow(authorInfo: MicAnchor) {
                logger.info("点击了关注主播$authorInfo")
                reportClick(StatisticCode.Follow + StatisticCode.LiveRoom)
                huanQueViewModel.follow(authorInfo.programId)
            }
        }
    }
    private val playerDataObserver = Observer<LiveBean> {
        if (it != null) {
            mVideoViewModel.stopAllStreamState.value = StopAllStreamState.Nothing
            startPlayMain(it.programPoster, it.playinfo)
            mVideoViewModel.playerData.value = null
        }
    }

    private val playInfoDataObserver = Observer<PlayInfo> {
        if (it != null) {
//                mMainVideoView?.mProgramID = mVideoViewModel?.programId ?: 0
//                playByInfo(it, mMainVideoView ?: return@Observer)
            startPlayMain(playInfo = it)
            mVideoViewModel.playInfoData.value = null
        }
    }

    private val addPlayerDataObserver = Observer<List<MicAnchor>> {
        if (it != null) {
            handleStreamAdded(it)
            videoPlayerViewModel.addPlayerDatas.value = null
        }
    }

    private val rmPlayerDataObserver = Observer<List<MicAnchor>> {
        if (it != null) {
//                logger.info("删除流通知：${it?.size}")
            handleStreamDeleted(it)
            videoPlayerViewModel.rmPlayerDatas.value = null

        }
    }
    private val stopAllStreamStateObserver = Observer<Int> {
        if (it > 0) {
//                AliPlayerManager.stop()
            //切换直播间时关闭所有类型的流
            when (it) {
                StopAllStreamState.StopNormal -> {
                    stopAllStream(false, needDisConnect = false)
                }
                StopAllStreamState.StopAll -> {
                    stopAllStream(true, needDisConnect = false)
                }
                StopAllStreamState.StopAllWithDisConnect -> {
                    stopAllStream(true, needDisConnect = true)
                }
            }

            mVideoViewModel.stopAllStreamState.value = StopAllStreamState.Nothing
        }
    }

    //用户使用的主播放器
    private var mMainVideoView: SingleVideoView? = null

    private var mRankRootView: LinearLayout? = null

    override fun getLayoutId() = R.layout.fragment_live_player

    override fun isRegisterEventBus() = true

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        //添加主布局
        mMainVideoView = SingleVideoView(requireContext(), true)
        mMainVideoView?.let { mv ->
            mViewList.add(mv)
            ll_container.addView(mv, 0)
            val lp = mv.layoutParams as? LinearLayout.LayoutParams
            lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
            lp?.width = 0
            lp?.weight = 1f
        }

        initViewModel()

        iv_orientation?.onClickNew {
            mVideoViewModel.actionBeanData.value = BottomActionBean(ClickType.SWITCH_SCREEN, ScreenType.HP)
        }

    }

    override fun onResume() {
        super.onResume()
        logger.info("DXCPlayer onResume")
        mViewList.forEach {
            if (it.visibility == View.VISIBLE) {
                it.resume()
            }
        }
    }

    private fun initViewModel() {
        //有可能在该播放界面没创建时 有未执行的缓存stopAllStreamState数据
        // 对于新创建的界面来说没有用而且会引起错误逻辑 在这里每次新创建时把该状态值清空
        mVideoViewModel.stopAllStreamState.value = StopAllStreamState.Nothing

        /**
         * ------------------------------------------------------------------------------------------------
         * 这些关键的增删流操作 全部使用observeForever去监听 与生命周期不挂钩  防止切换后台后 有些关键消息没有处理而导致错乱
         */
        //这个一定要放在前面 不然会后响应 执行顺序就乱了
        mVideoViewModel.playerData.observeForever(playerDataObserver)

        mVideoViewModel.playInfoData.observeForever(playInfoDataObserver)


        videoPlayerViewModel.addPlayerDatas.observeForever(addPlayerDataObserver)

        videoPlayerViewModel.rmPlayerDatas.observeForever(rmPlayerDataObserver)

        mVideoViewModel.stopAllStreamState.observeForever(stopAllStreamStateObserver)


        /**
         * ------------------------------------------------------------------------------------------------
         */
        videoPlayerViewModel.showVideoInfo.observe(this, Observer {
            if (it != null) {
                showVideoPlayerInfo()
            }
        })

        mConfigViewModel.screenTypeData.observe(this, Observer {
            if (it == ScreenType.HP && mConfigViewModel.horizonState.value != true && mPlayerViewModel.chatModeState.value != true) {
                //主播处于横屏，本地处于竖屏
                iv_orientation.show()
            } else {
                iv_orientation.hide()
            }
        })
        mConfigViewModel.horizonState.observe(this, Observer {
            val oriData = mConfigViewModel.screenTypeData.value
            mConfigViewModel.screenTypeData.value = oriData
        })

        mPlayerViewModel.chatModeState.observe(this, Observer {
            //重新设置一下 横竖屏切换按钮
            val screenData = mConfigViewModel.screenTypeData.value
            mConfigViewModel.screenTypeData.value = screenData
            if (it == true) {
                ll_container.hide()
                mViewList.forEach { sv ->
                    sv.mChatMode = true
                    sv.pause()
                }
            } else {
                ll_container.show()
                mViewList.forEach { sv ->
                    sv.mChatMode = false
                    sv.playStream()
                }
            }
        })

        huanQueViewModel.userInfoStatusChange.observe(this, Observer {
            logger.info("Player 关注状态 status = $it")
            if (it.isSuccess() && it.requireT().follow == FollowStatus.True || it.requireT().follow == FollowStatus.Mutual) {
                mViewList.forEach { view ->
                    val info = view.playerInfo ?: return@forEach
                    if (view != mMainVideoView && !view.isFree && info.programId == it.requireT().userId) {

                        logger.info("匹配到相应view${view.playerInfo?.programId}")
                        //刷新主播信息
                        info.follow = true
                        info.showInfo = true
                        view.setPlayInfo(info)
                        return@Observer
                    }

                }
            }


        })
    }

    /**
     * 开始播放主流.
     */
    private fun startPlayMain(posterUrl: String? = null, playInfo: PlayInfo?) {

        if (playInfo == null || mMainVideoView == null) {
            return
        }
        if (mMainVideoView?.getStreamUrl() == GlobalUtils.getPlayUrl(playInfo)) {
            logger.info("主流播放的流地址一致")
            return
        }
//        AliPlayerManager.stop()
        // 设置流信息
        mMainVideoView?.let { main ->
            main.setPlayInfo(MicAnchor().apply {
                this.streamID = "${mPlayerViewModel.programId}"
                //这里isAnchor = true只是为了方便隐藏主播信息视图 会理解有歧义
                this.isAnchor = true
            })

            //显示封面
            posterUrl?.let {
                main.showCover(posterUrl)
            }
            main.show()
            //播放流
            playByInfo(playInfo, main)

        }
    }

    /**
     * 房间内用户创建流. 主流播放不会变化 这里只处理其他流
     */
    private fun handleStreamAdded(infoList: List<MicAnchor>) {
        logger.info("handleStreamAdded：${infoList}")
        infoList.forEach { info ->

            if (info.streamID == mMainVideoView?.getStreamID()) {
                //主流
                logger.info("跟主流一致${info.streamID}")
                startPlayMain(info.prePic, info.playInfo)
                return@forEach
            }
            if (info.streamID.isNotEmpty() && isStreamExisted(info.streamID)) {
                //流存在，走replace流程
                logger.info("流已经存在或者空串${info.streamID}")
                handleStreamReplace(info)
                return@forEach
            } else {
                //流不存在，走新增流程
                logger.info("流不存在，走新增流程${info.streamID}")
                //播放其他流
                val freeView = if (info.pkType == PKType.LANDLORD) {
                    getRankFreeViewLine()
                } else {
                    getFreeView()
                } ?: return
                freeView.mOnVideoListener = singleVideoListener
                //显示封面
                freeView.showCover(info.prePic)
                //播放流
                playByInfo(info.playInfo ?: return, freeView)
                //设置主播信息
                freeView.setPlayInfo(info)
                freeView.show()
            }
        }
    }

    /**
     * 显示用户信息
     */
    private fun showVideoPlayerInfo() {
        for (viewLive in mViewList) {
            viewLive.showPlayerInfo()
        }
    }

    /**
     * 切换多路流操作 因为是切换所以要在原有的基础上 所以这里以[mViewList]已经存在的播放流为中心进行替换 如果没有匹配到那只能说明这个切换信息有问题
     */
    private fun handleStreamReplace(info: MicAnchor) {

        var isCatch = false
        for (viewLive in mViewList) {
            if (info.streamID == viewLive.getStreamID()) {
                logger.info("替换正在播的流${info.streamID}")
                viewLive.showCover(info.prePic)
                playByInfo(info.playInfo ?: return, viewLive)
                viewLive.setPlayInfo(info)
                isCatch = true
            }
        }
        if (!isCatch) {
            reportCrash("切换信息在当前播放流中不存在 ${info.streamID} 切换信息$info 当前的播放的流$mViewList")
        }
    }

    private fun isStreamExisted(streamID: String): Boolean {
        if (TextUtils.isEmpty(streamID)) {
            return true
        }

        var isExisted = false

        for (viewLive in mViewList) {
            if (streamID == viewLive.getStreamID()) {
                isExisted = true
                break
            }
        }

        return isExisted
    }

    /**
     * 遍历根据[streamID]获取播放view
     */
    protected fun findViewByStream(streamID: String): SingleVideoView? {

        var view: SingleVideoView? = null

        for (viewLive in mViewList) {
            if (streamID == viewLive.getStreamID()) {
                view = viewLive
                break
            }
        }

        return view
    }

    /**
     * 根据数据进行播放
     */
    private fun playByInfo(playInfo: PlayInfo, videoView: SingleVideoView) {
        videoView.play(GlobalUtils.getPlayUrl(playInfo), videoView == mMainVideoView)
//        videoView.play("rtmp://aliyun-rtmp.51lm.tv/lingmeng/24288", videoView == mMainVideoView)
    }

    /**
     * 房间内用户删除流.
     */
    private fun handleStreamDeleted(infoList: List<MicAnchor>) {
        infoList.forEach { info ->
            if (info.streamID == "${mPlayerViewModel.programId}") {
                logger.info("宿主流不能关${info.streamID}")
//                playByInfo(info.playInfo ?: return, mMainVideoView ?: return)
            } else {
                logger.info("删除流消息${info.streamID}")
                stopPlay(info.streamID)
            }
        }
        if (mRankRootView != null) {
            mRankRootView?.hide()
        }
    }

    /**
     * 将拉流全部重置 只保留主流 用于新增拉流时 清空残留的多余流
     */
    @Deprecated("有问题 不用")
    private fun resetAllStream() {
        videoPlayerViewModel.curPlayerList.clear()
        mViewList.forEach {
            if (it.getStreamID() != mMainVideoView?.getStreamID()) {
                it.stop(stopAll = true, needDisConnect = false)
            }
        }
        if (mRankRootView != null) {
            mRankRootView?.hide()
        }
    }

    /**
     * 停止播放
     */
    private fun stopPlay(streamID: String) {
        mViewList.forEach { view ->
            if (view.getStreamID() == streamID) {
                //找到对应的视图
//                view.release()
                view.stop(stopAll = true, needDisConnect = false)
            }
        }
    }

    /**
     * 获取空闲的View
     */
    private fun getFreeView(): SingleVideoView? {
        var vlFreeView: SingleVideoView? = null
        var i = 0
        val size = mViewList.size
        while (i < size) {
            val viewVideo = mViewList[i]
            if (viewVideo.isFree && viewVideo != mMainVideoView) {
                vlFreeView = viewVideo
                vlFreeView.visibility = View.VISIBLE
                break
            }
            i++
        }
        //移除斗地主布局中的播放器放到普通播放器布局中
        val parent = vlFreeView?.parent as? LinearLayout
        if (parent != null && parent.orientation == LinearLayout.VERTICAL) {
            MixedHelper.removeParent(vlFreeView)
            ll_container?.addView(vlFreeView)
            val lp = vlFreeView?.layoutParams as? LinearLayout.LayoutParams
            lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
            lp?.width = 0
            lp?.weight = 1f
        }
        if (vlFreeView != null && vlFreeView.getPlayerVideo() == null) {
            vlFreeView.initPlayer()
        }
        //当总数不足三个还是没取到 说明需要创建  超过三个不再处理
        if (vlFreeView == null && size < 3) {
            vlFreeView = SingleVideoView(requireContext())
            mViewList.add(vlFreeView)
            ll_container.addView(vlFreeView)
            val lp = vlFreeView.layoutParams as? LinearLayout.LayoutParams
            lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
            lp?.width = 0
            lp?.weight = 1f
        }
//
//        vlFreeView?.let { tView ->
//            if (tView.parent == null) {
//                //添加View
//                val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
//                lp.weight = 1f
//                ll_container.addView(tView, lp)
//            }
//        }
        MixedHelper.removeParent(mRankRootView)
        return vlFreeView
    }

    /**
     * 适用于斗地主直播布局
     * 布局结构
     * LinearLayout(HROIZONTAL){ SingleVideoView + LinearLayout(VERTICAL){ SingleVideoView + SingleVideoView }}
     * @author WanZhiYuan
     * @date 2020/04/17
     */
    private fun getRankFreeViewLine(): SingleVideoView? {
        var vlFreeView: SingleVideoView? = null
        var i = 0
        val size = mViewList.size
        while (i < size) {
            val viewVideo = mViewList[i]
            if (viewVideo.isFree && viewVideo != mMainVideoView) {
                vlFreeView = viewVideo
                vlFreeView.visibility = View.VISIBLE
                break
            }
            i++
        }
        if (mRankRootView == null) {
            mRankRootView = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                ll_container?.addView(this)
                val lp = layoutParams as? LinearLayout.LayoutParams
                lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
                lp?.width = 0
                lp?.weight = 1f
            }
        } else if (mRankRootView?.parent == null) {
            ll_container?.addView(mRankRootView)
            val lp = mRankRootView?.layoutParams as? LinearLayout.LayoutParams
            lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
            lp?.width = 0
            lp?.weight = 1f
        }
        if (mRankRootView?.isVisible == false) {
            mRankRootView?.show()
        }
        if (vlFreeView != null) {
            val parent = vlFreeView.parent as? LinearLayout
            if (parent != null && parent.orientation != LinearLayout.VERTICAL) {
                MixedHelper.removeParent(vlFreeView)
                mRankRootView?.addView(vlFreeView)
                val lp = vlFreeView.layoutParams as? LinearLayout.LayoutParams
                lp?.height = 0
                lp?.width = ViewGroup.LayoutParams.MATCH_PARENT
                lp?.weight = 1f
            }
            if (vlFreeView.getPlayerVideo() == null) {
                vlFreeView.initPlayer()
            }
        } else if (size < 3) {
            vlFreeView = SingleVideoView(requireContext())
            mViewList.add(vlFreeView)
            mRankRootView?.addView(vlFreeView)
            val lp = vlFreeView.layoutParams as? LinearLayout.LayoutParams
            lp?.height = 0
            lp?.width = ViewGroup.LayoutParams.MATCH_PARENT
            lp?.weight = 1f
        }
        return vlFreeView
    }


    /**
     * 清空所有流
     * [stopAll]是否需要关闭所有流（单例和常规）
     */
    private fun stopAllStream(stopAll: Boolean, needDisConnect: Boolean = false) {
        mViewList.forEach {
            it.stop(stopAll, needDisConnect)
        }
        if (mRankRootView != null) {
            mRankRootView?.hide()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun videoEvent(bean: VideoPlayerEvent) {
        mViewList.forEach {
            if (it.isFree) {
                return@forEach
            }
            if (bean.start) {
                it.soundOff()
            } else {
                it.soundOn()
            }
        }
    }

    /**
     * 悬浮窗关闭消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun floatingClose(bean: FloatingCloseEvent) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mMainVideoView?.resetHolder()
        } else {
            //如果当前的页面不在焦点 先设置null 防止底层获取不到渲染视图而一直报错
            mMainVideoView?.clearHolder()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAllStream(false)
        mVideoViewModel.playerData.removeObserver(playerDataObserver)

        mVideoViewModel.playInfoData.removeObserver(playInfoDataObserver)


        videoPlayerViewModel.addPlayerDatas.removeObserver(addPlayerDataObserver)

        videoPlayerViewModel.rmPlayerDatas.removeObserver(rmPlayerDataObserver)

        mVideoViewModel.stopAllStreamState.removeObserver(stopAllStreamStateObserver)
    }
}