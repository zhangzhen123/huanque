package com.julun.huanque.core.ui.main.tag_manager

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.ManagerTagBean
import com.julun.huanque.common.bean.beans.TagDetailBean
import com.julun.huanque.common.bean.beans.TagPicBean
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.ManagerTagCode
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.StaggeredDecoration
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_follow.headerPageView
import kotlinx.android.synthetic.main.activity_follow.mRefreshLayout
import kotlinx.android.synthetic.main.activity_tag_pics.*
import org.jetbrains.anko.startActivity

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/29 10:16
 *
 *@Description: TagPicsActivity
 *
 */
class TagPicsActivity : BaseVMActivity<TagPicsViewModel>() {


    companion object {
        fun start(act: Activity, tag: ManagerTagBean, likeUserId: Long? = null) {
            act.startActivity<TagPicsActivity>(ManagerTagCode.TAG_INFO to tag, IntentParamKey.USER_ID.name to likeUserId)
        }

        val width = (ScreenUtils.getScreenWidth() - dp2px(25)) / 2
    }

    override fun getLayoutId(): Int = R.layout.activity_tag_pics

    private val picListAdapter: BaseQuickAdapter<TagPicBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<TagPicBean, BaseViewHolder>(R.layout.item_tag_pic_list), LoadMoreModule {
            override fun convert(holder: BaseViewHolder, item: TagPicBean) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                val sdvLp = sdv.layoutParams as FrameLayout.LayoutParams
                logger.info("holder.adapterPosition =${holder.adapterPosition}")
                val w = width
                val h = if (holder.adapterPosition == 0) {
                    width
                } else {
                    (width * 1.411).toInt()
                }
//                sdvLp.width = w
                sdvLp.height = h
                sdv.requestLayout()
                sdv.loadImageInPx(item.applyPic, w, h)
                if (item.picNum == 0) {
                    holder.setGone(R.id.ll_top_tag, true)
                } else {
                    holder.setGone(R.id.ll_top_tag, false)
                }
            }

        }
    }

    private var currentTag: ManagerTagBean? = null
    private var currentLikeUserId: Long? = null
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        currentTag = intent.getSerializableExtra(ManagerTagCode.TAG_INFO) as? ManagerTagBean
        currentLikeUserId = intent.getLongExtra(IntentParamKey.USER_ID.name, 0L)
        headerPageView.imageViewBack.onClickNew {
            finish()
        }
        headerPageView.textTitle.text = currentTag?.tagName ?: ""

        // rv_pics.layoutManager = GridLayoutManager(this, 2)
        rv_pics.addItemDecoration(StaggeredDecoration(dp2px(5)))
        rv_pics.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        initViewModel()
        iv_like.onClickNew {
            //todo 跨页面 数据共享
        }
        tv_like.onClickNew {

        }
        rv_pics.adapter = picListAdapter


        picListAdapter.setOnItemClickListener { _, _, position ->
            val item = picListAdapter.getItemOrNull(position)
            if (item != null) {
                logger.info("查看大图")

            }
        }

        picListAdapter.loadMoreModule.setOnLoadMoreListener {
            queryData(QueryType.LOAD_MORE)
        }

        mRefreshLayout.setOnRefreshListener {
            queryData(QueryType.REFRESH)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)
        queryData(QueryType.INIT)

    }


    private fun initViewModel() {
        mViewModel.tagDetail.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderData(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadDataFail(it.isRefresh())
            }
            mRefreshLayout.isRefreshing = false
        })
    }

    private fun loadDataFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show2("刷新失败")
        } else {
            picListAdapter.loadMoreModule.loadMoreFail()
        }
    }


    private fun renderData(listData: TagDetailBean<TagPicBean>) {

        val list = listData.authPage.list

        if (listData.authPage.isPull) {
            sdv_tag.loadImage(listData.tagIcon, 15f, 15f)
            tv_tag.text = listData.tagName
            tv_num.text = "认证${listData.authNum}人"
            tv_desc.text = listData.tagDesc
            if (listData.like) {
                iv_like.setImageResource(R.mipmap.icon_tag_like)
                tv_like.text = "取消喜欢"
            } else {
                iv_like.setImageResource(R.mipmap.icon_tag_dislike)
                tv_like.text = "喜欢"
            }
            val programList = list.distinct()
            picListAdapter.setList(programList)

            rv_pics.post {
                rv_pics.scrollToPosition(0)
            }
        } else {
            val programList = list.removeDuplicate(picListAdapter.data)
            picListAdapter.addData(programList)
        }
        if (listData.authPage.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (list.isEmpty()) {
                picListAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                picListAdapter.loadMoreModule.loadMoreComplete()
            }
        } else {
            //防止底部没有边距
            picListAdapter.loadMoreModule.loadMoreEnd()
        }
        if (picListAdapter.data.isEmpty()) {
            picListAdapter.setEmptyView(
                MixedHelper.getEmptyView(
                    this,
                    msg = "暂无数据"/* ,
                    btnTex = "前往",
                   onClick = View.OnClickListener {
                        logger.info("跳转到交友")
                        ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                            .withInt(IntentParamKey.TARGET_INDEX.name, 0).navigation()
                    }*/
                )
            )
        }
    }

    private fun queryData(type: QueryType) {
        val tag = currentTag ?: return
        currentLikeUserId ?: return
        mViewModel.requestTagList(type, tag.tagId, currentLikeUserId!!)
    }

    override fun showLoadState(state: NetState) {

        when (state.state) {
            NetStateType.SUCCESS -> {
                //由于上面在renderData中已经设置了空白页 这里不需要了
//                picListAdapter.setEmptyView(MixedHelper.getEmptyView(this))
            }
            NetStateType.LOADING -> {
                picListAdapter.setEmptyView(MixedHelper.getLoadingView(this))
            }
            NetStateType.ERROR -> {
                picListAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                picListAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            queryData(QueryType.INIT)
                        })
                )

            }
        }


    }

}