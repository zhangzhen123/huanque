package com.julun.huanque.core.ui.live.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.bean.beans.BottomActionBean
import com.julun.huanque.common.bean.beans.MicAnchor
import com.julun.huanque.common.bean.beans.PlayInfo
import com.julun.huanque.common.bean.events.VideoPlayerEvent
import com.julun.huanque.common.constant.ClickType
import com.julun.huanque.common.constant.PKType
import com.julun.huanque.common.constant.ScreenType
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
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

    //保存播放使用的View
    private val mViewList = mutableListOf<SingleVideoView>()

    private val singleVideoListener: SingleVideoView.OnVideoListener by lazy {
        object : SingleVideoView.OnVideoListener {
            override fun onClickAuthorInfo(authorInfo: MicAnchor) {
                logger.info("点击了主播信息$authorInfo")
                mPlayerViewModel.checkoutRoom.value = authorInfo.programId
            }
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
        mVideoViewModel.stopAllStreamState.value = null

        //这个一定要放在前面 不然会后响应 执行顺序就乱了
        mVideoViewModel.playerData.observe(this, Observer {
            if (it != null) {
                mVideoViewModel.stopAllStreamState.value = null
                startPlayMain(it.programPoster, it.playinfo)
            }
        })

        mVideoViewModel.playInfoData.observe(this, Observer {
            if (it != null) {
//                mMainVideoView?.mProgramID = mVideoViewModel?.programId ?: 0
//                playByInfo(it, mMainVideoView ?: return@Observer)
                startPlayMain(playInfo = it)
            }
        })

        videoPlayerViewModel.showVideoInfo.observe(this, Observer {
            if (it != null) {
                showVideoPlayerInfo()
            }
        })


        videoPlayerViewModel.addPlayerDatas.observe(this, Observer {
            if (it != null) {
                handleStreamAdded(it)
                videoPlayerViewModel.addPlayerDatas.value = null
            }
        })

        videoPlayerViewModel.rmPlayerDatas.observe(this, Observer {
            if (it != null) {
//                logger.info("删除流通知：${it?.size}")
                handleStreamDeleted(it)
                videoPlayerViewModel.rmPlayerDatas.value = null

            }
        })

        mVideoViewModel.stopAllStreamState.observe(this, Observer {
            if (it == true) {
                stopAllStream()
                mVideoViewModel.stopAllStreamState.value = null
            }
        })

//        mVideoViewModel?.baseData?.observe(this, Observer {
//            if (it != null) {
//                //显示封面
//                mMainVideoView?.showCover(it.prePic)
//                //播放流
//                mMainVideoView?.mProgramID = mVideoViewModel?.programId ?: 0
//                playByInfo(it.playInfo ?: return@Observer, mMainVideoView ?: return@Observer)
//
//            }
//        })
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
    }

    /**
     * 开始播放主流.
     */
    private fun startPlayMain(posterUrl: String? = null, playInfo: PlayInfo?) {

        if (playInfo == null || mMainVideoView == null) {
            return
        }
        if (mMainVideoView?.getStreamUrl() == GlobalUtils.getPlayUrl(playInfo)) {
            logger.info("播放的流地址一致")
            return
        }
        // 设置流信息
        mMainVideoView?.let { main ->
            main.setPlayInfo(MicAnchor().apply {
                this.streamID = "${mVideoViewModel.programId}"
                //这里isAnchor = true只是为了方便隐藏主播信息视图 会理解有歧义
                this.isAnchor = true
            })
            main.show()
            //显示封面
            posterUrl?.let {
                mMainVideoView?.showCover(posterUrl)
            }
            //播放流
            playByInfo(playInfo, mMainVideoView ?: return)
            mVideoViewModel.playerData.value = null
        }
    }

    /**
     * 房间内用户创建流. 主流播放不会变化 这里只处理其他流
     */
    private fun handleStreamAdded(infoList: List<MicAnchor>) {
        infoList.forEach { info ->
            logger.info("新增流消息${info.streamID}")
            if (info.streamID == mMainVideoView?.getStreamID()) {
                //主流
                startPlayMain(info.prePic, info.playInfo)
                return@forEach
            }
            if (info.streamID.isNotEmpty() && isStreamExisted(info.streamID)) {
                //流存在，走replace流程
                handleStreamReplace(info)
                logger.info("流已经存在或者空串${info.streamID}")
                return@forEach
            } else {
                //流不存在，走新增流程
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
            if (info.streamID == "${mVideoViewModel.programId}") {
                logger.info("宿主流不能关${info.streamID}")
                playByInfo(info.playInfo ?: return, mMainVideoView ?: return)
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
     * 停止播放
     */
    private fun stopPlay(streamID: String) {
        mViewList.forEach { view ->
            if (view.getStreamID() == streamID) {
                //找到对应的视图
                view.release()
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
     */
    private fun stopAllStream() {
        mViewList.forEach {
            it.release()
//            if (it == mMainVideoView) {
//                //主流不要release，会影响下次播放
//                it.stop()
//            } else {
//                it.release()
//            }
        }
        if (mRankRootView != null) {
            mRankRootView?.hide()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun VideoEvent(bean: VideoPlayerEvent) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        stopAllStream()
    }
}