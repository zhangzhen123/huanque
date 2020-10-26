package com.julun.huanque.core.ui.main.makefriend

import android.content.Intent
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.LoginEvent
import com.julun.huanque.common.bean.events.UserInfoEditEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.HomeMakeMoneyType
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.PlayerFrom
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.MediaPlayFunctionListener
import com.julun.huanque.common.manager.audio.MediaPlayInfoListener
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.AliPlayerManager
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.main.bird.LeYuanBirdActivity
import com.julun.huanque.core.ui.main.home.HomeViewModel
import com.julun.huanque.core.viewmodel.TodayFateViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_make_friend.*
import kotlinx.android.synthetic.main.sticky_mkf_task.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity


class MakeFriendsFragment : BaseVMFragment<MakeFriendsViewModel>() {

    companion object {
        fun newInstance() = MakeFriendsFragment()
    }


    private val mHomeViewModel: HomeViewModel by activityViewModels()

    private val mTodayFateViewModel: TodayFateViewModel by activityViewModels()

    private val mAdapter: MakeFriendsAdapter by lazy { MakeFriendsAdapter() }
    override fun lazyLoadData() {
        mViewModel.queryInfo()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_make_friend
    }

    override fun isRegisterEventBus(): Boolean = true


    private val audioPlayerManager: AudioPlayerManager by lazy { AudioPlayerManager(requireContext()) }
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
        //去除item刷新的默认动画
        (mRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        mAdapter.setEmptyView(MixedHelper.getLoadingView(requireContext()))
        initViewModel()
        //半秒回调一次
        audioPlayerManager.setSleep(500)
        audioPlayerManager.setMediaPlayFunctionListener(object : MediaPlayFunctionListener {
            override fun prepared() {
                logger.info("prepared")
            }

            override fun start() {
                logger.info("start 总长=${audioPlayerManager.getDuration()}")
                //不使用实际的值
//                currentPlayHomeRecomItem?.introduceVoiceLength = (audioPlayerManager.getDuration() / 1000)+1
//                AliplayerManager.soundOff()
            }

            override fun resume() {
                logger.info("resume")
                AliPlayerManager.soundOff()
            }

            override fun pause() {
                logger.info("pause")
                AliPlayerManager.soundOn()
            }

            override fun stop() {
                logger.info("stop")
                AliPlayerManager.soundOn()
            }


        })
        audioPlayerManager.setMediaPlayInfoListener(object : MediaPlayInfoListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int) {
                logger.info("onError mediaPlayer=${mp.hashCode()} what=$what extra=$extra")
            }

            override fun onCompletion(mediaPlayer: MediaPlayer?) {
                logger.info("onCompletion mediaPlayer=${mediaPlayer.hashCode()}")
                currentPlayHomeRecomItem?.let {
                    it.currentPlayProcess = it.introduceVoiceLength
                    it.isPlay = false
                    mAdapter.notifyItemChanged(currentIndex)
                }
                //播完后恢复原样
                currentPlayHomeRecomItem = null
                currentIndex = -1
            }

            override fun onBufferingUpdate(mediaPlayer: MediaPlayer?, i: Int) {
                logger.info("onBufferingUpdate mediaPlayer=${mediaPlayer.hashCode()} i=$i")
            }

            override fun onSeekComplete(mediaPlayer: MediaPlayer?) {
                logger.info("onSeekComplete mediaPlayer=${mediaPlayer.hashCode()} ")
            }

            override fun onSeekBarProgress(progress: Int) {
//                logger.info("onSeekBarProgress progress=${progress / 1000}")
                currentPlayHomeRecomItem?.let {
                    it.currentPlayProcess = it.introduceVoiceLength - progress / 1000
                    if (it.currentPlayProcess <= 0) {
                        it.currentPlayProcess = 0
                    }
                    mAdapter.notifyItemChanged(currentIndex)
                }
            }
        })
        tv_balance_h.setTFDinCdc2()
        MixedHelper.setSwipeRefreshStyle(mRefreshView)
    }

    override fun initEvents(rootView: View) {

        mAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info("loadMoreModule 加载更多")
            mViewModel.queryInfo(QueryType.LOAD_MORE)
        }
        mRefreshView.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
        mAdapter.onAdapterClickNew { _, _, position ->
            logger.info("点击了第几个index=$position")
//            pauseAudio()
            val item = mAdapter.getItem(position) ?: return@onAdapterClickNew
            when (item.showType) {
                HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION -> {
                    logger.info("跳转编辑资料页")
                    RNPageActivity.start(requireActivity(), RnConstant.EDIT_MINE_HOMEPAGE)
                }
                HomeItemBean.GUIDE_TO_ADD_TAG -> {
                    logger.info("跳转添加标签页")
                    RNPageActivity.start(requireActivity(), RnConstant.EDIT_MINE_HOMEPAGE)
                }
                HomeItemBean.NORMAL -> {
                    val bean = item.content as? HomeRecomItem ?: return@onAdapterClickNew
                    if (bean.userId == SessionUtils.getUserId()) {
                        RNPageActivity.start(requireActivity(), RnConstant.MINE_HOMEPAGE)
                    } else {
                        RNPageActivity.start(
                            requireActivity(),
                            RnConstant.PERSONAL_HOMEPAGE,
                            Bundle().apply { putLong("userId", bean.userId) })
                    }

                }
            }

        }
        mAdapter.mOnItemAdapterListener = object : MakeFriendsAdapter.OnItemAdapterListener {
            override fun onPhotoClick(
                index: Int,
                position: Int,
                list: MutableList<PhotoBean>
            ) {
                logger.info("index=$index position=$position ")
                val item = mAdapter.getItemOrNull(index)?.content as? HomeRecomItem
                ImageActivity.start(
                    requireActivity(),
                    position,
                    list.map { StringHelper.getOssImgUrl(it.url) },
                    item?.userId
                )
            }

            override fun onHeadClick(item: HeadModule?) {
                logger.info("头部分类：${item?.type}")
                when (item?.type) {
                    HeadModule.MaskQueen -> {

                    }
                    HeadModule.AnonymousVoice -> {
                        //匿名语音
                        ARouter.getInstance().build(ARouterConstant.ANONYMOUS_VOICE_ACTIVITY).navigation()
                    }
                    HeadModule.MagpieParadise -> {
                        requireActivity().startActivity<LeYuanBirdActivity>()
                    }
                    HeadModule.HotLive -> {
                        //热门直播
                        requireActivity().startActivity<PlayerActivity>(ParamConstant.FROM to PlayerFrom.Home)

                    }
                    HeadModule.PlumFlower -> {
                        //花魁
                        activity?.let { act ->
                            val intent = Intent(act, PlumFlowerActivity::class.java)
                            if (ForceUtils.activityMatch(intent)) {
                                act.startActivity(intent)
                            }
                        }
                    }
                    else -> {

                    }
                }

            }

        }
        mAdapter.onAdapterChildClickNew { _, view, position ->
            when (view.id) {
                R.id.btn_action -> {

                    val bean = mAdapter.getItemOrNull(position)?.content as? HomeRecomItem ?: return@onAdapterChildClickNew
                    if (bean.anchor && bean.living) {
                        logger.info("点击围观--$position")
                        PlayerActivity.start(requireActivity(), programId = bean.userId, from = PlayerFrom.Home)
                    } else {
                        logger.info("点击了私信--$position")
                        val bundle = Bundle()
                        bundle.putLong(ParamConstant.TARGET_USER_ID, bean.userId)
                        bundle.putString(ParamConstant.NICKNAME, bean.nickname)
                        bundle.putString(ParamConstant.HeaderPic, bean.headPic)
                        ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle)
                            .navigation(requireActivity())
                    }
                }
                R.id.ll_audio -> {
                    logger.info("点击了音频播放---$position")
                    val bean = mAdapter.getItem(position)?.content as? HomeRecomItem
                    switchAudio(position, bean)
                }
                R.id.iv_guide_tag_close -> {
                    logger.info("点击引导标签关闭---$position")
                    mAdapter.removeAt(position)
                    mViewModel.guideCloseByUser1 = true
                }
                R.id.iv_guide_info_close -> {
                    logger.info("点击引导完善资料关闭---$position")
                    mAdapter.removeAt(position)
                    mViewModel.guideCloseByUser2 = true
                }
                R.id.ll_balance -> {
                    logger.info("零钱")
//                    requireActivity().startActivity<WithdrawActivity>()
                    RNPageActivity.start(requireActivity(), RnConstant.SmallChangePage)
                }
                R.id.ll_task -> {
                    logger.info("去赚钱")
                    gotoMakeMoney()
                }
                R.id.living_fg -> {
                    //点击了开播的头像
                    val bean = mAdapter.getItemOrNull(position)?.content as? HomeRecomItem ?: return@onAdapterChildClickNew
                    if (bean.anchor && bean.living) {
                        logger.info("点击围观--$position")
                        PlayerActivity.start(requireActivity(), programId = bean.userId, from = PlayerFrom.Home)
                    }
                }
            }
        }
        ll_balance_h.onClickNew {
            logger.info("零钱")
//            requireActivity().startActivity<WithdrawActivity>()
            RNPageActivity.start(requireActivity(), RnConstant.SmallChangePage)
        }
        ll_task_h.onClickNew {
            logger.info("去赚钱")
            gotoMakeMoney()
        }
        mRecyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewDetachedFromWindow(view: View) {
                if (view.getTag(R.id.play_tag_key) == ParamConstant.IS_AUDIO_PLAY) {
                    logger.info("播放的item被移除了 ")
                }
            }

            override fun onChildViewAttachedToWindow(view: View) {
                if (view.getTag(R.id.play_tag_key) == ParamConstant.IS_AUDIO_PLAY) {
                    logger.info("播放的item再次添加 ")
                }
            }

        })

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var isUserDo = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserDo = true
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                val viewHead: View? = linearLayoutManager.findViewByPosition(0)
                val view: View? = linearLayoutManager.findViewByPosition(1)

                if (!mTodayFateViewModel.hasShowTodayFate && isUserDo) {
                    val lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
//                    logger.info("当前的位置=$lastPosition")
                    if (lastPosition in 11..30) {
                        checkShouldShowFate()
                    }
                }

