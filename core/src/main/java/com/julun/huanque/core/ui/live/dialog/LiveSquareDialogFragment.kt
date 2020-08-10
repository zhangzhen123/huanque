package com.julun.huanque.core.ui.live.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMDialogFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.AuthorFollowBean
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.constant.PlayerFrom
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.LiveSquareViewModel
import kotlinx.android.synthetic.main.fragment_live_square.*
import kotlinx.android.synthetic.main.layout_header_live_square.view.*

class LiveSquareDialogFragment : BaseVMDialogFragment<LiveSquareViewModel>() {

    private val playViewModel: PlayerViewModel by activityViewModels()

    override fun getLayoutId(): Int = R.layout.fragment_live_square

    private val headerLayout: View by lazy {
        LayoutInflater.from(requireContext()).inflate(R.layout.layout_header_live_square, null)
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = 520)
    }

    override fun initViews() {

        headerLayout.followList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        headerLayout.followList.adapter = followAdapter
        followAdapter.setOnItemClickListener { adapter, view, position ->
            val data = adapter.getItem(position) as? AuthorFollowBean
            //Social
            if (data != null) {
                PlayerActivity.start(requireActivity(), data.programId, from = PlayerFrom.Social)
            }

        }

        authorList.layoutManager = GridLayoutManager(requireContext(), 2)
        initViewModel()


        followAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info("followAdapter loadMoreModule 加载更多")
            mViewModel.requestFollowList(false)
        }

        authorAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info(" authorList.stopScroll()")
            authorList.stopScroll()
            logger.info("authorAdapter loadMoreModule 加载更多")
            mViewModel.requestHotList(QueryType.LOAD_MORE, playViewModel.programId)
        }
        authorAdapter.addHeaderView(headerLayout)

        authorList.adapter = authorAdapter
        authorList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
        authorList.isNestedScrollingEnabled = false

        authorAdapter.setOnItemClickListener { _, _, position ->
            val item = authorAdapter.getItemOrNull(position)
            if (item != null) {
                logger.info("跳转直播间${item.programId}")
                playViewModel.checkoutRoom.value = item.programId
            }
        }
        mRefreshLayout.setOnRefreshListener {
            mViewModel.requestFollowList(true)
            mViewModel.requestHotList(QueryType.REFRESH, playViewModel.programId)
        }

        mViewModel.requestFollowList(true)
        mViewModel.requestHotList(QueryType.INIT, playViewModel.programId)
    }

    private fun initViewModel() {
        mViewModel.followList.observe(this, Observer {
            if (it.isSuccess()) {
                renderFollowData(it.getT())
            }
        })

        mViewModel.hotDataList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderHotData(it.getT())
            } else if (it.state == NetStateType.ERROR) {
                ToastUtils.show(it.error?.busiMessage)
            }
            mRefreshLayout.isRefreshing = false
        })
    }

    private fun renderFollowData(data: RootListData<AuthorFollowBean>) {
        if (data.isPull) {
            if (data.list.isEmpty()) {
                headerLayout.followTitle.hide()
                headerLayout.followList.hide()
            } else {
                headerLayout.followTitle.show()
                headerLayout.followList.show()
                headerLayout.hotTitle.show()
//                headerLayout.followTitle.text = "关注"
                followAdapter.setNewInstance(data.list)
                if (data.hasMore) {
                    followAdapter.loadMoreModule.loadMoreComplete()
                } else {
                    followAdapter.loadMoreModule.loadMoreEnd(true)
                }
            }
        } else {
            followAdapter.addData(data.list)
            if (data.hasMore) {
                if (data.list.isEmpty()) {
                    followAdapter.loadMoreModule.loadMoreEnd(false)
                } else {
                    followAdapter.loadMoreModule.loadMoreComplete()
                }

            } else {
                followAdapter.loadMoreModule.loadMoreEnd()
            }

        }
    }

    private fun renderHotData(listData: RootListData<ProgramLiveInfo>) {

        if (listData.isPull) {
            authorAdapter.setList(listData.list)
            if (listData.list.isNotEmpty()) {
                headerLayout.hotTitle.show()
            }
        } else {
            authorAdapter.addData(listData.list)
        }

        if (listData.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (listData.list.isEmpty()) {
                authorAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                authorAdapter.loadMoreModule.loadMoreComplete()
            }

        } else {
            if (listData.isPull) {
                authorAdapter.loadMoreModule.loadMoreEnd(true)
            } else {
                authorAdapter.loadMoreModule.loadMoreEnd()
            }

        }
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
                state_pager_view.showSuccess()
                authorList.show()
                authorAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        requireContext(),
                        msg = "没有开播的主播，去交友看看吧",
                        btnTex = "前往",
                        onClick = View.OnClickListener {
                            //todo
                            logger.info("跳转到xxxx")
                        })
                )

            }
            NetStateType.LOADING -> {//showLoading()
                authorList.hide()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.queryInfo()
                })
            }

        }

    }

    private val followAdapter =
        object : BaseQuickAdapter<AuthorFollowBean, BaseViewHolder>(R.layout.item_live_square_follow_list),
            LoadMoreModule {
            override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

                val holder = super.onCreateDefViewHolder(parent, viewType)
                val img = holder.getView<SimpleDraweeView>(R.id.sdv_living)
                ImageUtils.loadGifImageLocal(img, R.mipmap.gif_is_living01)
                return holder
            }

            override fun convert(holder: BaseViewHolder, item: AuthorFollowBean) {
                holder.setText(R.id.anchorNickname, item.programName)
                ImageUtils.loadImage(
                    holder.getView(R.id.anchorPicture)
                        ?: return, item.anchorPic, 55f, 55f
                )
                if (item.livingStatus) {
                    holder.setGone(R.id.fl_living, false)
                } else {
                    holder.setGone(R.id.fl_living, true)
                }
            }
        }
    private val authorAdapter =
        object : BaseQuickAdapter<ProgramLiveInfo, BaseViewHolder>(R.layout.item_live_square_anchor_list), LoadMoreModule {

            override fun convert(holder: BaseViewHolder, item: ProgramLiveInfo) {
                holder.setText(R.id.anchor_nickname, item.programName).setText(R.id.user_count, "${item.heatValue}")
                ImageUtils.loadImage(
                    holder.getView(R.id.anchorPicture)
                        ?: return, item.coverPic, 150f, 150f
                )
                if (item.city.isEmpty()) {
                    holder.setGone(R.id.anchor_city, true)
                } else {
                    holder.setGone(R.id.anchor_city, false)

                    holder.setText(R.id.anchor_city, item.city)
                }

                if (item.isLiving) {
                    holder.setText(R.id.tv_author_status, "直播中")
                } else {
                    holder.setText(R.id.tv_author_status, "休息中")
                }
            }
        }

}