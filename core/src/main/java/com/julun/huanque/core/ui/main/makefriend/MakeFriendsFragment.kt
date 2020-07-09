package com.julun.huanque.core.ui.main.makefriend

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.julun.huanque.common.base.BaseViewModelFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.HeadModule
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.HomeRecomItem
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.MediaPlayFunctionListener
import com.julun.huanque.common.manager.audio.MediaPlayInfoListener
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_make_friend.*


class MakeFriendsFragment : BaseViewModelFragment<MakeFriendsViewModel>() {

    companion object {
        fun newInstance() = MakeFriendsFragment()
    }

    private val mAdapter: MakeFriendsAdapter by lazy { MakeFriendsAdapter() }
    override fun lazyLoadData() {
        mViewModel.queryInfo()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_make_friend
    }

    private val audioPlayerManager: AudioPlayerManager by lazy { AudioPlayerManager(requireContext()) }
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        //一秒回调一次
        audioPlayerManager.setSleep(1000)
        audioPlayerManager.setMediaPlayFunctionListener(object : MediaPlayFunctionListener {
            override fun prepared() {
                logger.info("prepared")
            }

            override fun start() {
                logger.info("start 总长=${audioPlayerManager.getDuration() / 1000}")
                currentPlayHomeRecomItem?.introduceVoiceLength = audioPlayerManager.getDuration() / 1000
            }

            override fun pause() {
                logger.info("pause")
            }

            override fun stop() {
                logger.info("stop")
            }

            override fun reset() {
                logger.info("reset")
            }
        })
        audioPlayerManager.setMediaPlayInfoListener(object : MediaPlayInfoListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int) {
                logger.info("onError mediaPlayer=${mp.hashCode()} what=$what extra=$extra")
            }

            override fun onCompletion(mediaPlayer: MediaPlayer?) {
                logger.info("onCompletion mediaPlayer=${mediaPlayer.hashCode()}")
            }

            override fun onBufferingUpdate(mediaPlayer: MediaPlayer?, i: Int) {
                logger.info("onBufferingUpdate mediaPlayer=${mediaPlayer.hashCode()} i=$i")
            }

            override fun onSeekComplete(mediaPlayer: MediaPlayer?) {
                logger.info("onSeekComplete mediaPlayer=${mediaPlayer.hashCode()} ")
            }

            override fun onSeekBarProgress(progress: Int) {
                logger.info("onSeekBarProgress progress=${progress / 1000}")
                currentPlayHomeRecomItem?.let {
                    it.currentPlayProcess = it.introduceVoiceLength - progress / 1000
                    mAdapter.notifyItemChanged(currentIndex)
                }
            }
        })
    }

    override fun initEvents(rootView: View) {
        mRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mRecyclerView.adapter = mAdapter
        //去除item刷新的默认动画
        (mRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
//        mAdapter.setOnLoadMoreListener({
//            mViewModel.queryInfo(QueryType.LOAD_MORE)
//        }, mRecyclerView)
        mAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info("loadMoreModule 加载更多")
            mViewModel.queryInfo(QueryType.LOAD_MORE)
        }
        mRefreshView.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
        mAdapter.setOnItemClickListener { _, _, position ->
            logger.info("点击了第几个index=$position")
            val item = mAdapter.getItem(position) ?: return@setOnItemClickListener
            if (item.showType == HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION) {
                logger.info("跳转编辑资料页")
            }

        }
        mAdapter.mOnItemAdapterListener = object : MakeFriendsAdapter.OnItemAdapterListener {
            override fun onPhotoClick(
                index: Int,
                position: Int,
                list: MutableList<PhotoBean>
            ) {
                logger.info("index=$index position=$position ")
                ImageActivity.start(requireActivity(), position, list.map { StringHelper.getOssImgUrl(it.url) })
            }

            override fun onHeadClick(item: HeadModule?) {
                logger.info("头部分类：${item?.moduleType}")
            }

        }
        mAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.btn_action -> {

                    val bean = mAdapter.getItemOrNull(position)?.content as? HomeRecomItem ?: return@setOnItemChildClickListener
                    if (bean.anchor && bean.living) {
                        logger.info("点击围观--$position")
                    } else {
                        logger.info("点击了私信--$position")
                    }
                }
                R.id.iv_audio_play -> {
                    logger.info("点击了音频播放---$position")
                    val bean = mAdapter.getItem(position)?.content as? HomeRecomItem
                    switchAudio(position, bean)
                }
                R.id.iv_guide_tag_close -> {
                    logger.info("点击引导标签关闭---$position")
                }
                R.id.iv_guide_info_close -> {
                    logger.info("点击引导完善资料关闭---$position")
                }
            }
        }
        mRecyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewDetachedFromWindow(view: View) {
                if (view.getTag(R.id.play_tag_key) == ParamConstant.IS_AUDIO_PLAY) {
                    //todo
                    logger.info("播放的item被移除了 ")
                }
            }

            override fun onChildViewAttachedToWindow(view: View) {
                if (view.getTag(R.id.play_tag_key) == ParamConstant.IS_AUDIO_PLAY) {
                    //todo
                    logger.info("播放的item再次添加 ")
                }
            }

        })

    }

    var currentPlayHomeRecomItem: HomeRecomItem? = null
    var currentIndex: Int = -1
    private fun switchAudio(index: Int, bean: HomeRecomItem?) {
        if (bean == null || bean.userId == 0) {
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

    private fun initViewModel() {
        mViewModel.stateList.observe(viewLifecycleOwner, Observer {
            //
            mRefreshView.isRefreshing = false
            if (it.state == NetStateType.SUCCESS) {
                loadData(it.getT())
            } else if (it.state == NetStateType.ERROR) {
                //dodo
            }
        })


    }

    private fun loadData(stateList: RootListData<HomeItemBean>) {

        if (stateList.isPull) {
            mAdapter.setList(stateList.list)
        } else {
            mAdapter.addData(stateList.list)
        }

        if (stateList.hasMore) {
//            mAdapter.loadMoreComplete()
            mAdapter.loadMoreModule.loadMoreComplete()
        } else {
            if (stateList.isPull) {
//                mAdapter.loadMoreEnd(true)
                mAdapter.loadMoreModule.loadMoreEnd(true)
            } else {
//                mAdapter.loadMoreEnd()
                mAdapter.loadMoreModule.loadMoreEnd()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager.destroy()
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
            }
            NetStateType.LOADING -> {//showLoading()
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

}