//                logger.info("viewHead=${viewHead?.id}  view=$view ")
                if (viewHead == null || view == null) {
                    if (!ic_sticky_mkf_task.isVisible() && isUserDo) {
                        logger.info("此时需要显示粘性布局")
                        ic_sticky_mkf_task.show()
                    }
                    return
                }
                if (viewHead.id == R.id.mkf_header_container) {
                    val top = view.top

                    if (top <= dp2px(45)) {
                        ic_sticky_mkf_task.show()
                    } else {
                        ic_sticky_mkf_task.hide()
                    }
                }

            }
        })

    }

    private fun checkShouldShowFate() {
        if (!mTodayFateViewModel.hasShowTodayFate) {
            mTodayFateViewModel.hasShowTodayFate = true

            val date = StorageHelper.getLastTodayFateTime()

            val today = DateHelper.formatNow()

            val diff = if (date.isNotEmpty()) {
                DateHelper.getDifferDays(date, today)
            } else {
                -1
            }
            ULog.i("今日缘分间隔时间：$diff")
            if (diff == -1 || diff >= 1) {
                mTodayFateViewModel.requestInfo()
            }
        }
    }

    private fun gotoMakeMoney() {
        val type: String = currentHeadInfo?.taskBar?.type ?: return
        when (type) {
            HomeMakeMoneyType.Invite -> RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
            HomeMakeMoneyType.Game -> {
//                ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY).withInt(IntentParamKey.TARGET_INDEX.name, 1)
//                    .navigation()
                requireActivity().startActivity<LeYuanBirdActivity>()
            }
            HomeMakeMoneyType.Task -> RNPageActivity.start(requireActivity(), RnConstant.CHAT_COLLEGE_PAGE)
        }

    }

    var currentPlayHomeRecomItem: HomeRecomItem? = null
    var currentIndex: Int = -1
    private fun switchAudio(index: Int, bean: HomeRecomItem?) {
        if (bean == null || bean.userId == 0L) {
            return
        }
        //如果此时操作的时同一个item的播放音频
        if (bean.userId == currentPlayHomeRecomItem?.userId) {
            if (bean.isPlay) {
                logger.info("此时操作的是同一个item的播放音频 pause")
                audioPlayerManager.pause()
            } else {
                logger.info("此时操作的是同一个item的播放音频 resume")
                audioPlayerManager.resume()
            }
            bean.isPlay = !bean.isPlay
        } else {
            //如果已经有播放 先前的播放重置
            if (currentPlayHomeRecomItem != null) {
                currentPlayHomeRecomItem?.currentPlayProcess = currentPlayHomeRecomItem!!.introduceVoiceLength
                currentPlayHomeRecomItem?.isPlay = false
                mAdapter.notifyItemChanged(currentIndex)
            }
            audioPlayerManager.setNetPath(StringHelper.getOssAudioUrl(bean.introduceVoice))
            audioPlayerManager.start()
            bean.isPlay = true
            currentPlayHomeRecomItem = bean
            currentIndex = index
        }

        mAdapter.notifyItemChanged(index)
    }

    /**
     * 暂停音频
     */
    private fun pauseAudio() {
        if (currentPlayHomeRecomItem?.isPlay == true) {
            audioPlayerManager.pause()
            currentPlayHomeRecomItem?.isPlay = false
            mAdapter.notifyItemChanged(currentIndex)
        }
    }

    override fun onPause() {
        logger.info("onPause")
        super.onPause()
        pauseAudio()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        logger.info("onHiddenChanged=$hidden")
        super.onHiddenChanged(hidden)
        if (hidden) {
            pauseAudio()
        }
    }

    //父组件调用
    fun hideTodo() {
        logger.info("hideTodo")
        pauseAudio()
    }

    private fun initViewModel() {
        mViewModel.stateList.observe(viewLifecycleOwner, Observer {
            //
            mRefreshView.isRefreshing = false
            if (it.state == NetStateType.SUCCESS) {
                loadData(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadFail(it.isRefresh())
            }
            if (it.queryType == QueryType.INIT) {
                mTodayFateViewModel.hasShowTodayFate = false
            }
        })
        mViewModel.flowerPic.observe(viewLifecycleOwner, Observer {
            //
            mHomeViewModel.flowerPic.value = it
        })


    }

    private var currentHeadInfo: HeadNavigateInfo? = null
    private fun loadData(stateList: RootListData<HomeItemBean>) {

        if (stateList.isPull) {
            //每次刷新后重置播放
            audioPlayerManager.stop()
            currentPlayHomeRecomItem = null
            mAdapter.setList(stateList.list)
        } else {
            mAdapter.addData(stateList.list)
        }

        if (stateList.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (stateList.list.isEmpty()) {
                mAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                mAdapter.loadMoreModule.loadMoreComplete()
            }

        } else {
            if (stateList.isPull) {
                mAdapter.loadMoreModule.loadMoreEnd(true)
            } else {
                mAdapter.loadMoreModule.loadMoreEnd()
            }

        }
        val headerData = stateList.list.getOrNull(0)
        if (headerData?.showType == HomeItemBean.HEADER) {
            ic_sticky_mkf_task.hide()
            val headerInfo = headerData.content as? HeadNavigateInfo ?: return
            currentHeadInfo = headerInfo
            tv_balance_h.text = headerInfo.taskBar.myCash
            val content = StringBuilder()
            content.append("${headerInfo.taskBar.label}：")
            val start = content.length//记录开始位置
            content.append(headerInfo.taskBar.desc)
            val styleSpan1A = StyleSpan(Typeface.BOLD)
//                val end = content.length
            val sp = SpannableString(content)
            sp.setSpan(styleSpan1A, 0, start, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            tv_task_h.text = sp
        }

    }

    private fun loadFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show("刷新失败")
        } else {
            mAdapter.loadMoreModule.loadMoreFail()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager.destroy()
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {
                mAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext()))
            }
            NetStateType.LOADING -> {
                mAdapter.setEmptyView(MixedHelper.getLoadingView(requireContext()))
            }
            NetStateType.ERROR -> {
                mAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = requireContext(),
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                mAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = requireContext(),
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
        }
    }

    //
    fun scrollToTopAndRefresh() {
        mRecyclerView.smoothScrollToPosition(0)
        mViewModel.queryInfo(QueryType.REFRESH)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun receiveLoginCode(event: LoginEvent) {
        logger.info("登录事件:${event.result}")
        if (event.result) {
            mViewModel.queryInfo(QueryType.REFRESH)
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveEditInfo(event: UserInfoEditEvent) {
        logger.info("修改信息")
        if (!event.headPic.isNullOrEmpty()) {
            SessionUtils.setHeaderPic(event.headPic!!)
        }
        if (!event.nickname.isNullOrEmpty()) {
            SessionUtils.setNickName(event.nickname!!)
        }
        mViewModel.curRemind?.let {
            if (!event.picList.isNullOrEmpty()) {
                it.picList = event.picList!!
            }

        }
        val index = mAdapter.data.indexOfFirst { it.showType == HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION }
        if(index >= 0){
            mAdapter.notifyItemRemoved(index)
        }
    }
}